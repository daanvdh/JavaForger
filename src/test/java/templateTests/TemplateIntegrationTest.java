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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import freemarker.template.TemplateException;
import generator.DefaultConfigurations;
import generator.JavaForger;
import generator.JavaForgerConfiguration;

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
public class TemplateIntegrationTest {

  private static final String ORIGINAL_CLASS = "src/test/java/inputClassesForTests/CLassWithEverything.java";
  private static final String ORIGINAL_TEST_CLASS = "src/test/java/inputClassesForTests/CLassWithEverythingTest.java";
  private static final String COPY_CLASS = "src/test/resources/temporaryTestResults/ClassWithEverything.java";
  private static final String COPY_TEST_CLASS = "src/test/resources/temporaryTestResults/ClassWithEverythingTest.java";
  private static final String EXPECTED_RESULTS_PATH = "src/test/resources/templateTestOutcomes/";

  @Before
  public void setup() throws IOException {
    removeTestClassIfExists(COPY_CLASS);
    removeTestClassIfExists(COPY_TEST_CLASS);
    copyClass(ORIGINAL_CLASS, COPY_CLASS);
    copyClass(ORIGINAL_TEST_CLASS, COPY_TEST_CLASS);
  }

  @After
  public void tearDown() {
    removeTestClassIfExists(COPY_CLASS);
    removeTestClassIfExists(COPY_TEST_CLASS);
  }

  @Test
  public void testInnerBuilder() throws IOException {
    String expectedClass = "verify-innerBuilder.java";
    String expectedTestClass = "verify-innerBuilderTest.java";
    executeAndVerify(DefaultConfigurations.forBuilderAndTest(), expectedClass, expectedTestClass);
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
    verifyFileEqual(expectedClass, COPY_CLASS);
    verifyFileEqual(expectedTestClass, COPY_TEST_CLASS);
  }

  private void copyClass(String input, String copyLocation) throws IOException, FileNotFoundException {
    File src = new File(input);
    File dest = new File(copyLocation);
    FileUtils.copyFile(src, dest);
  }

  private void removeTestClassIfExists(String input) {
    File f = new File(input);
    if (f.exists() && !f.isDirectory()) {
      f.delete();
    }
  }

  private void executeAndVerify(JavaForgerConfiguration config, String expectedClass) throws IOException {
    execute(config);
    verifyFileEqual(expectedClass, COPY_CLASS);
  }

  private void execute(JavaForgerConfiguration config) {
    JavaForger.execute(config, COPY_CLASS);
  }

  // Protected so that we can override it, to make tests green instead of verifying anything.
  protected void verifyFileEqual(String expectedPath, String actualPath) throws IOException {
    File file1 = new File(actualPath);
    String expPath = EXPECTED_RESULTS_PATH + expectedPath;
    File file2 = new File(expPath);
    boolean contentEquals = FileUtils.contentEquals(file1, file2);
    if (contentEquals == false) {
      System.err.println("Actual file " + actualPath + ":");
      printFile(file1);
      System.err.println("Expected file " + expPath + ":");
      printFile(file2);
    }
    Assert.assertTrue("Expected file (" + expPath + ") was not equal to actual (" + actualPath + ")", contentEquals);
  }

  private void printFile(File file) throws FileNotFoundException {
    try (Scanner input = new Scanner(file)) {
      while (input.hasNextLine()) {
        System.out.println(input.nextLine());
      }
    }
  }

  /**
   * VERY DANGEROUS MAIN METHOD, ONLY USE THIS WHEN YOU KNOW WHAT YOU'RE DOING.
   * </p>
   * This main method will force the whole test to be green again, by replacing all verify files with the actual result.
   *
   * @param args
   * @throws IOException
   * @throws TemplateException
   */
  public static void main(String[] args) throws IOException {

    // This statement prevents the main method from accidently being executed.
    Assert.fail();

    TemplateIntegrationTest test = new TemplateIntegrationTest() {
      @Override
      protected void verifyFileEqual(String expectedPath, String actualPath) throws IOException {
        System.out.println("hi");
        super.copyClass(actualPath, EXPECTED_RESULTS_PATH + expectedPath);
      }
    };

    test.setup();
    test.testEquals();
    test.setup();
    test.testHashCode();
    test.setup();
    test.testInnerBuilder();
    test.setup();
    test.testToString();
    test.tearDown();
  }

}
