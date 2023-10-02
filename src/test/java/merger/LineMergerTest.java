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
package merger;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import common.AbstractFileChangingTest;
import configuration.JavaForgerConfiguration;
import generator.CodeSnipit;

/**
 * Unit test for {@link JavaParserMerger}.
 *
 * @author Daan
 */
public class LineMergerTest extends AbstractFileChangingTest {

  /** Path to the file which will be written to create an expected file */
  private static final String EXPECTED_RESULTS_PATH = "src/test/resources/templateTestOutcomes/LineMergerTest/";

  private LineMerger merger = new LineMerger();

  @Test
  public void testAddInnerClass() throws IOException {
    String code = "public class ClassWithEverything {\n class AddedInnerClass {\n protected newMethod() { \n // Do Nothing \n }\n }\n }\n";
    String expectedClass = "verify-addInnerClass.java";
    executeAndVerify(expectedClass, code);
  }

  @Test
  public void testMergeInnerClass() throws IOException {
    String code = "public class ClassWithEverything {\n class InnerClass {\n protected void methodBetween3and4() { \n // Do Nothing \n }\n }\n }\n";
    String expectedClass = "verify-mergeInnerClass.java";
    executeAndVerify(expectedClass, code);
  }

  @Test
  public void testReplaceConstructor() throws IOException {
    String code = "private ClassWithEverything(int i) {\n this.i = i;\n}\n";
    String expectedClass = "verify-constructor.java";
    executeAndVerify(expectedClass, code);
  }

  @Test
  public void testField() throws IOException {
    String code = "protected String prettyString = \"very pretty\";";
    String expectedClass = "verify-field.java";
    executeAndVerify(expectedClass, code);
  }

  @Test
  public void testImport() throws IOException {
    String code = "import my.impord;";
    String expectedClass = "verify-import.java";
    executeAndVerify(expectedClass, code);
  }

  @Test
  public void testPackage() throws IOException {
    String code = "package my.packingedinges;";
    String expectedClass = "verify-package.java";
    executeAndVerify(expectedClass, code);
  }

  private void executeAndVerify(String expectedClass, String code) throws IOException {
    merger.merge(JavaForgerConfiguration.builder().override(true).build(), new CodeSnipit(code), INPUT_CLASS, null, null);
    verifyFileEqual(EXPECTED_RESULTS_PATH + expectedClass, INPUT_CLASS);
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

    LineMergerTest test = new LineMergerTest() {
      @Override
      protected void verifyFileEqual(String expectedPath, String actualPath) throws IOException {
        System.out.println("Copying:\n" + actualPath + " to:\n" + expectedPath);
        super.copyClass(actualPath, expectedPath);
      }
    };

    test.setup();
    test.testPackage();
    test.setup();
    test.testImport();
    test.setup();
    test.testField();
    test.setup();
    test.testReplaceConstructor();
    test.setup();
    test.testAddInnerClass();
    test.setup();
    test.testMergeInnerClass();
    test.tearDown();

    Assert.fail();
  }

}
