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

import com.github.javaparser.JavaParser;

import configuration.DefaultAdjusters;
import configuration.JavaForgerConfiguration;
import configuration.MergeClassProvider;
import freemarker.core.ParseException;
import freemarker.template.MalformedTemplateNameException;
import freemarker.template.TemplateException;
import freemarker.template.TemplateNotFoundException;
import merger.CodeSnipitMerger;
import reader.ClassContainerReader;
import templateInput.ClassContainer;
import templateInput.TemplateInputDefaults;
import templateInput.TemplateInputParameters;

/**
 * Class for generating code given a template and already existing java class files.
 *
 * @author Daan
 */
public class Generator {

  private ClassContainerReader classReader = new ClassContainerReader();
  private CodeSnipitMerger merger = new CodeSnipitMerger();

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
    // TODO should be removed
    setupSymbolSolver(config);

    String mergeClassPath = getMergeClass(inputClass, parentMergeClass, config);

    TemplateInputParameters inputParameters = getInputParameters(config, inputClass, mergeClassPath);
    CodeSnipit codeSnipit = processTemplate(config, inputParameters);
    merge(config, codeSnipit, mergeClassPath);
    executeChildren(config, inputClass, codeSnipit, mergeClassPath);
    return codeSnipit;
  }

  private void setupSymbolSolver(JavaForgerConfiguration config) {
    JavaParser.getStaticConfiguration().setSymbolResolver(config.getSymbolSolver());
  }

  private void merge(JavaForgerConfiguration config, CodeSnipit codeSnipit, String mergeClassPath) throws IOException {
    if (mergeClassPath != null && config.isMerge()) {
      if (config.isCreateFileIfNotExists()) {
        createFileAndFillFile(mergeClassPath, codeSnipit);
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
    MergeClassProvider provider = config.getMergeClassProvider();
    String mergeClassPath = null;
    if (provider != null) {
      switch (provider.provideFrom()) {
      case SELF:
        mergeClassPath = provider.provide("");
        break;
      case INPUT_CLASS:
        mergeClassPath = provider.provide(inputClass);
        break;
      case PARENT_CONFIG_MERGE_CLASS:
        mergeClassPath = provider.provide(parentMergeClass);
        break;
      default:
      }
    }
    return mergeClassPath;
  }

  private void createFileAndFillFile(String mergeClassPath, CodeSnipit codeSnipit) throws IOException {
    File f = new File(mergeClassPath);
    if (!f.exists()) {
      f.getParentFile().mkdirs();
    }
    try (PrintWriter writer = new PrintWriter(mergeClassPath, "UTF-8")) {
      writer.write(codeSnipit.toString());
    }
  }

  private void executeChildren(JavaForgerConfiguration config, String inputClass, CodeSnipit codeSnipit, String parentMergeClass)
      throws IOException, TemplateException {
    // forloop needed because we cannot throw exceptions from within a stream
    // TODO let execute only throw our own unchecked exception and replace the forloop with stream below.
    // config.getChildConfigs().stream().map(conf -> execute(conf, inputClass, parentMergeClass)).collect(Collectors.toList());
    List<CodeSnipit> codeSnipits = new ArrayList<>();
    for (JavaForgerConfiguration conf : config.getChildConfigs()) {
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
    config.getTemplate().process(inputParameters, writer);
    return new CodeSnipit(writer.toString());
  }

  private TemplateInputParameters getInputParameters(JavaForgerConfiguration config, String inputClass, String mergeClassPath) throws IOException {
    TemplateInputParameters inputParameters = config.getInputParameters();

    if (inputClass != null && !inputClass.isEmpty()) {
      if (!inputParameters.containsKey(TemplateInputDefaults.FIELDS.getName()) && !inputParameters.containsKey(TemplateInputDefaults.CLASS.getName())
          && !inputParameters.containsKey(TemplateInputDefaults.METHODS.getName())
          && !inputParameters.containsKey(TemplateInputDefaults.CONSTRUCTORS.getName())) {
        ClassContainer claz = classReader.read(inputClass, config);
        config.getAdjuster().accept(claz);
        if (!inputParameters.containsKey(TemplateInputDefaults.FIELDS.getName())) {
          inputParameters.put(TemplateInputDefaults.FIELDS.getName(), claz.getFields());
        }
        if (!inputParameters.containsKey(TemplateInputDefaults.CLASS.getName())) {
          inputParameters.put(TemplateInputDefaults.CLASS.getName(), claz);
        }
        if (!inputParameters.containsKey(TemplateInputDefaults.METHODS.getName())) {
          inputParameters.put(TemplateInputDefaults.METHODS.getName(), claz.getMethods());
        }
        if (!inputParameters.containsKey(TemplateInputDefaults.CONSTRUCTORS.getName())) {
          inputParameters.put(TemplateInputDefaults.CONSTRUCTORS.getName(), claz.getConstructors());
        }
      }
    }
    if (mergeClassPath != null) {
      if (!inputParameters.containsKey(TemplateInputDefaults.PACKAGE.getName())) {
        String pack = convertMavenPathToPackage(mergeClassPath);
        inputParameters.put(TemplateInputDefaults.PACKAGE.getName(), pack);
      }
      if (!inputParameters.containsKey(TemplateInputDefaults.MERGE_CLASS_NAME.getName())) {
        String a = mergeClassPath.replace("\\", "*").replace("/", "*");
        String name = a.substring(a.lastIndexOf("*") + 1, a.lastIndexOf("."));
        inputParameters.put(TemplateInputDefaults.MERGE_CLASS_NAME.getName(), name);
      }
    }

    return inputParameters;
  }

  private String convertMavenPathToPackage(String mergeClassPath) {
    String withPoints = mergeClassPath.replace("\\", ".").replace("/", ".");
    String folder = ".src.main.java.";
    int i = withPoints.indexOf(folder);
    String packageWithClass = withPoints.substring(i + folder.length()).replace("\\", ".").replace(".java", "");
    int classIndex = packageWithClass.lastIndexOf(".");
    String pack = packageWithClass.substring(0, classIndex);
    return pack;
  }

  public static void main(String[] args) throws IOException, TemplateException {
    String inputClass = "src/test/java/inputClassesForTests/Product.java";
    String template = "innerBuilder.javat";
    String testMergeClass = "src/test/java/inputClassesForTests/Product.java";
    String testTemplate = "innerBuilderUnitTest.javat";

    JavaForgerConfiguration.Builder builder =
        JavaForgerConfiguration.builder().withParameterAdjusters(DefaultAdjusters.removeStaticFields()).withTemplate(template); // .withMergeClass(inputClass);
    builder.withChildConfig(JavaForgerConfiguration.builder().withParameterAdjusters(DefaultAdjusters.removeStaticFields()).withTemplate(testTemplate) // .withMergeClass(testMergeClass)
        .build());

    String code = new Generator().execute(builder.build(), inputClass).toString();
    System.out.println(code);
  }

}
