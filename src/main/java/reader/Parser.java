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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;

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
      System.out.println(printWithLineNumbers(code));
      throw e;
    }
    return cu;
  }

  public static CompilationUnit parse(FileInputStream in) throws IOException {
    return parse(getFileContent(in));
  }

  /**
   * Print the input string with a line number before every new line.
   *
   * @param code The string to add line numbers to.
   * @return The string with line numbers.
   */
  public static String printWithLineNumbers(String code) {
    String[] split = code.split("\\r?\\n");
    List<String> codeList = Arrays.asList(split);
    IntStream.range(0, codeList.size()).mapToObj(i -> (Integer) i).forEach(i -> codeList.set(i, i + "\t" + codeList.get(i)));
    return codeList.stream().collect(Collectors.joining("\n"));
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
