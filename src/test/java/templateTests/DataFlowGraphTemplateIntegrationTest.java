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

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import common.AbstractFileChangingTest;
import configuration.DefaultConfigurations;
import configuration.JavaForgerConfiguration;
import dataflow.DataFlowGraph;
import generator.JavaForger;

/**
 * Integration test for templates that require a {@link DataFlowGraph} to be constructed, such as stateFullClassTest.javat
 *
 * @author Daan
 */
public class DataFlowGraphTemplateIntegrationTest extends AbstractFileChangingTest {

  private static final String EXPECTED_RESULTS_PATH = "src/test/resources/stateFullClassTest/";

  @Test
  public void testStateFullClassTest_setter() throws IOException {
    String claz = //
        "public class Claz {\n" + //
            "  private String s;\n" + //
            "  public void setS(String a) {\n" + //
            "    this.s = a;\n" + //
            "  }\n" + //
            "}"; //

    String expectedClass = "verify-stateFullClassTest.java";
    executeAndVerify(DefaultConfigurations.forStateFullClassTest(), claz, expectedClass);
  }

  @Test
  public void testStatelessClassTest_facade() throws IOException {
    String claz = //
        "public class Claz {\n" + //
            "  StringBuilder sb = new StringBuilder(); \n" + //
            "  public StringBuilder setS(String a) {\n" + //
            "    return sb.append(a);\n" + //
            "  }\n" + //
            "}"; //

    String expectedClass = "verify-statelessClassTest-facade.java";
    executeAndVerify(DefaultConfigurations.forStatelessClassTest(), claz, expectedClass);
  }

  private void executeAndVerify(JavaForgerConfiguration config, String claz, String expectedClass) throws IOException {
    stringToFile(INPUT_CLASS, claz);
    JavaForger.execute(config, INPUT_CLASS);
    verifyFileEqual(EXPECTED_RESULTS_PATH + expectedClass, INPUT_TEST_CLASS);
  }

  /**
   * VERY DANGEROUS MAIN METHOD, ONLY USE THIS WHEN YOU KNOW WHAT YOU'RE DOING.
   * </p>
   * This main method will force the whole test to be green again, by replacing all verify files with the actual result.
   *
   * @param args
   * @throws IOException
   */
  public static void main(String[] args) throws IOException {

    // This statement prevents the main method from accidently being executed.
    Assert.fail();

    DataFlowGraphTemplateIntegrationTest test = new DataFlowGraphTemplateIntegrationTest() {
      @Override
      protected void verifyFileEqual(String expectedPath, String actualPath) throws IOException {
        System.out.println("Copying:\n" + actualPath + " to:\n" + expectedPath);
        super.copyClass(actualPath, expectedPath);
      }
    };

    test.setup();
    test.testStateFullClassTest_setter();
    test.tearDown();

    Assert.fail();
  }
}
