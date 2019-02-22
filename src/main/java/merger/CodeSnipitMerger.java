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

import java.io.File;
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

import configuration.JavaForgerConfiguration;
import configuration.PathConverter;
import generator.CodeSnipit;
import generator.JavaForgerException;
import reader.Parser;

/**
 * TODO javadoc
 *
 * @author Daan
 */
public abstract class CodeSnipitMerger {

  public void merge(JavaForgerConfiguration config, CodeSnipit codeSnipit, String mergeClassPath) throws IOException {
    if (validate(codeSnipit, mergeClassPath)) {
      executeMerge(config, codeSnipit, mergeClassPath);
      format(config, mergeClassPath);
    }
  }

  protected abstract void executeMerge(JavaForgerConfiguration config, CodeSnipit codeSnipit, String mergeClassPath) throws IOException;

  protected boolean validate(CodeSnipit codeSnipit, String mergeClassPath) {
    boolean success = true;
    if (mergeClassPath == null) {
      throw new JavaForgerException("merge class path may not be null");
    }
    if (mergeClassPath.isEmpty()) {
      throw new JavaForgerException("merge class path may not be empty");
    }
    if (!new File(mergeClassPath).exists()) {
      throw new JavaForgerException("merge class path does not point to existing file: " + mergeClassPath);
    }
    if (codeSnipit.getCode().isEmpty()) {
      System.err.println("CodeSnipit is empty and cannot be merged to: " + mergeClassPath);
      success = false;
    }
    return success;
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

  protected String toCompleteClass(CodeSnipit codeSnipit, String mergeClassPath) {
    String string = codeSnipit.toString();
    int index = firstIndexAfterImports(string);

    StringBuilder code = new StringBuilder();
    code.append(string.substring(0, index));
    boolean hasClassDefined = hasClassDefined(string.substring(index));
    if (!hasClassDefined) {
      code.append("\n\npublic class " + PathConverter.toClassName(mergeClassPath) + " {\n");
    }
    code.append(string.substring(index));
    if (!hasClassDefined) {
      code.append("\n}");
    }
    return code.toString();
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

  private boolean hasClassDefined(String string) {
    return parseDeclaration(string, ParseStart.CLASS_OR_INTERFACE_TYPE).isSuccessful();
  }

  private ParseResult<PackageDeclaration> parsePackage(String declaration) {
    return parseDeclaration(declaration, ParseStart.PACKAGE_DECLARATION);
  }

  private ParseResult<ImportDeclaration> parseImport(String declaration) {
    return parseDeclaration(declaration, ParseStart.IMPORT_DECLARATION);
  }

  private <N extends Node> ParseResult<N> parseDeclaration(String declaration, ParseStart<N> parseStart) {
    JavaParser parser = new JavaParser();
    Provider provider = Providers.provider(declaration);
    ParseResult<N> result = parser.parse(parseStart, provider);
    return result;
  }

  protected void format(JavaForgerConfiguration config, String mergeClassPath) {
    System.err.println("formatting is not yet supported and should be done manually for class " + mergeClassPath);
    // TODO Probably best to call the formatter via the cmd:
    // https://stackoverflow.com/questions/15464111/run-cmd-commands-through-java
    // https://www.beyondjava.net/run-eclipse-formatter-command-line

    // TODO Probably we have to let the user set the path to the desired java-formatter in the config
  }

}
