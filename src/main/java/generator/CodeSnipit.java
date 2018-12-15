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
package generator;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Contains a list of Stings, each string represents a line of code.
 *
 * @author Daan
 */
public class CodeSnipit {

  private String code;

  public CodeSnipit(String codeString) {
    code = new String(codeString);
  }

  public String getCode() {
    return code;
  }

  public void add(String string) {
    code = code + string;
  }

  public void print() {
    System.out.println(toString());
  }

  public void printWithLineNumbers() {
    System.out.println(toStringWithLineNumbers());
  }

  /**
   * @return The code with line numbers.
   */
  private String toStringWithLineNumbers() {
    String[] split = code.split("\\r?\\n");
    List<String> codeList = Arrays.asList(split);
    IntStream.range(0, codeList.size()).mapToObj(i -> (Integer) i).forEach(i -> codeList.set(i, i + "\t" + codeList.get(i)));
    return codeList.stream().collect(Collectors.joining("\n"));
  }

  @Override
  public String toString() {
    return code;
  }

}
