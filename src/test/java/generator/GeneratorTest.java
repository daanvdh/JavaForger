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
import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Test;

import freemarker.template.TemplateException;
import inputClassesForTests.Product;
import templateInput.TemplateInputParameters;
import templateTests.AbstractTemplateExecutingTest;

/**
 * Unit test for {@link Generator}.
 *
 * @author Daan
 */
public class GeneratorTest extends AbstractTemplateExecutingTest {

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

}
