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
package common;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

/**
 * This abstract class creates a new file before each test which the extending test class can change within the test, so that the original file is not changed.
 * The newly created file is removed after the test is run.
 *
 * @author Daan
 */
public abstract class AbstractFileChangingTest {

  private static final String ORIGINAL_CLASS = "src/test/java/inputClassesForTests/CLassWithEverything.java";
  private static final String ORIGINAL_TEST_CLASS = "src/test/java/inputClassesForTests/CLassWithEverythingTest.java";
  protected static final String INPUT_CLASS = "src/test/resources/temporaryTestResults/InputClass.java";
  protected static final String INPUT_TEST_CLASS = "src/test/resources/temporaryTestResults/InputClassTest.java";

  @Before
  public void setup() throws IOException {
    tearDown();
    copyClass(getOriginalClass(), INPUT_CLASS);
    copyClass(getOriginalTestClass(), INPUT_TEST_CLASS);
  }

  @After
  public void tearDown() {
    removeTestClassIfExists(INPUT_CLASS);
    removeTestClassIfExists(INPUT_TEST_CLASS);
  }

  protected void removeTestClassIfExists(String input) {
    File f = new File(input);
    if (f.exists() && !f.isDirectory()) {
      f.delete();
    }
  }

  protected void copyClass(String input, String copyLocation) throws IOException, FileNotFoundException {
    File src = new File(input);
    File dest = new File(copyLocation);
    FileUtils.copyFile(src, dest);
  }

  protected String fileToString(String path) throws IOException {
    byte[] encoded = Files.readAllBytes(Paths.get(path));
    return new String(encoded, StandardCharsets.UTF_8);
  }

  protected void stringToFile(String path, String content) throws IOException {
    try (PrintWriter writer = new PrintWriter(path, "UTF-8")) {
      writer.write(content);
      writer.close();
    }
  }

  protected void verifyFileEqual(String expectedPath, String actualPath) throws IOException {
    verifyFileEquals(new File(expectedPath), new File(actualPath));
  }

  protected void verifyFileEquals(File expectedFile, File actualFile) throws IOException, FileNotFoundException {
    boolean contentEquals = FileUtils.contentEquals(actualFile, expectedFile);
    if (contentEquals == false) {
      System.err.println("Actual file " + actualFile.getAbsolutePath() + ":");
      printFile(actualFile);
      System.err.println("Expected file " + expectedFile.getAbsolutePath() + ":");
      printFile(expectedFile);
    }
    Assert.assertTrue("Expected file (" + expectedFile.getAbsolutePath() + ") was not equal to actual (" + actualFile.getAbsolutePath() + ")", contentEquals);
  }

  protected void printFile(File file) throws FileNotFoundException {
    try (Scanner input = new Scanner(file)) {
      while (input.hasNextLine()) {
        System.out.println(input.nextLine());
      }
    }
  }

  protected String getOriginalClass() {
    return ORIGINAL_CLASS;
  }

  protected String getOriginalTestClass() {
    return ORIGINAL_TEST_CLASS;
  }

  /**
   * Method for precisely indicating which char in the two strings is the first that is not equal. Created because of the endless debugging for the different
   * line-endings.
   */
  protected void verifyEquals(String expect, String result) {
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

}
