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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParseStart;
import com.github.javaparser.Provider;
import com.github.javaparser.Providers;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;

import configuration.PathConverter;
import generator.CodeSnipit;
import reader.Parser;

/**
 * TODO javadoc
 *
 * @author Daan
 */
// TODO this should never extend CodeSnipitMerger, all code this class uses from the merger should be moved here.
public class CodeSnipitReader {

  public CompilationUnit read(CodeSnipit codeSnipit, String mergeClassPath) {
    CompilationUnit n;
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

  public String toCompleteClass(CodeSnipit codeSnipit, String mergeClassPath) {
    String string = codeSnipit.toString();
    int index = firstIndexAfterImports(string);

    StringBuilder code = new StringBuilder();
    code.append(string.substring(0, index));
    boolean hasClassDefined = hasClassDefined(string.substring(index));
    if (!hasClassDefined) {

      // TODO this was the old code, check if we really don't need this anymore
      // code.append("\n\npublic class " + PathConverter.toClassName(mergeClassPath) + " {\n");

      // Don't add any lines otherwise the CodeSnipitInserter cannot know the line number anymore
      code.append("public class " + PathConverter.toClassName(mergeClassPath) + " {");
    }
    code.append(string.substring(index));
    if (!hasClassDefined) {
      code.append("\n}");
    }
    return code.toString();
  }

  /**
   * @param codeSnipit The code to analyze
   * @return True if it has a class defined or fields, constructors or methods that should have been in the class.
   */
  private boolean hasClassCodeDefined(CodeSnipit codeSnipit) {
    String string = codeSnipit.toString();
    int index = firstIndexAfterImports(string);
    String codeAfterImports = string.substring(index);
    return hasClassDefined(codeAfterImports) || codeAfterImports.contains(";");
  }

  protected CompilationUnit readClass(String completeClass) {
    // TODO make this flexible so that we only add the class if needed

    CompilationUnit cu = Parser.parse(completeClass);
    // Needed to preserve the original formatting
    LexicalPreservingPrinter.setup(cu);
    return cu;
  }

  protected CompilationUnit read(String className) throws IOException {
    CompilationUnit cu = null;
    try (FileInputStream in = new FileInputStream(className)) {
      cu = JavaParser.parse(in);
      in.close();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    // Needed to preserve the original formatting
    LexicalPreservingPrinter.setup(cu);
    return cu;
  }

  // TODO probably remove this thing
  protected boolean hasClassDefined(CodeSnipit codeSnipit) {
    String string = codeSnipit.toString();
    int index = firstIndexAfterImports(string);
    boolean hasClassDefined = hasClassDefined(string.substring(index));
    return hasClassDefined;
  }

  protected int firstIndexAfterImports(String string) {
    int lineBegin = getFirstIndexAfterComment(string);
    int lineEnd = lineBegin + string.substring(lineBegin).indexOf(";");
    lineEnd = lineEnd < 0 ? string.length() : (lineEnd + 1);
    String declaration = string.substring(lineBegin, lineEnd);

    ParseResult<?> result = parsePackage(declaration);
    if (!result.isSuccessful()) {
      result = parseImport(declaration);
    }

    while (result.isSuccessful()) {
      if (lineEnd == string.length()) {
        lineBegin = lineEnd;
        break;
      }
      lineBegin = lineEnd + 1;
      lineEnd = lineBegin + string.substring(lineBegin).indexOf(";");
      lineEnd = lineEnd < 0 ? string.length() : (lineEnd + 1);
      declaration = string.substring(lineBegin, lineEnd);
      result = parseImport(declaration);

    }
    return lineBegin;
  }

  protected int getFirstIndexAfterComment(String string) {
    int index = 0;
    if (string.startsWith("/*")) {
      index = string.indexOf("*/") + 3;
    }
    return index;
  }

  protected ParseResult<PackageDeclaration> parsePackage(String declaration) {
    return parseDeclaration(declaration, ParseStart.PACKAGE_DECLARATION);
  }

  protected ParseResult<ImportDeclaration> parseImport(String declaration) {
    return parseDeclaration(declaration, ParseStart.IMPORT_DECLARATION);
  }

  protected boolean hasClassDefined(String string) {
    return parseDeclaration(string, ParseStart.COMPILATION_UNIT).isSuccessful();
  }

  private <N extends Node> ParseResult<N> parseDeclaration(String declaration, ParseStart<N> parseStart) {
    JavaParser parser = new JavaParser();
    Provider provider = Providers.provider(declaration);
    ParseResult<N> result = parser.parse(parseStart, provider);
    return result;
  }

}