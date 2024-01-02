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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import configuration.JavaForgerConfiguration;

/**
 * Class for inserting code into an existing class based on earlier determined insertion locations. This class is not responsible for any of the content of the
 * file, except for the formatting. The input {@link CodeSnippetLocation}s determine what should be located where. Any formatting belongs to the
 * {@link CodeSnippetLocation} after it. This includes spaces, tabs and newLines.
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
  public void insert(JavaForgerConfiguration config, String mergeClassPath, String newCode, InsertionMap newCodeInsertionLocations) throws IOException {
    List<String> existingLines = Files.readAllLines(Paths.get(mergeClassPath), StandardCharsets.UTF_8);
    List<String> newlines = Arrays.asList(newCode.split("\\r?\\n"));
    List<String> result = insert(config, existingLines, newlines, newCodeInsertionLocations);
    Files.write(Paths.get(mergeClassPath), result, StandardCharsets.UTF_8);
  }

  private List<String> insert(JavaForgerConfiguration config, List<String> existingLines, List<String> newlines, InsertionMap newCodeInsertionLocations) {
    // Because newCodeInsertionLocations is ordered, we keep track of the already added lines to determine the new insertion location
    int totalAddedLines = 0;
    for (InsertionEntry locations : newCodeInsertionLocations.entries()) {
      if (config.isOverride() || locations.getTo().isEmpty()) {
        int addedLines = insertEntry(existingLines, newlines, totalAddedLines, locations);
        totalAddedLines += addedLines;
      }
    }
    return existingLines;
  }

  private int insertEntry(List<String> existingLines, List<String> newlines, int addedLines, InsertionEntry insertionEntry) {
    CodeSnippetLocation codeLocation = insertionEntry.getKey();
    CodeSnippetLocation insertLocation = insertionEntry.getValue();

    String insertPrefix = createPrefix(existingLines, insertLocation, addedLines);
    String insertPostfix = createPostFix(existingLines, insertLocation, addedLines);

    List<String> linesToAdd = collectLinesToAdd(newlines, codeLocation, insertPrefix, insertPostfix);

    int firstLineToChange = calculateFirstLineToChange(existingLines, insertLocation, insertPrefix, addedLines);
    int nofRemovedLines = removeExistingLines(existingLines, insertLocation, firstLineToChange);
    int nofAddedLines = addLines(existingLines, firstLineToChange, linesToAdd);

    return nofAddedLines - nofRemovedLines;
  }

  private List<String> collectLinesToAdd(List<String> newlines, CodeSnippetLocation codeLocation, String insertPrefix, String insertPostfix) {

    String preceedingBlankLine = null;
    if (codeLocation.getStartLineIndex() > 0) {
      String possiblyBlancSpacingLine = newlines.get(codeLocation.getStartLineIndex() - 1);
      if (possiblyBlancSpacingLine.isBlank()) {
        preceedingBlankLine = possiblyBlancSpacingLine;
      }
    }

    String firstInsertLine = createFirstLine(newlines, codeLocation, insertPrefix, insertPostfix);
    String lastInsertLine = createLastLine(newlines, codeLocation, insertPostfix);

    // First create a list of all lines to be added:
    List<String> linesToAdd = new ArrayList<>();
    if (preceedingBlankLine != null) {
      linesToAdd.add(preceedingBlankLine);
    }
    linesToAdd.add(firstInsertLine);
    // Add the lines in between
    for (int i = codeLocation.getStartLineIndex() + 1; i <= codeLocation.getEndLineIndex() - 1; i++) {
      linesToAdd.add(newlines.get(i));
    }
    if (lastInsertLine != null) {
      linesToAdd.add(lastInsertLine);
    }
    return linesToAdd;
  }

  private int addLines(List<String> existingLines, int firstLineToChange, List<String> linesToAdd) {
    int totalAddedLines = 0;
    // Now actually add the lines
    for (int i = 0; i < linesToAdd.size(); i++) {
      String line = linesToAdd.get(i);
      existingLines.add(firstLineToChange + i, line);
      totalAddedLines++;
    }
    return totalAddedLines;
  }

  private String createLastLine(List<String> newlines, CodeSnippetLocation codeLocation, String insertPostfix) {
    // Create and add the last line including the postFix
    String lastInsertLine;
    if (codeLocation.isOnSingleLine()) {
      // The postfix was already handled in the firstLine.
      lastInsertLine = null;
    } else {
      String lastLine = newlines.get(codeLocation.getEndLineIndex());
      lastInsertLine = lastLine.substring(0, codeLocation.getEndCharacterIndex());
      if (insertPostfix != null) {
        lastInsertLine = lastInsertLine + insertPostfix;
      }
    }
    return lastInsertLine;
  }

  private String createFirstLine(List<String> newlines, CodeSnippetLocation codeLocation, String insertPrefix, String insertPostfix) {
    // Create and add the first line including the prefix
    String newLine = newlines.get(codeLocation.getStartLineIndex());
    String possiblyBlancPrefix = newLine.substring(0, codeLocation.getStartCharacterIndex());
    String firstInsertLine = newLine.substring(codeLocation.getStartCharacterIndex());
    if (insertPrefix != null) {
      firstInsertLine = insertPrefix + firstInsertLine;
    } else {
      // Add formatting as created if no prefix will be added.
      if (possiblyBlancPrefix.isBlank()) {
        firstInsertLine = possiblyBlancPrefix + firstInsertLine;
      }
    }
    if (codeLocation.isOnSingleLine() && insertPostfix != null) {
      firstInsertLine = firstInsertLine + insertPostfix;
    }
    return firstInsertLine;
  }

  private int removeExistingLines(List<String> existingLines, CodeSnippetLocation insertLocation, int firstLineToChange) {
    int totalRemovedLines = 0;
    // FIXME I think this is not taking into account already added lines for the limit of the loop
    for (int i = firstLineToChange; i <= insertLocation.getEndLineIndex(); i++) {
      existingLines.remove(firstLineToChange);
      totalRemovedLines++;
    }
    return totalRemovedLines;
  }

  /**
   * Calculates the line before which the new code needs to be inserted. This method takes into account any formatting in the existing lines.
   * 
   * @param existingLines
   * @param insertLocation
   * @param insertPrefix
   * @param addedLines
   * @return
   */
  private int calculateFirstLineToChange(List<String> existingLines, CodeSnippetLocation insertLocation, String insertPrefix, int addedLines) {
    int firstLineToChange = insertLocation.getStartLineIndex() + addedLines;
    if (insertPrefix == null) {
      // If the prefix is empty, we will write a complete new line. This is true because the prefix will only be empty if the insert location is after the end
      // of the existing line.
      firstLineToChange++;
    }

    if (firstLineToChange > 0) {
      String preceedingLine = existingLines.get(firstLineToChange - 1);
      if (preceedingLine.isBlank()) {
        // All formatting is considered to belong to the code before it. Therefore if the preceding line is blank, we will start inserting code before it.
        // We currently assume only a single formatting line should ever be present.
        firstLineToChange--;
      }
    }
    return firstLineToChange;
  }

  private String createPostFix(List<String> existingLines, CodeSnippetLocation insertLocation, int addedLines) {
    // Get the existing postFix
    String endLine = existingLines.get(insertLocation.getEndLineIndex() + addedLines);
    String insertPostfix;
    if (endLine.length() <= insertLocation.getEndCharacterIndex()) {
      insertPostfix = null;
    } else {
      insertPostfix = endLine.substring(insertLocation.getEndCharacterIndex());
    }
    return insertPostfix;
  }

  /**
   * Gets the existing code on the line that is being changed, before the location from which new code will be inserted.
   * 
   * @param existingLines
   * @param insertLocation
   * @param addedLines
   * @return
   */
  private String createPrefix(List<String> existingLines, CodeSnippetLocation insertLocation, int addedLines) {
    // Get the existing prefix
    String startLine = existingLines.get(insertLocation.getStartLineIndex() + addedLines);
    String insertPrefix;
    if (startLine.length() <= insertLocation.getStartCharacterIndex()) {
      insertPrefix = null;
    } else {
      insertPrefix = startLine.substring(0, insertLocation.getStartCharacterIndex());
    }
    return insertPrefix;
  }

}
