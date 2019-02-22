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

import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.PackageDeclaration;

import configuration.JavaForgerConfiguration;
import generator.CodeSnipit;

/**
 * Class to merge new code into an existing class, on line per line basis.
 *
 * @author Daan
 */
public class LineMerger extends CodeSnipitMerger {

  private CodeSnipitLocater locater = new CodeSnipitLocater();
  private CodeSnipitInserter inserter = new CodeSnipitInserter();

  @Override
  protected void executeMerge(JavaForgerConfiguration config, CodeSnipit codeSnipit, String mergeClassPath) throws IOException {
    CompilationUnit existingCode = read(mergeClassPath);
    String completeClass = toCompleteClass(codeSnipit, mergeClassPath);
    CompilationUnit newCode = readClass(completeClass);

    Node newCode2 = toNode(codeSnipit, mergeClassPath);

    LinkedHashMap<CodeSnipitLocation, CodeSnipitLocation> newCodeInsertionLocations = locater.locate(existingCode, newCode2);
    inserter.insert(mergeClassPath, completeClass, newCodeInsertionLocations);
  }

  private Node toNode(CodeSnipit codeSnipit, String mergeClassPath) {
    Node n;
    if (hasClassCodeDefined(codeSnipit)) {
      String completeClass = toCompleteClass(codeSnipit, mergeClassPath);
      n = readClass(completeClass);
    } else {
      // TODO handle it if there is javadoc defined above the package
      CompilationUnit cu = new CompilationUnit();

      String string = codeSnipit.toString();

      int lineBegin = getFirstIndexAfterComment(string);
      int lineEnd = lineBegin + string.substring(lineBegin).indexOf(";");
      lineEnd = lineEnd < 0 ? string.length() : (lineEnd + 1);
      String declaration = string.substring(lineBegin, lineEnd);

      boolean endOfFile = false;

      // Add package if present
      ParseResult<PackageDeclaration> pack = parsePackage(declaration);
      if (pack.isSuccessful()) {
        cu.setPackageDeclaration(pack.getResult().get());

        endOfFile = lineEnd == string.length();

        if (!endOfFile) {
          lineBegin = lineEnd + 1;
          lineEnd = lineBegin + string.substring(lineBegin).indexOf(";");
          lineEnd = lineEnd < 0 ? string.length() : (lineEnd + 1);
          declaration = string.substring(lineBegin, lineEnd);
        }
      }

      // Add imports
      declaration = string.substring(lineBegin, lineEnd);
      ParseResult<ImportDeclaration> result = parseImport(declaration);
      while (result.isSuccessful() && !endOfFile) {
        cu.addImport(result.getResult().get());

        endOfFile = lineEnd == string.length();
        if (!endOfFile) {
          lineBegin = lineEnd + 1;
          lineEnd = lineBegin + string.substring(lineBegin).indexOf(";");
          lineEnd = lineEnd < 0 ? string.length() : (lineEnd + 1);
          declaration = string.substring(lineBegin, lineEnd);
          result = parseImport(declaration);
        }
      }

      n = cu;
    }
    return n;
  }

  /**
   * @param codeSnipit The code to analyze
   * @return True if it has a class defined or fields, constructors or methods that should have been in the class.
   */
  private boolean hasClassCodeDefined(CodeSnipit codeSnipit) {
    // TODO Auto-generated method stub
    return false;
  }

}
