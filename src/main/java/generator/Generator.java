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

import com.github.javaparser.ast.Modifier;

import freemarker.core.ParseException;
import freemarker.template.MalformedTemplateNameException;
import freemarker.template.TemplateException;
import freemarker.template.TemplateNotFoundException;
import generator.GeneratorConfiguration.Builder;
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
    return execute(GeneratorConfiguration.builder().withInputParameters(inputParameters).withTemplate(template).build(), inputClass);
  }

  public CodeSnipit execute(GeneratorConfiguration genConfig) throws IOException, TemplateException {
    return execute(genConfig, "");
  }

  public CodeSnipit execute(GeneratorConfiguration config, String inputClass) throws IOException, TemplateException {
    TemplateInputParameters inputParameters = getInputParameters(config, inputClass);
    CodeSnipit codeSnipit = processTemplate(config, inputParameters);
    merger.merge(config, codeSnipit);
    executeChildren(config, inputClass, codeSnipit);
    return codeSnipit;
  }

  private void executeChildren(GeneratorConfiguration config, String inputClass, CodeSnipit codeSnipit) {
    List<CodeSnipit> codeSnipits = new ArrayList<>();
    config.getChildConfigs().stream().forEach(conf -> {
      try {
        codeSnipits.add(execute(conf, inputClass));
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

  private CodeSnipit processTemplate(GeneratorConfiguration config, TemplateInputParameters inputParameters)
      throws IOException, TemplateNotFoundException, MalformedTemplateNameException, ParseException, TemplateException {
    Writer writer = new StringWriter();
    config.getTemplate().process(inputParameters, writer);
    return new CodeSnipit(writer.toString());
  }

  private TemplateInputParameters getInputParameters(GeneratorConfiguration config, String inputClass) throws IOException {
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

    Builder builder = GeneratorConfiguration.builder().withoutModifiers(Modifier.STATIC).withTemplate(template); // .withMergeClass(inputClass);
    builder.withChildConfig(GeneratorConfiguration.builder().withoutModifiers(Modifier.STATIC).withTemplate(testTemplate) // .withMergeClass(testMergeClass)
        .build());

    String code = new Generator().execute(builder.build(), inputClass).toString();
    System.out.println(code);
  }

}
