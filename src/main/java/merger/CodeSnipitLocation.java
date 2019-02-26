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

import java.util.Optional;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.nodeTypes.NodeWithJavadoc;
import com.github.javaparser.javadoc.Javadoc;

/**
 * Defines the location of code with an inclusive start line number and an exclusive end line number.
 *
 * @author Daan
 */
public class CodeSnipitLocation {

  /** start of the code snipit (inclusive), first possible line number is '1' */
  private final int start;
  /** end of the code snipit (exclusive) */
  private final int end;

  public CodeSnipitLocation(int start, int end) {
    this.start = start;
    this.end = end;
  }

  public int getStart() {
    return start;
  }

  public int getEnd() {
    return end;
  }

  /**
   * @return the start line number minus one. Mend to be used in combination with arrays of strings.
   */
  public int getFirstIndex() {
    return start - 1;
  }

  /**
   * @return the end line number minus one. Mend to be used in combination with arrays of strings.
   */
  public int getLastIndex() {
    return end - 1;
  }

  public int size() {
    return end - start;
  }

  public static CodeSnipitLocation of(int start, int end) {
    return new CodeSnipitLocation(start, end);
  }

  public static CodeSnipitLocation of(int startEnd) {
    return of(startEnd, startEnd);
  }

  public static CodeSnipitLocation of(Node node) {
    int javaDocLines = countJavaDocLines(node);
    CodeSnipitLocation location = of(node.getBegin().get().line - javaDocLines, node.getEnd().get().line + 1);
    return location;
  }

  private static int countJavaDocLines(Node node) {
    int javaDocLines = 0;
    if (NodeWithJavadoc.class.isAssignableFrom(node.getClass())) {
      NodeWithJavadoc<?> javaDocNode = (NodeWithJavadoc<?>) node;
      Optional<Javadoc> javadoc = javaDocNode.getJavadoc();
      if (javadoc.isPresent()) {
        String text = javadoc.get().toText();
        String[] lines = text.split("\r\n|\r|\n");
        javaDocLines = lines.length + 2; // The plus 2 is for the begin and end lines containing /** and */.

        // TODO it is currently not supported to have only a single line of javadoc.
      }
    }
    return javaDocLines;
  }

  public static CodeSnipitLocation before(Node node) {
    return of(node.getBegin().get().line);
  }

  public static CodeSnipitLocation after(Node node) {
    return of(node.getEnd().get().line + 1);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + end;
    result = prime * result + start;
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    CodeSnipitLocation other = (CodeSnipitLocation) obj;
    if (end != other.end)
      return false;
    if (start != other.start)
      return false;
    return true;
  }

  @Override
  public String toString() {
    ToStringBuilder builder = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
    builder.append("start", start).append("end", end);
    return builder.toString();
  }

}
