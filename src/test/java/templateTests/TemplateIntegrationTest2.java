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
package templateTests;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import common.AbstractFileChangingTest;
import configuration.DefaultConfigurations;
import configuration.JavaForgerConfiguration;
import generator.JavaForger;

/**
 * Integration test testing the whole flow of inserting code into a class. If this test fails, comment out deleting the created file in the tearDown method.
 * Then go to the TEST_CLASS path to check what the actual result is of executing the failing test. Possibly copy the result to the ORIGINAL_CLASS to check for
 * compilation errors.
 * </p>
 * This class is a bitch to maintain, with small changes in the application every verify file has to be updated. We made maintenance easier with the VERY
 * DANGEROUS main method that will make the whole test green again by replacing ALL verify files. USE WITH CARE AND TRIPLE CHECK GIT BEFORE COMMITTING.
 *
 * @author Daan
 */
public class TemplateIntegrationTest2 extends AbstractFileChangingTest {

  private static final String EXPECTED_RESULTS_PATH = "src/test/resources/templateTestOutcomes/";

  @Test
  public void testInnerBuilder() throws IOException {
    String expectedClass = "verify-innerBuilder.java";
    String expectedTestClass = "verify-innerBuilderTest.java";
    executeAndVerify(DefaultConfigurations.forBuilderAndTest(), expectedClass, expectedTestClass);
  }

  @Test
  public void testExtendableInnerBuilder() throws IOException {
    String expectedClass = "verify-extendableInnerBuilder.java";
    String expectedTestClass = "verify-innerBuilderTest.java";
    executeAndVerify(DefaultConfigurations.forExtendableBuilderAndTest(), expectedClass, expectedTestClass);
  }

  @Test
  public void testEquals() throws IOException {
    String expectedClass = "verify-equals.java";
    String expectedTestClass = "verify-equalsTest.java";
    executeAndVerify(DefaultConfigurations.forEqualsAndTest(), expectedClass, expectedTestClass);
  }

  @Test
  public void testHashCode() throws IOException {
    String expectedClass = "verify-hashCode.java";
    String expectedTestClass = "verify-hashCodeTest.java";
    executeAndVerify(DefaultConfigurations.forHashCodeAndTest(), expectedClass, expectedTestClass);
  }

  @Test
  public void testToString() throws IOException {
    String expectedClass = "verify-toString.java";
    executeAndVerify(DefaultConfigurations.forToString(), expectedClass);
  }

  private void executeAndVerify(JavaForgerConfiguration config, String expectedClass, String expectedTestClass) throws IOException {
    execute(config);
    verifyFileEqual(EXPECTED_RESULTS_PATH + expectedClass, INPUT_CLASS);
    verifyFileEqual(EXPECTED_RESULTS_PATH + expectedTestClass, INPUT_TEST_CLASS);
  }

  private void executeAndVerify(JavaForgerConfiguration config, String expectedClass) throws IOException {
    execute(config);
    verifyFileEqual(EXPECTED_RESULTS_PATH + expectedClass, INPUT_CLASS);
  }

  private void execute(JavaForgerConfiguration config) {
    JavaForger.execute(config, INPUT_CLASS);
  }

  /**
   * VERY DANGEROUS MAIN METHOD, ONLY USE THIS WHEN YOU KNOW WHAT YOU'RE DOING.
   * </p>
   * This main method will force the whole test to be green again, by replacing all verify files with the actual result.
   *
   * @param args
   */
  public static void main(String[] args) {

    // This statement prevents the main method from accidently being executed.
    // Assert.fail();

    TemplateIntegrationTest test = new TemplateIntegrationTest() {
      @Override
      protected void verifyFileEqual(String expectedPath, String actualPath) throws IOException {
        System.out.println("Copying:\n" + actualPath + " to:\n" + expectedPath);
        super.copyClass(actualPath, expectedPath);
      }
    };

    try {
      test.setup();
      test.testEquals();
      test.setup();
      test.testInnerBuilder();
      test.setup();
      test.testHashCode();
      test.setup();
      test.testInnerBuilder();
      test.setup();
      test.testExtendableInnerBuilder();
      test.setup();
      test.testToString();
      test.tearDown();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    Assert.fail();
  }

}
