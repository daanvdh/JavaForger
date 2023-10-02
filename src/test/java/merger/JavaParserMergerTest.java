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

import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;

import org.junit.After;
import org.junit.Test;

import common.AbstractFileChangingTest;
import configuration.JavaForgerConfiguration;
import generator.CodeSnipit;

/**
 * Unit test for {@link JavaParserMerger}.
 *
 * @author Daan
 */
public class JavaParserMergerTest extends AbstractFileChangingTest {

  /** Path to the file which will be written to create an expected file */
  private static final String EXPECTED_CLASS = "src/test/resources/temporaryTestResults/ExpectedClass.java";
  private static final JavaForgerConfiguration CONFIG = JavaForgerConfiguration.builder().build();

  @Override
  @After
  public void tearDown() {
    super.tearDown();
    removeTestClassIfExists(EXPECTED_CLASS);
  }

  @Test
  public void testMerge_newPublicMethod() throws IOException {
    String newCode = "public void newMethod() {\n// Does this method exist?\n}";
    String expected = genExpected(newCode, 48, 48);

    executeAndVerify(expected, newCode);
  }

  private void executeAndVerify(String expected, String merge) throws IOException {
    executeAndVerify(CONFIG, expected, merge);
  }

  private void executeAndVerify(JavaForgerConfiguration conf, String expected, String merge) throws IOException {
    new JavaParserMerger().merge(conf, new CodeSnipit(merge), INPUT_CLASS, null, null);

    super.stringToFile(EXPECTED_CLASS, expected);

    verifyFileEqual(EXPECTED_CLASS, INPUT_CLASS);
  }

  private String genExpected(String newCode, int startRemoveBlock, int endRemoveBlock) throws IOException {
    try (LineNumberReader reader = new LineNumberReader(new FileReader(INPUT_CLASS))) {
      StringBuilder sb = new StringBuilder();

      while (reader.getLineNumber() < startRemoveBlock) {
        String readLine = reader.readLine();
        sb.append(readLine + "\r\n");
      }

      reader.setLineNumber(endRemoveBlock);
      sb.append(newCode + "\r\n\r\n");

      String readLine = reader.readLine();
      while (readLine != null) {
        sb.append(readLine + "\r\n");
        readLine = reader.readLine();
      }

      reader.close();

      return sb.toString();
    }
  }

}
