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

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.nodeTypes.NodeWithJavadoc;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.javadoc.Javadoc;

import generator.CodeSnipit;

/**
 * Defines the location of a code block with an inclusive start line number and an exclusive end line number.
 *
 * @author Daan
 */
public class CodeSnipitLocation implements Comparable<CodeSnipitLocation> {

  /** start of the {@link CodeSnipitLocation} (inclusive), first possible line number is '1' */
  private final int start;
  /** end of the {@link CodeSnipitLocation} (exclusive) */
  private final int end;

  /**
   * @param start {@link CodeSnipitLocation#start}
   * @param end {@link CodeSnipitLocation#end}
   */
  public CodeSnipitLocation(int start, int end) {
    this.start = start;
    this.end = end;
  }

  /**
   * @see CodeSnipitLocation#start
   */
  public int getStart() {
    return start;
  }

  /**
   * @see CodeSnipitLocation#end
   */
  public int getEnd() {
    return end;
  }

  /**
   * @return The number of lines represented by this {@link CodeSnipit}.
   */
  public int getNumberOfLines() {
    return end - start;
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

  /**
   * @return {@code true} if the end line is higher than the start line, {@code false} otherwise.
   */
  public boolean containsLines() {
    return end > start;
  }

  /**
   * @return The total number of lines represented by this {@link CodeSnipitLocation}.
   */
  public int size() {
    return end - start;
  }

  /**
   * Creates a new {@link CodeSnipitLocation}.
   *
   * @param start The {@link CodeSnipitLocation#start}
   * @param end The {@link CodeSnipitLocation#end}
   * @return new {@link CodeSnipitLocation}
   */
  public static CodeSnipitLocation of(int start, int end) {
    return new CodeSnipitLocation(start, end);
  }

  /**
   * Creates a new {@link CodeSnipitLocation} of a single line.
   *
   * @param startEnd The {@link CodeSnipitLocation#start} and {@link CodeSnipitLocation#end}
   * @return new {@link CodeSnipitLocation}
   */
  public static CodeSnipitLocation of(int startEnd) {
    return of(startEnd, startEnd);
  }

  /**
   * Creates a new {@link CodeSnipitLocation} for the given {@link Node}.
   *
   * @param node The {@link JavaParser} {@link Node} used to determine the beginning and end of the node. Javadoc is not taken into account.
   * @return new {@link CodeSnipitLocation}.
   */
  public static CodeSnipitLocation of(Node node) {
    return of(calculateStart(node), calculateEnd(node));
  }

  /**
   * Creates a new {@link CodeSnipitLocation} representing the line directly above the given {@link JavaParser} {@link Node}.
   *
   * @param node the {@link JavaParser} {@link Node}.
   * @return a new {@link CodeSnipitLocation}.
   */
  public static CodeSnipitLocation before(Node node) {
    return of(node.getBegin().get().line);
  }

  /**
   * Creates a new {@link CodeSnipitLocation} representing the line directly below the given {@link JavaParser} {@link Node}.
   *
   * @param node the {@link JavaParser} {@link Node}.
   * @return a new {@link CodeSnipitLocation}.
   */
  public static CodeSnipitLocation after(Node node) {
    return of(calculateEnd(node));
  }

  /**
   * Creates a new {@link CodeSnipitLocation} for the given {@link Node}s.
   *
   * @param inclusiveStart The beginning of this {@link JavaParser} {@link Node} will be the start of the resulting {@link CodeSnipitLocation}.
   * @param exclusiveEnd The beginning of this {@link JavaParser} {@link Node} will be the end of the resulting {@link CodeSnipitLocation}.
   * @return new {@link CodeSnipitLocation}.
   */
  public static CodeSnipitLocation fromUntil(Node inclusiveStart, Node exclusiveEnd) {
    return of(calculateStart(inclusiveStart), calculateStart(exclusiveEnd));
  }

  /**
   * Creates a new {@link CodeSnipitLocation} for the given {@link Node}s.
   *
   * @param exclusiveStart The end of this {@link JavaParser} {@link Node} will be the start of the resulting {@link CodeSnipitLocation}.
   * @param inclusiveEnd The end of this {@link JavaParser} {@link Node} will be the end of the resulting {@link CodeSnipitLocation}.
   * @return new {@link CodeSnipitLocation}.
   */
  public static CodeSnipitLocation fromAfterUntilIncluding(Node exclusiveStart, BlockStmt inclusiveEnd) {
    return of(calculateEnd(exclusiveStart), calculateEnd(inclusiveEnd));
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

  @Override
  public int compareTo(CodeSnipitLocation that) {
    return this.start - that.start;
  }

  private static int calculateEnd(Node node) {
    return node.getEnd().get().line + 1;
  }

  private static int calculateStart(Node node) {
    int javaDocLines = countJavaDocLines(node);
    int calculatedStart = node.getBegin().get().line - javaDocLines;
    return calculatedStart;
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

}
