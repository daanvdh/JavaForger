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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import inputClassesForTests.Product;

/**
 * Unit test for {@link Generator}.
 *
 * @author Daan
 */
public class GeneratorTest {

  private Generator gen;

  @Before
  public void setup() throws IOException {
    Configuration config = FreeMarkerConfiguration.getConfig();
    config.setDirectoryForTemplateLoading(new File("src/test/resources/templates"));
    gen = new Generator();
  }

  @Test
  public void testExecute_simple() throws IOException, TemplateException {
    CodeSnipit code = gen.execute("simple.ftlh", "");
    verifyEquals("This is a simple test template.", code.toString());
  }

  @Test
  public void testExecute_conditionFalse() throws IOException, TemplateException {
    Map<String, Object> map = new HashMap<>();
    map.put("user", "Steve");
    CodeSnipit code = gen.execute("condition.ftlh", map);
    verifyEquals("Welcome Steve, minion of Big Joe!", code.toString());
  }

  @Test
  public void testExecute_conditionTrue() throws IOException, TemplateException {
    Map<String, Object> map = new HashMap<>();
    map.put("user", "Big Joe");
    CodeSnipit code = gen.execute("condition.ftlh", map);
    verifyEquals("Welcome Big Joe, our beloved leader!", code.toString());
  }

  @Test
  public void testExecute_objectAsInput() throws IOException, TemplateException {
    Map<String, Object> map = new HashMap<>();
    map.put("prod", new Product("tovernaar", "goochelen"));
    CodeSnipit code = gen.execute("object.ftlh", map);
    verifyEquals("The product with 2 properties: name=tovernaar, url=goochelen", code.toString());
  }

  @Test
  public void testExecute_includeOtherTemplate() throws IOException, TemplateException {
    Map<String, Object> map = new HashMap<>();
    map.put("prod", new Product("homeopathie", "magie"));
    CodeSnipit code = gen.execute("include.ftlh", map);
    verifyEquals("We can also include stuff:\n" + "The product with 2 properties: name=homeopathie, url=magie", code.toString());
  }

  @Test
  public void testExecute_sequenceEmpty() throws IOException, TemplateException {
    Map<String, Object> map = new HashMap<>();
    map.put("products", new ArrayList<>());
    CodeSnipit code = gen.execute("sequence.ftlh", map);
    verifyEquals("We hebben deze dieren:", code.toString());
  }

  @Test
  public void testExecute_sequence() throws IOException, TemplateException {
    Map<String, Object> map = new HashMap<>();
    Product p1 = new Product("sprinkhaan", "springen");
    Product p2 = new Product("eend", "wachelen");
    map.put("products", Arrays.asList(p1, p2));
    CodeSnipit code = gen.execute("sequence.ftlh", map);
    verifyEquals("We hebben deze dieren:\n" + "Een sprinkhaan kan springen.\n" + "Een eend kan wachelen.", code.toString());
  }

  @Test
  public void testExecute_fillFromClassFile() throws IOException, TemplateException {
    String inputClass = "src/test/java/inputClassesForTests/Product.java";
    CodeSnipit code = gen.execute("classFields.ftlh", inputClass, new HashMap<>());
    verifyEquals("The input class has the following fields:\n" + "String url\n" + "String name", code.toString());
  }

  /**
   * We don't care about all the stupid line endings, so this is an assert that is agnostic to some differences. Not everything you can think of is covered
   * within this method, just the bare minimum.
   */
  private void verifyEquals(String expected, String actual) {
    String e = expected.replace("\n", "\r\n");
    if (e.equals(actual)) {
      Assert.assertEquals(e, actual);
    } else {
      Assert.assertEquals(e + "\r\n", actual);
    }
  }

}
