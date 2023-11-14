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
import org.apache.commons.lang3.tuple.Pair;

import com.github.javaparser.JavaParser;
import com.github.javaparser.Position;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.nodeTypes.NodeWithJavadoc;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.javadoc.Javadoc;

import generator.CodeSnippet;

/**
 * Defines the location of a code block with an inclusive start line number and an exclusive end line number.
 *
 * @author Daan
 */
public class CodeSnippetLocation implements Comparable<CodeSnippetLocation> {

  // TODO rename to startLine
  /** start of the {@link CodeSnippetLocation} (inclusive), first possible line number is '1' */
  private final int start;
  // TODO rename to endLine
  /** end of the {@link CodeSnippetLocation} (exclusive) */
  private final int end;

  /** The start column of the {@link CodeSnippetLocation} (inclusive) */
  private final int startCharacter;
  /** The end column of the {@link CodeSnippetLocation} (exclusive) */
  private final int endCharacter;

  /** Mainly for debugging purposes, to easily see what this {@link CodeSnippetLocation} represents. */
  private final Node node;

  /**
   * @param start {@link CodeSnippetLocation#start}
   * @param end {@link CodeSnippetLocation#end}
   */
  public CodeSnippetLocation(int start, int startCharacter, int end, int endCharacter, Node node) {
    if (start > end || (start == end && startCharacter > endCharacter)) {
      throw new RuntimeException(
          "Start (" + start + ", " + startCharacter + ") cannot be bigger than end (" + end + ", " + endCharacter + ") for node " + node);
    }
    this.start = start;
    this.end = end;
    this.startCharacter = startCharacter;
    this.endCharacter = endCharacter;
    this.node = node;
  }

  /**
   * @see CodeSnippetLocation#start
   */
  public int getStart() {
    return start;
  }

  public int getStartCharacter() {
    return startCharacter;
  }

  /**
   * @see CodeSnippetLocation#end
   */
  public int getEnd() {
    return end;
  }

  public int getEndCharacter() {
    return endCharacter;
  }

  /**
   * @return The number of lines represented by this {@link CodeSnippet}.
   */
  public int getNumberOfLines() {
    return end - start;
  }

  public int getNumberOfColumns() {
    return this.endCharacter - startCharacter;
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
   * @return The total number of lines represented by this {@link CodeSnippetLocation}.
   */
  public int size() {
    return end - start;
  }

  public static CodeSnippetLocation of(int start, int startCharacter, int end, int endCharacter) {
    return of(start, startCharacter, end, endCharacter, null);
  }

  /**
   * Creates a new {@link CodeSnippetLocation}.
   *
   * @param start The {@link CodeSnippetLocation#start}
   * @param end The {@link CodeSnippetLocation#end}
   * @return new {@link CodeSnippetLocation}
   */
  public static CodeSnippetLocation of(int start, int startCharacter, int end, int endCharacter, Node node) {
    return new CodeSnippetLocation(start, startCharacter, end, endCharacter, node);
  }

  public static CodeSnippetLocation of(Pair<Integer, Integer> start, Pair<Integer, Integer> end) {
    return of(start, end, null);
  }

  public static CodeSnippetLocation of(Pair<Integer, Integer> start, Pair<Integer, Integer> end, Node node) {
    return of(start.getLeft(), start.getRight(), end.getLeft(), end.getRight(), node);
  }

  public static CodeSnippetLocation of(Pair<Integer, Integer> startEnd) {
    return of(startEnd.getLeft(), startEnd.getRight(), startEnd.getLeft(), startEnd.getRight());
  }

  /**
   * Creates a new {@link CodeSnippetLocation} for the given {@link Node}.
   *
   * @param node The {@link JavaParser} {@link Node} used to determine the beginning and end of the node. Javadoc is not taken into account.
   * @return new {@link CodeSnippetLocation}.
   */
  public static CodeSnippetLocation of(Node node) {
    return of(calculateStart(node), calculateEnd(node), node);
  }

  /**
   * Creates a new {@link CodeSnippetLocation} representing the line directly above the given {@link JavaParser} {@link Node}.
   *
   * @param node the {@link JavaParser} {@link Node}.
   * @return a new {@link CodeSnippetLocation}.
   */
  public static CodeSnippetLocation before(Node node) {
    Position position = node.getBegin().get();
    return of(position.line, position.column, position.line, position.column, node);
  }

  /**
   * Creates a new {@link CodeSnippetLocation} representing the line directly below the given {@link JavaParser} {@link Node}.
   *
   * @param node the {@link JavaParser} {@link Node}.
   * @return a new {@link CodeSnippetLocation}.
   */
  public static CodeSnippetLocation after(Node node) {
    return of(calculateEnd(node));
  }

  /**
   * Creates a new {@link CodeSnippetLocation} for the given {@link Node}s.
   *
   * @param inclusiveStart The beginning of this {@link JavaParser} {@link Node} will be the start of the resulting {@link CodeSnippetLocation}.
   * @param exclusiveEnd The beginning of this {@link JavaParser} {@link Node} will be the end of the resulting {@link CodeSnippetLocation}.
   * @return new {@link CodeSnippetLocation}.
   */
  public static CodeSnippetLocation fromUntil(Node inclusiveStart, Node exclusiveEnd) {
    return of(calculateStart(inclusiveStart), calculateStart(exclusiveEnd));
  }

  /**
   * Creates a new {@link CodeSnippetLocation} for the given {@link Node}s.
   *
   * @param exclusiveStart The end of this {@link JavaParser} {@link Node} will be the start of the resulting {@link CodeSnippetLocation}.
   * @param inclusiveEnd The end of this {@link JavaParser} {@link Node} will be the end of the resulting {@link CodeSnippetLocation}.
   * @return new {@link CodeSnippetLocation}.
   */
  public static CodeSnippetLocation fromAfterUntilIncluding(Node exclusiveStart, BlockStmt inclusiveEnd) {
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
    CodeSnippetLocation other = (CodeSnippetLocation) obj;
    if (end != other.end)
      return false;
    if (start != other.start)
      return false;
    return true;
  }

  @Override
  public String toString() {
    ToStringBuilder builder = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
    builder.append("start", "(" + start + ", " + startCharacter + ")").append("end", "(" + end + ", " + endCharacter + ")").append("node", node);
    return builder.toString();
  }

  @Override
  public int compareTo(CodeSnippetLocation that) {
    return this.start - that.start;
  }

  private static Pair<Integer, Integer> calculateEnd(Node node) {
    // TODO Should not do +1 anymore since we are working with columns now. Maybe +1 on the column?
    return Pair.of(node.getEnd().get().line + 1, node.getEnd().get().column);
  }

  private static Pair<Integer, Integer> calculateStart(Node node) {
    int javaDocLines = countJavaDocLines(node);
    int calculatedStart = node.getBegin().get().line - javaDocLines;
    // TODO if javadoc lines are not empty, the column might not be correct.
    // But maybe IF it has javadoc lines the column is always zero and it makes no difference.
    return Pair.of(calculatedStart, node.getBegin().get().column);
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

  public Node getNode() {
    return node;
  }

  public boolean isEmpty() {
    return this.getNumberOfLines() == 0 && getNumberOfColumns() == 0;
  }

  public boolean isNotEmpty() {
    return !this.isEmpty();
  }

  public boolean isBefore(CodeSnippetLocation value) {
    return this.compareTo(value) < 0;
  }
}
