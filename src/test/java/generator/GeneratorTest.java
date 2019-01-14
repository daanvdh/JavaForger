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
import java.io.LineNumberReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import configuration.FreeMarkerConfiguration;
import configuration.JavaForgerConfiguration;
import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import inputClassesForTests.Product;
import parameters.TemplateInputParameters;

/**
 * Unit test for {@link Generator}.
 *
 * @author Daan
 */
public class GeneratorTest {

  private Generator gen = new Generator();
  private JavaForgerConfiguration genConfig;

  @Before
  public void setup() throws IOException {
    Configuration freeMarkerConfig = FreeMarkerConfiguration.getDefaultConfig();
    freeMarkerConfig.setDirectoryForTemplateLoading(new File("src/test/resources/templates"));
    genConfig = JavaForgerConfiguration.builder().withFreeMarkerConfiguration(freeMarkerConfig).build();
  }

  @Test
  public void testExecute_simple() throws IOException, TemplateException {
    String template = "simple.ftlh";
    String expected = "This is a simple test template.";
    executeAndVerify(template, null, null, expected);
  }

  @Test
  public void testExecute_conditionFalse() throws IOException, TemplateException {
    TemplateInputParameters map = new TemplateInputParameters();
    map.put("user", "Steve");
    String template = "condition.ftlh";
    String expected = "Welcome Steve, minion of Big Joe!";
    executeAndVerify(template, null, map, expected);
  }

  @Test
  public void testExecute_conditionTrue() throws IOException, TemplateException {
    TemplateInputParameters map = new TemplateInputParameters();
    map.put("user", "Big Joe");
    String template = "condition.ftlh";
    String expected = "Welcome Big Joe, our beloved leader!";
    executeAndVerify(template, null, map, expected);
  }

  @Test
  public void testExecute_objectAsInput() throws IOException, TemplateException {
    TemplateInputParameters map = new TemplateInputParameters();
    map.put("prod", new Product("tovernaar", "goochelen"));
    String template = "object.ftlh";
    String expected = "The product with 2 properties: name=tovernaar, url=goochelen";
    executeAndVerify(template, null, map, expected);
  }

  @Test
  public void testExecute_includeOtherTemplate() throws IOException, TemplateException {
    TemplateInputParameters map = new TemplateInputParameters();
    map.put("prod", new Product("homeopathie", "magie"));
    String template = "include.ftlh";
    String expected = "We can also include stuff:\n" + "The product with 2 properties: name=homeopathie, url=magie";
    executeAndVerify(template, null, map, expected);
  }

  @Test
  public void testExecute_sequenceEmpty() throws IOException, TemplateException {
    TemplateInputParameters map = new TemplateInputParameters();
    map.put("products", new ArrayList<>());
    String template = "sequence.ftlh";
    String expected = "We hebben deze dieren:";
    executeAndVerify(template, null, map, expected);
  }

  @Test
  public void testExecute_sequence() throws IOException, TemplateException {
    TemplateInputParameters map = new TemplateInputParameters();
    Product p1 = new Product("sprinkhaan", "springen");
    Product p2 = new Product("eend", "wachelen");
    map.put("products", Arrays.asList(p1, p2));

    String template = "sequence.ftlh";
    String expected = "We hebben deze dieren:\n" + "Een sprinkhaan kan springen.\n" + "Een eend kan wachelen.";
    executeAndVerify(template, null, map, expected);
  }

  @Test
  public void testExecute_fillFromClassFile() throws IOException, TemplateException {
    String template = "classFields.ftlh";
    String inputClass = "src/test/java/inputClassesForTests/Product.java";
    String expected = "The input class has the following fields:\n" + "String url\n" + "String name\n" + "Product prod";
    executeAndVerify(template, inputClass, null, expected);
  }

  private void executeAndVerify(String template, String inputClass, TemplateInputParameters map, String expected) throws IOException, TemplateException {
    CodeSnipit code = execute(template, inputClass, map);
    verifyEquals(expected, code.toString());
  }

  private CodeSnipit execute(String template, String inputClass, TemplateInputParameters map) throws IOException, TemplateException {
    if (map != null) {
      genConfig.setInputParameters(map);
    }
    if (template != null) {
      genConfig.setTemplate(template);
    }
    CodeSnipit code = gen.execute(genConfig, inputClass);
    return code;
  }

  /**
   * We don't care about all the stupid line endings, so this is an assert that is agnostic to some differences. Not everything you can think of is covered
   * within this method, just the bare minimum.
   *
   * @throws IOException
   */
  private void verifyEquals(String expected, String actual) throws IOException {
    LineNumberReader reader1 = new LineNumberReader(new StringReader(expected));
    LineNumberReader reader2 = new LineNumberReader(new StringReader(actual));

    String line1 = reader1.readLine();
    String line2 = reader2.readLine();
    boolean equal = true;

    while (equal && line1 != null) {
      equal = line1.equals(line2);
      line1 = reader1.readLine();
      line2 = reader2.readLine();
    }

    equal = equal && line1 == null && line2 == null;

    if (!equal) {
      System.err.println("Actual: " + actual);
      System.err.println("Expected: " + expected);
    }

    Assert.assertTrue("Was not equal on line " + reader1.getLineNumber() + " expected: " + line1 + " actual: " + line2, equal);
  }

}
