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
import java.util.Arrays;
import java.util.List;

import configuration.ClassProvider;
import configuration.JavaForgerConfiguration;
import freemarker.core.ParseException;
import freemarker.template.MalformedTemplateNameException;
import freemarker.template.TemplateException;
import freemarker.template.TemplateNotFoundException;
import merger.CodeSnippetMerger;
import merger.LineMerger;
import merger.git.GitMerger;
import template.TemplateService;
import templateInput.TemplateInputParameters;

/**
 * Class for generating code given a template and already existing java class files.
 *
 * @author Daan
 */
public class Generator {

  private List<CodeSnippetMerger> mergers = Arrays.asList(new GitMerger(this), new LineMerger());
  private TemplateInputParametersService inputService = new TemplateInputParametersService();
  private TemplateService templateService = new TemplateService();

  public CodeSnippet execute(String template, TemplateInputParameters inputParameters) throws IOException, TemplateException {
    return execute(template, null, inputParameters);
  }

  public CodeSnippet execute(String template, String inputClass) throws IOException, TemplateException {
    return execute(template, inputClass, new TemplateInputParameters());
  }

  public CodeSnippet execute(String template, String inputClass, TemplateInputParameters inputParameters) throws IOException, TemplateException {
    return execute(JavaForgerConfiguration.builder().inputParameters(inputParameters).template(template).build(), inputClass);
  }

  public CodeSnippet execute(JavaForgerConfiguration genConfig) throws IOException, TemplateException {
    return execute(genConfig, "");
  }

  public CodeSnippet execute(JavaForgerConfiguration config, String inputClass) throws IOException, TemplateException {
    return execute(config, inputClass, null);
  }

  /**
   * Note that this does not execute any children yet. It also does not support merging yet.
   *
   * @param config
   * @param inputContent The full class to be used as input represented as String.
   * @param inputClass
   * @return {@link CodeSnippet}
   * @throws IOException
   * @throws TemplateException
   */
  public CodeSnippet executeFromContent(JavaForgerConfiguration config, String inputContent, String inputClass, String mergeClassPath)
      throws IOException, TemplateException {
    TemplateInputParameters inputParameters = inputService.getInputParametersFromFileContent(config, inputContent, mergeClassPath);
    CodeSnippet codeSnipit = execute(config, inputClass, mergeClassPath, inputParameters);
    return codeSnipit;
  }

  public CodeSnippet execute(JavaForgerConfiguration config, String inputClass, String mergeClassPath) throws IOException, TemplateException {
    TemplateInputParameters inputParameters = inputService.getInputParameters(config, inputClass, mergeClassPath);
    CodeSnippet codeSnipit = execute(config, inputClass, mergeClassPath, inputParameters);
    return codeSnipit;
  }

  private CodeSnippet execute(JavaForgerConfiguration config, String inputClass, String mergeClassPath, TemplateInputParameters inputParameters)
      throws IOException, TemplateNotFoundException, MalformedTemplateNameException, ParseException, TemplateException {
    CodeSnippet codeSnipit = processTemplate(config, inputParameters);
    merge(config, codeSnipit, mergeClassPath, inputParameters, inputClass);
    executeChildren(config, inputClass, codeSnipit, mergeClassPath);
    return codeSnipit;
  }

  private void merge(JavaForgerConfiguration config, CodeSnippet codeSnipit, String mergeClassPath, TemplateInputParameters inputParameters,
      String inputFilePath) throws IOException, TemplateException {
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
          CodeSnippet codeSnipitInit = processTemplate(config.getConfigIfFileDoesNotExist(), inputParameters);
          createAndFillFile(mergeClassPath, codeSnipitInit);
          executeMerge(config, codeSnipit, mergeClassPath, inputFilePath);
        }
      } else {
        executeMerge(config, codeSnipit, mergeClassPath, inputFilePath);
      }
    }
  }

  private void executeMerge(JavaForgerConfiguration config, CodeSnippet codeSnipit, String mergeClassPath, String inputFilePath) {
    boolean success = false;
    try {
      mergers.stream().filter(m -> m.supports(config)).findFirst().ifPresent(m -> m.merge(config, codeSnipit, mergeClassPath, inputFilePath));
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

  private void createAndFillFile(String mergeClassPath, CodeSnippet codeSnipit) throws IOException {
    File f = new File(mergeClassPath);
    if (!f.exists()) {
      f.getParentFile().mkdirs();
    }
    try (PrintWriter writer = new PrintWriter(mergeClassPath, "UTF-8")) {
      writer.write(codeSnipit.toString());
    }
  }

  private void executeChildren(JavaForgerConfiguration config, String parentInputClass, CodeSnippet codeSnipit, String parentMergeClass)
      throws IOException, TemplateException {
    // forloop needed because we cannot throw exceptions from within a stream
    // TODO let execute only throw our own unchecked exception and replace the forloop with stream below.
    // config.getChildConfigs().stream().map(conf -> execute(conf, inputClass, parentMergeClass)).collect(Collectors.toList());
    List<CodeSnippet> codeSnipits = new ArrayList<>();
    for (JavaForgerConfiguration conf : config.getChildConfigs()) {
      String inputClass = conf.getInputClassProvider().provide(parentInputClass, parentMergeClass);
      String mergeClassPath = getMergeClass(inputClass, parentMergeClass, config);
      codeSnipits.add(execute(conf, inputClass, mergeClassPath));
    }
    codeSnipits.forEach(s -> {
      codeSnipit.add("\n======================================================================\n");
      codeSnipit.add(s.toString());
    });
  }

  private CodeSnippet processTemplate(JavaForgerConfiguration config, TemplateInputParameters inputParameters)
      throws IOException, TemplateNotFoundException, MalformedTemplateNameException, ParseException, TemplateException {
    Writer writer = new StringWriter();
    templateService.getTemplate(config).process(inputParameters, writer);
    return new CodeSnippet(writer.toString());
  }

}
