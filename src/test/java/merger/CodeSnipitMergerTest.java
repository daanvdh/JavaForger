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

import org.junit.After;
import org.junit.Test;

import common.AbstractFileChangingTest;
import generator.CodeSnipit;
import generator.JavaForgerConfiguration;

/**
 * Unit test for {@link CodeSnipitMerger}.
 *
 * @author Daan
 */
public class CodeSnipitMergerTest extends AbstractFileChangingTest {

  /** Path to the file which will be written to create an expected file */
  private static final String EXPECTED_CLASS = "src/test/resources/temporaryTestResults/ExpectedClass.java";
  private static final JavaForgerConfiguration CONFIG = JavaForgerConfiguration.builder().build();;

  private static final String METHOD1 = "  public void method1() {\r\n" + "    method2(i, s);\r\n" + "  }\r\n" + "\r\n";

  @Override
  @After
  public void tearDown() {
    super.tearDown();
    removeTestClassIfExists(EXPECTED_CLASS);
  }

  @Test
  public void testMerge_newPublicMethod() throws IOException {
    String newCode = "public void newMethod() {\n// Does this method exist?\n}";
    String expected = genExpected(newCode, METHOD1, false);

    executeAndVerify(expected, newCode);
  }

  @Test
  public void testMerge_newMethodSameNameDifferentSignature() {

  }

  @Test
  public void testMerge_existingMethod() {

  }

  @Test
  public void testMerge_newVariable() {

  }

  @Test
  public void testMerge_existingVariable() {

  }

  @Test
  public void testMerge_newClass() {

  }

  @Test
  public void testMerge_existingClass() {

  }

  @Test
  public void testMerge_failingJavaParserPrinter() {
    // TODO anonymously overwrite CodeSnipitPrinter::write(CompilationUnit existingCode, PrintWriter writer) to throw an exception and delete everything in the
    // existing file. Then check if the file is still the same afterwards.
  }

  private void executeAndVerify(String expected, String merge) throws IOException {
    executeAndVerify(CONFIG, expected, merge);
  }

  private void executeAndVerify(JavaForgerConfiguration conf, String expected, String merge) throws IOException {
    new CodeSnipitMerger().merge(conf, new CodeSnipit(merge), INPUT_CLASS);

    super.stringToFile(EXPECTED_CLASS, expected);
    verifyFileEqual(EXPECTED_CLASS, INPUT_CLASS);
  }

  private String genExpected(String newCode, String atLocation, boolean replace) throws IOException {
    String wholeFile = fileToString(INPUT_CLASS);
    String verySpecialLineEndingsToMakeItAllWork = "\r\n\r\n";
    String replaceWith = replace ? newCode + verySpecialLineEndingsToMakeItAllWork : atLocation + newCode + verySpecialLineEndingsToMakeItAllWork;
    String newFile = wholeFile.replace(atLocation, replaceWith);
    return newFile;
  }

}
