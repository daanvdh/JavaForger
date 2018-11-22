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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.github.javaparser.ast.Modifier;

import freemarker.core.ParseException;
import freemarker.template.Configuration;
import freemarker.template.MalformedTemplateNameException;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateNotFoundException;
import generator.GeneratorConfiguration.Builder;
import merger.CodeSnipitMerger;
import reader.ClassReader;
import reader.FieldReader;
import reader.MethodReader;
import templateInput.TemplateInputValues;
import templateInput.VariableDefinition;

/**
 * Class for generating code given a template and already existing java class files.
 *
 * @author Daan
 */
public class Generator {

  FieldReader reader = new FieldReader();
  VariableInitializer initializer = new VariableInitializer();
  CodeSnipitMerger merger = new CodeSnipitMerger();

  public CodeSnipit execute(String template, Map<String, Object> inputParameters) throws IOException, TemplateException {
    return execute(template, null, inputParameters);
  }

  public CodeSnipit execute(String template, String inputClass) throws IOException, TemplateException {
    return execute(template, inputClass, new HashMap<>());
  }

  public CodeSnipit execute(String template, String inputClass, Map<String, Object> inputParameters) throws IOException, TemplateException {
    return execute(GeneratorConfiguration.builder().withInputParameters(inputParameters).withTemplate(template).build(), inputClass);
  }

  public CodeSnipit execute(GeneratorConfiguration config, String inputClass) throws IOException, TemplateException {
    Map<String, Object> inputParameters = getInputParameters(config, inputClass);
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

  private CodeSnipit processTemplate(GeneratorConfiguration config, Map<String, Object> inputParameters)
      throws IOException, TemplateNotFoundException, MalformedTemplateNameException, ParseException, TemplateException {
    CodeSnipit codeSnipit;
    Configuration cfg = FreeMarkerConfiguration.getConfig();
    Template temp = cfg.getTemplate(config.getTemplate());
    Writer writer = new StringWriter();
    temp.process(inputParameters, writer);
    codeSnipit = new CodeSnipit(writer.toString());
    return codeSnipit;
  }

  private Map<String, Object> getInputParameters(GeneratorConfiguration config, String inputClass) throws IOException {
    Map<String, Object> inputParameters = config.getInputParameters().entrySet().stream().collect(Collectors.toMap(Entry::getKey, Entry::getValue));

    if (inputClass != null && !inputClass.isEmpty()) {
      inputParameters.put(TemplateInputValues.CLASS_NAME.getName(), getClassName(inputClass));
      inputParameters.put(TemplateInputValues.LOWER_CLASS_NAME.getName(), lowerCaseFirstChar(getClassName(inputClass)));

      // TODO maybe better to get it from the config, not decided yet.
      inputParameters.put(TemplateInputValues.CLASS.getName(), new ClassReader().read(inputClass));
      inputParameters.put(TemplateInputValues.METHODS.getName(), new MethodReader().read(inputClass));

      List<VariableDefinition> fields = reader.getFields(config, inputClass);
      initializer.init(fields);
      inputParameters.put(TemplateInputValues.CLASS_FIELDS.getName(), fields);
    }
    return inputParameters;
  }

  private String lowerCaseFirstChar(String s) {
    char[] c = s.toCharArray();
    c[0] = Character.toLowerCase(c[0]);
    return new String(c);
  }

  private String getClassName(String inputClass) {
    String separator = inputClass.contains("/") ? "/" : "\\";
    int firstIndex = inputClass.lastIndexOf(separator) + 1;
    int lastIndex = inputClass.lastIndexOf(".");
    String name = inputClass.substring(firstIndex, lastIndex);
    return name;
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
