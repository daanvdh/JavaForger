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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;

/**
 * This abstract class creates a new file before each test which the extending test class can change within the test, so that the original file is not changed.
 * The newly created file is removed after the test is run.
 *
 * @author Daan
 */
public class AbstractFileChangingTest {

  private static final String ORIGINAL_CLASS = "src/test/java/inputClassesForTests/CLassWithEverything.java";
  private static final String ORIGINAL_TEST_CLASS = "src/test/java/inputClassesForTests/CLassWithEverythingTest.java";
  protected static final String INPUT_CLASS = "src/test/resources/temporaryTestResults/ClassWithEverything.java";
  protected static final String INPUT_TEST_CLASS = "src/test/resources/temporaryTestResults/ClassWithEverythingTest.java";

  @Before
  public void setup() throws IOException {
    removeTestClassIfExists(INPUT_CLASS);
    removeTestClassIfExists(INPUT_TEST_CLASS);
    copyClass(ORIGINAL_CLASS, INPUT_CLASS);
    copyClass(ORIGINAL_TEST_CLASS, INPUT_TEST_CLASS);
  }

  @After
  public void tearDown() {
    removeTestClassIfExists(INPUT_CLASS);
    removeTestClassIfExists(INPUT_TEST_CLASS);
  }

  private void removeTestClassIfExists(String input) {
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

}
