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
package reader;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;

import generator.CodeSnipit;

/**
 * Parser using {@link JavaParser} to parse classes. If an exception is thrown within JavaParser, this class will output the input String so that everything can
 * be debugged.
 *
 * @author Daan
 */
public class Parser {

  /**
   * Execute {@link JavaParser} and output the input code if an exception is thrown inside JavaParser so that it becomes debuggable.
   *
   * @param code The code to be parsed
   * @return The {@link CompilationUnit} result after parsing
   */
  public static CompilationUnit parse(String code) {
    CompilationUnit cu;
    try {
      cu = JavaParser.parse(code);
    } catch (Exception e) {
      e.printStackTrace();
      System.out.println("The following could not be parsed:");
      new CodeSnipit(code).printWithLineNumbers();
      throw e;
    }
    return cu;
  }

  public static CompilationUnit parse(FileInputStream in) throws IOException {
    return parse(getFileContent(in));
  }

  private static String getFileContent(FileInputStream fis) throws IOException {
    try (BufferedReader br = new BufferedReader(new InputStreamReader(fis, "UTF-8"))) {
      StringBuilder sb = new StringBuilder();
      String line;
      while ((line = br.readLine()) != null) {
        sb.append(line);
        sb.append('\n');
      }
      return sb.toString();
    }
  }

}
