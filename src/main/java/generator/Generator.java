/*
 * Copyright 2018 by Daan van den Heuvel.
 *
 * This file is part of JavaForger.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package generator;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import configuration.ClassProvider;
import configuration.JavaForgerConfiguration;
import configuration.StaticJavaForgerConfiguration;
import freemarker.core.ParseException;
import freemarker.template.MalformedTemplateNameException;
import freemarker.template.TemplateException;
import freemarker.template.TemplateNotFoundException;
import merger.CodeSnipitMerger;
import templateInput.TemplateInputParameters;

/**
 * Class for generating code given a template and already existing java class files.
 *
 * @author Daan
 */
public class Generator {

  private CodeSnipitMerger merger = StaticJavaForgerConfiguration.getMerger();
  private TemplateInputParametersService inputService = new TemplateInputParametersService();
  private static StaticJavaForgerConfiguration staticConfig = StaticJavaForgerConfiguration.getConfig();

  public CodeSnipit execute(String template, TemplateInputParameters inputParameters) throws IOException, TemplateException {
    return execute(template, null, inputParameters);
  }

  public CodeSnipit execute(String template, String inputClass) throws IOException, TemplateException {
    return execute(template, inputClass, new TemplateInputParameters());
  }

  public CodeSnipit execute(String template, String inputClass, TemplateInputParameters inputParameters) throws IOException, TemplateException {
    return execute(JavaForgerConfiguration.builder().withInputParameters(inputParameters).withTemplate(template).build(), inputClass);
  }

  public CodeSnipit execute(JavaForgerConfiguration genConfig) throws IOException, TemplateException {
    return execute(genConfig, "");
  }

  public CodeSnipit execute(JavaForgerConfiguration config, String inputClass) throws IOException, TemplateException {
    return execute(config, inputClass, null);
  }

  private CodeSnipit execute(JavaForgerConfiguration config, String inputClass, String parentMergeClass) throws IOException, TemplateException {
    String mergeClassPath = getMergeClass(inputClass, parentMergeClass, config);

    TemplateInputParameters inputParameters = inputService.getInputParameters(config, inputClass, mergeClassPath);
    CodeSnipit codeSnipit = processTemplate(config, inputParameters);
    merge(config, codeSnipit, mergeClassPath, inputParameters);
    executeChildren(config, inputClass, codeSnipit, mergeClassPath);
    return codeSnipit;
  }

  private void merge(JavaForgerConfiguration config, CodeSnipit codeSnipit, String mergeClassPath, TemplateInputParameters inputParameters)
      throws IOException, TemplateException {
    if (mergeClassPath != null && config.isMerge()) {
      boolean exists = new File(mergeClassPath).exists();
      if (!exists) {
        if (!config.isCreateFileIfNotExists()) {
          throw new JavaForgerException("Merge file '" + mergeClassPath + "' does not exist and JavaForgerConfiguration for template "
              + config.getTemplateName() + " is not setup to create it. ");
        }
        if (config.getConfigIfFileDoesNotExist() == null) {
          createAndFillFile(mergeClassPath, codeSnipit);
        } else {
          CodeSnipit codeSnipitInit = processTemplate(config.getConfigIfFileDoesNotExist(), inputParameters);
          createAndFillFile(mergeClassPath, codeSnipitInit);
          executeMerge(config, codeSnipit, mergeClassPath);
        }
      } else {
        executeMerge(config, codeSnipit, mergeClassPath);
      }
    }
  }

  private void executeMerge(JavaForgerConfiguration config, CodeSnipit codeSnipit, String mergeClassPath) throws IOException {
    boolean success = false;
    try {
      merger.merge(config, codeSnipit, mergeClassPath);
      success = true;
    } finally {
      if (!success) {
        codeSnipit.printWithLineNumbers();
      }
    }
  }

  private String getMergeClass(String inputClass, String parentMergeClass, JavaForgerConfiguration config) {
    ClassProvider provider = config.getMergeClassProvider();
    return (provider == null) ? null : provider.provide(inputClass, parentMergeClass);
  }

  private void createAndFillFile(String mergeClassPath, CodeSnipit codeSnipit) throws IOException {
    File f = new File(mergeClassPath);
    if (!f.exists()) {
      f.getParentFile().mkdirs();
    }
    try (PrintWriter writer = new PrintWriter(mergeClassPath, "UTF-8")) {
      writer.write(codeSnipit.toString());
    }
  }

  private void executeChildren(JavaForgerConfiguration config, String parentInputClass, CodeSnipit codeSnipit, String parentMergeClass)
      throws IOException, TemplateException {
    // forloop needed because we cannot throw exceptions from within a stream
    // TODO let execute only throw our own unchecked exception and replace the forloop with stream below.
    // config.getChildConfigs().stream().map(conf -> execute(conf, inputClass, parentMergeClass)).collect(Collectors.toList());
    List<CodeSnipit> codeSnipits = new ArrayList<>();
    for (JavaForgerConfiguration conf : config.getChildConfigs()) {
      String inputClass = conf.getInputClassProvider().provide(parentInputClass, parentMergeClass);
      codeSnipits.add(execute(conf, inputClass, parentMergeClass));
    }
    codeSnipits.forEach(s -> {
      codeSnipit.add("\n======================================================================\n");
      codeSnipit.add(s.toString());
    });
  }

  private CodeSnipit processTemplate(JavaForgerConfiguration config, TemplateInputParameters inputParameters)
      throws IOException, TemplateNotFoundException, MalformedTemplateNameException, ParseException, TemplateException {
    Writer writer = new StringWriter();

    staticConfig.getFreeMarkerConfiguration().getTemplate(config.getTemplate()).process(inputParameters, writer);
    return new CodeSnipit(writer.toString());
  }

}
