/*
 * Copyright 2019 by Daan van den Heuvel.
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
package templateTests;

import java.io.File;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.StringReader;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

import configuration.FreeMarkerConfiguration;
import configuration.JavaForgerConfiguration;
import configuration.StaticJavaForgerConfiguration;
import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import generator.CodeSnipit;
import generator.Generator;
import templateInput.TemplateInputParameters;

/**
 * Class with helper methods for executing templates and verifying the result.
 *
 * @author Daan
 */
public abstract class AbstractTemplateExecutingTest {

  private Generator gen = new Generator();
  private JavaForgerConfiguration genConfig = JavaForgerConfiguration.builder().build();

  @Before
  public void setup() throws IOException {
    StaticJavaForgerConfiguration staticConfig = StaticJavaForgerConfiguration.getConfig();
    staticConfig.reset();
    Configuration freeMarkerConfig = FreeMarkerConfiguration.getDefaultConfig();
    freeMarkerConfig.setDirectoryForTemplateLoading(new File("src/test/resources/templates"));
    staticConfig.setFreeMarkerConfiguration(freeMarkerConfig);
  }

  @After
  public void tearDown() {
    StaticJavaForgerConfiguration.reset();
  }

  protected void executeAndVerify(String template, String inputClass, TemplateInputParameters map, String expected) throws IOException, TemplateException {
    CodeSnipit code = execute(template, inputClass, map);
    verifyEquals(expected, code.toString());
  }

  protected CodeSnipit execute(String template, String inputClass, TemplateInputParameters map) throws IOException, TemplateException {
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
  protected void verifyEquals(String expected, String actual) throws IOException {
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
