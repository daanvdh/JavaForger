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

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import freemarker.core.ParseException;
import freemarker.template.MalformedTemplateNameException;
import freemarker.template.TemplateException;
import freemarker.template.TemplateNotFoundException;
import generator.JavaForgerConfiguration.Builder;
import merger.CodeSnipitMerger;
import parameters.TemplateInputDefaults;
import parameters.TemplateInputParameters;
import reader.ClassReader;
import reader.FieldReader;
import reader.MethodReader;
import templateInput.VariableDefinition;

/**
 * Class for generating code given a template and already existing java class files.
 *
 * @author Daan
 */
public class Generator {

  private FieldReader fieldReader = new FieldReader();
  private ClassReader classReader = new ClassReader();
  private MethodReader methodReader = new MethodReader();
  private VariableInitializer initializer = new VariableInitializer();
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
    TemplateInputParameters inputParameters = getInputParameters(config, inputClass);
    CodeSnipit codeSnipit = processTemplate(config, inputParameters);
    String mergedClass = merge(config, codeSnipit, inputClass, parentMergeClass);
    executeChildren(config, inputClass, codeSnipit, mergedClass);
    return codeSnipit;
  }

  private String merge(JavaForgerConfiguration config, CodeSnipit codeSnipit, String inputClass, String parentMergeClass) throws IOException {
    String mergeClassPath = null;
    MergeClassProvider mergeClassProvider = config.getMergeClassProvider();
    if (mergeClassProvider != null) {
      switch (mergeClassProvider.provideFrom()) {
      case SELF:
        mergeClassPath = mergeClassProvider.provide("");
        break;
      case INPUT_CLASS:
        mergeClassPath = mergeClassProvider.provide(inputClass);
        break;
      case PARENT_CONFIG_MERGE_CLASS:
        mergeClassPath = mergeClassProvider.provide(parentMergeClass);
        break;
      default:
      }
      merger.merge(config, codeSnipit, mergeClassPath);
    }
    return mergeClassPath;
  }

  private void executeChildren(JavaForgerConfiguration config, String inputClass, CodeSnipit codeSnipit, String parentMergeClass) {
    // TODO provide the parent somehow

    List<CodeSnipit> codeSnipits = new ArrayList<>();
    config.getChildConfigs().stream().forEach(conf -> {
      try {
        codeSnipits.add(execute(conf, inputClass, parentMergeClass));
      } catch (IOException e) {
        throw new RuntimeException(e);
      } catch (TemplateException e) {
        throw new RuntimeException(e);
      }
    });
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

  private TemplateInputParameters getInputParameters(JavaForgerConfiguration config, String inputClass) throws IOException {
    TemplateInputParameters inputParameters = config.getInputParameters();

    if (inputClass != null && !inputClass.isEmpty()) {
      List<VariableDefinition> fields = fieldReader.getFields(config, inputClass);
      initializer.init(fields);
      inputParameters.put(TemplateInputDefaults.FIELDS.getName(), fields);
      inputParameters.put(TemplateInputDefaults.CLASS.getName(), classReader.read(inputClass));
      inputParameters.put(TemplateInputDefaults.METHODS.getName(), methodReader.read(inputClass));

      config.getAdjuster().accept(inputParameters);
    }
    return inputParameters;
  }

  public static void main(String[] args) throws IOException, TemplateException {
    String inputClass = "src/test/java/inputClassesForTests/Product.java";
    String template = "innerBuilder.javat";
    String testMergeClass = "src/test/java/inputClassesForTests/Product.java";
    String testTemplate = "innerBuilderUnitTest.javat";

    Builder builder = JavaForgerConfiguration.builder().withParameterAdjusters(DefaultAdjusters.removeStaticFields()).withTemplate(template); // .withMergeClass(inputClass);
    builder.withChildConfig(JavaForgerConfiguration.builder().withParameterAdjusters(DefaultAdjusters.removeStaticFields()).withTemplate(testTemplate) // .withMergeClass(testMergeClass)
        .build());

    String code = new Generator().execute(builder.build(), inputClass).toString();
    System.out.println(code);
  }

}
