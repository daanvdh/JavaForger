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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import configuration.JavaForgerConfiguration;

/**
 * Class for insterting code into an existing class based on earlier determined insertion locations.
 *
 * @author Daan
 */
public class CodeSnipitInserter {

  /**
   * Inserts the new code into the file given by the mergeClassPath according to the insertLocations.
   *
   * @param config {@link JavaForgerConfiguration} indicating insert settings, such as if it is allowed to override code or only to insert.
   * @param mergeClassPath The path to the class to merge the new code into.
   * @param newCode The code to insert.
   * @param newCodeInsertionLocations Defines where new code needs to be inserted in the existing code. This map should be ordered on increasing insertLocation.
   * @throws IOException If path to existing class is invalid.
   */
  public void insert(JavaForgerConfiguration config, String mergeClassPath, String newCode,
      LinkedHashMap<CodeSnipitLocation, CodeSnipitLocation> newCodeInsertionLocations) throws IOException {
    List<String> existingLines = Files.readAllLines(Paths.get(mergeClassPath), StandardCharsets.UTF_8);
    List<String> newlines = Arrays.asList(newCode.split("\\r?\\n"));
    List<String> result = insert(config, existingLines, newlines, newCodeInsertionLocations);
    Files.write(Paths.get(mergeClassPath), result, StandardCharsets.UTF_8);
  }

  private List<String> insert(JavaForgerConfiguration config, List<String> existingLines, List<String> newlines,
      LinkedHashMap<CodeSnipitLocation, CodeSnipitLocation> newCodeInsertionLocations) {
    // Because newCodeInsertionLocations is ordered, we keep track of the already added lines to determine the new insertion location
    int addedLines = 0;

    for (Map.Entry<CodeSnipitLocation, CodeSnipitLocation> locations : newCodeInsertionLocations.entrySet()) {
      CodeSnipitLocation codeLocation = locations.getKey();
      CodeSnipitLocation insertLocation = locations.getValue();

      if (config.isOverride() || !insertLocation.containsLines()) {
        // Remove old lines
        for (int i = insertLocation.getFirstIndex(); i < insertLocation.getLastIndex(); i++) {
          existingLines.remove(addedLines + insertLocation.getFirstIndex());
        }
        // write new lines
        for (int i = 0; i < codeLocation.size(); i++) {
          existingLines.add(addedLines + insertLocation.getFirstIndex() + i, newlines.get(codeLocation.getFirstIndex() + i));
        }
        addedLines += codeLocation.size() - insertLocation.size();
      }
    }
    return existingLines;
  }

}
