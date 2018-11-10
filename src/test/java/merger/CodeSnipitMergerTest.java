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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import generator.CodeSnipit;
import generator.GeneratorConfiguration;

/**
 * Unit test for {@link CodeSnipitMerger}.
 *
 * @author Daan
 */
public class CodeSnipitMergerTest {

  private static final String INPUT_CLASS = "src/test/java/inputClassesForTests/ClassWithEverything.java";
  private static String SAVED_INPUT = null;
  private static final GeneratorConfiguration CONFIG = GeneratorConfiguration.builder().withMergeClass(INPUT_CLASS).build();

  private static final String METHOD1 = "  public void method1() {\r\n" + "    method2(i, s);\r\n" + "  }\r\n" + "\r\n";

  @Before
  public void setup() throws IOException {
    if (SAVED_INPUT == null) {
      SAVED_INPUT = readFile(INPUT_CLASS);
    }
    if (SAVED_INPUT == null) {
      SAVED_INPUT = readFile(INPUT_CLASS);
    }
  }

  @After
  public void after() throws FileNotFoundException, UnsupportedEncodingException {
    try (PrintWriter writer = new PrintWriter(INPUT_CLASS, "UTF-8")) {
      writer.append(SAVED_INPUT);
      writer.close();
    }
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
  public void testMerge_inputClassNull() throws IOException {
    GeneratorConfiguration conf = GeneratorConfiguration.builder().build();

    executeAndVerify(conf, SAVED_INPUT, "something that should not be parsed");
  }

  @Test
  public void testMerge_failingJavaParserPrinter() {
    // TODO anonymously overwrite CodeSnipitPrinter::write(CompilationUnit existingCode, PrintWriter writer) to throw an exception and delete everything in the
    // existing file. Then check if the file is still the same afterwards.
  }

  private void executeAndVerify(String expected, String merge) throws IOException {
    executeAndVerify(CONFIG, expected, merge);
  }

  private void executeAndVerify(GeneratorConfiguration conf, String expected, String merge) throws IOException {
    new CodeSnipitMerger().merge(conf, new CodeSnipit(merge));
    String result = readFile(INPUT_CLASS);

    String expect = expected;

    verifyEquals(expect, result);
    Assert.assertEquals(expect, result);
  }

  /**
   * Method for precisely indicating which char in the two strings is the first that is not equal. Created because of the endless debugging for the different
   * line-endings.
   */
  private void verifyEquals(String expect, String result) {
    char[] e = expect.toCharArray();
    char[] r = result.toCharArray();
    byte[] eb = expect.getBytes();
    byte[] rb = result.getBytes();

    int i = 0;
    int ej = 1;
    int ei = 1;
    int rj = 1;
    int ri = 1;

    if (e.length != r.length) {
      System.out.println("expected as length " + e.length + " but actual as length " + r.length);
    }

    for (; i < e.length && i < r.length; i++) {
      if (!(e[i] == r[i]) && !((e[i] == '\r' || e[i] == '\n') && (r[i] == '\r' || r[i] == '\n'))) {
        System.out.println("first occurance of not equal string at index " + i + ":");
        System.out.println("at line " + ej + " and column  " + ei + " expect char: " + e[i]);
        System.out.println("at line " + rj + " and column  " + ri + " actual char: " + r[i]);
        System.out.println("at line " + ej + " and column  " + ei + " expect byte: " + eb[i]);
        System.out.println("at line " + rj + " and column  " + ri + " actual byte: " + rb[i]);
        System.out.println("actual: ");
        break;
      }
      ej = e[i] == '\n' || e[i] == '\r' ? ej + 1 : ej;
      rj = r[i] == '\n' || r[i] == '\r' ? rj + 1 : rj;
      ei = e[i] == '\n' || e[i] == '\r' ? 1 : ei + 1;
      ri = r[i] == '\n' || r[i] == '\r' ? 1 : ri + 1;
    }
  }

  private String genExpected(String newCode, String atLocation, boolean replace) {
    String wholeFile = new String(SAVED_INPUT);
    String verySpecialLineEndingsToMakeItAllWork = "\r\n\r\n";
    String replaceWith = replace ? newCode + verySpecialLineEndingsToMakeItAllWork : atLocation + newCode + verySpecialLineEndingsToMakeItAllWork;
    String newFile = wholeFile.replace(atLocation, replaceWith);
    return newFile;
  }

  private String readFile(String path) throws IOException {
    byte[] encoded = Files.readAllBytes(Paths.get(path));
    return new String(encoded, StandardCharsets.UTF_8);
  }

}
