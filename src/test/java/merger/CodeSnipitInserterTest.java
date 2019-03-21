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
package merger;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Test;

import common.AbstractFileChangingTest;
import configuration.JavaForgerConfiguration;

/**
 * Unit test for {@link CodeSnipitInserter}.
 *
 * @author Daan
 */
public class CodeSnipitInserterTest extends AbstractFileChangingTest {

  /** Path to the file which will be written to create an expected file */
  private static final String EXPECTED_RESULTS_PATH = "src/test/resources/templateTestOutcomes/CodeSnipitInserterTest/";

  private CodeSnipitInserter inserter = new CodeSnipitInserter();

  @Test
  public void testInsert_addPackageNoOverride() throws IOException {
    executeAndVerify("verify-nothingHappend.java", false, CodeSnipitLocation.of(18, 19));
  }

  @Test
  public void testInsert_addPackageOverride() throws IOException {
    executeAndVerify("verify-overriden.java", true, CodeSnipitLocation.of(18, 19));
  }

  @Test
  public void testInsert_addImportNoOverride() throws IOException {
    executeAndVerify("verify-inserted.java", false, CodeSnipitLocation.of(19));
  }

  @Test
  public void testInsert_addImportOverride() throws IOException {
    executeAndVerify("verify-inserted.java", true, CodeSnipitLocation.of(19));
  }

  private void executeAndVerify(String expectedClass, boolean override, CodeSnipitLocation... insertLocation) throws IOException {
    LinkedHashMap<CodeSnipitLocation, CodeSnipitLocation> newCodeInsertionLocations = new LinkedHashMap<>();

    IntStream.range(0, insertLocation.length).forEach(i -> newCodeInsertionLocations.put(CodeSnipitLocation.of(1 + i, 2 + i), insertLocation[i]));

    executeAndVerify(expectedClass, override, newCodeInsertionLocations);
  }

  private void executeAndVerify(String expectedClass, boolean override, LinkedHashMap<CodeSnipitLocation, CodeSnipitLocation> newCodeInsertionLocations)
      throws IOException {
    inserter.insert(JavaForgerConfiguration.builder().withOverride(override).build(), INPUT_CLASS, "this is the new Code 1\nthis is the new Code 2\n",
        newCodeInsertionLocations);
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

    CodeSnipitInserterTest test = new CodeSnipitInserterTest() {
      @Override
      protected void verifyFileEqual(String expectedPath, String actualPath) throws IOException {
        System.out.println("Copying:\n" + actualPath + " to:\n" + expectedPath);
        super.copyClass(actualPath, expectedPath);
      }
    };

    test.setup();
    test.testInsert_addPackageNoOverride();
    test.setup();
    test.testInsert_addPackageOverride();
    test.setup();
    test.testInsert_addImportNoOverride();
    test.setup();
    test.testInsert_addImportOverride();
    test.tearDown();

    Assert.fail();
  }

}
