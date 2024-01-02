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
import com.github.javaparser.javadoc.Javadoc;

import generator.CodeSnippet;

/**
 * Defines the location of a code block with an inclusive start line number and an exclusive end line number.
 *
 * @author Daan
 */
public class CodeSnippetLocation implements Comparable<CodeSnippetLocation> {

  /** start of the {@link CodeSnippetLocation} (inclusive), first possible line number is '1' */
  private final int startLine;
  /** The start column of the {@link CodeSnippetLocation} (inclusive) */
  private final int startCharacter;
  /** end of the {@link CodeSnippetLocation} (inclusive) */
  private final int endLine;
  /** The end column of the {@link CodeSnippetLocation} (exclusive) */
  private final int endCharacter;

  /** Mainly for debugging purposes, to easily see what this {@link CodeSnippetLocation} represents. */
  private final Node node;

  /**
   * @param start {@link CodeSnippetLocation#startLine}
   * @param end {@link CodeSnippetLocation#endLine}
   */
  public CodeSnippetLocation(int start, int startCharacter, int end, int endCharacter, Node node) {
    if (start > end || (start == end && startCharacter > endCharacter)) {
      throw new RuntimeException(
          "Start (" + start + ", " + startCharacter + ") cannot be bigger than end (" + end + ", " + endCharacter + ") for node " + node);
    }
    this.startLine = start;
    this.endLine = end;
    this.startCharacter = startCharacter;
    this.endCharacter = endCharacter;
    this.node = node;
  }

  /**
   * @see CodeSnippetLocation#startLine
   */
  public int getStartLine() {
    return startLine;
  }

  public int getStartCharacter() {
    return startCharacter;
  }

  /**
   * @see CodeSnippetLocation#endLine
   */
  public int getEndLine() {
    return endLine;
  }

  public int getEndCharacter() {
    return endCharacter;
  }

  /**
   * @return The number of lines represented by this {@link CodeSnippet}.
   */
  public int getNumberOfLines() {
    // We need to do plus one because the endLine is inclusive, meaning endLine = 1 and startLine = 1 represents line 1.
    // Only if the characters are equal this whole CodeSnippetLocation is empty.
    return endLine - startLine + 1;
  }

  public int getNumberOfColumns() {
    return this.endCharacter - startCharacter;
  }

  /**
   * @return the start line number minus one. Mend to be used in combination with arrays of strings.
   */
  public int getStartLineIndex() {
    return startLine - 1;
  }

  /**
   * @return the end line number minus one. Mend to be used in combination with arrays of strings.
   */
  public int getEndLineIndex() {
    return endLine - 1;
  }

  /**
   * @return the end start character minus one. Mend to be used in combination with indexes of strings.
   */
  public int getStartCharacterIndex() {
    return this.startCharacter - 1;
  }

  /**
   * @return the end start character minus one. Mend to be used in combination with indexes of strings.
   */
  public int getEndCharacterIndex() {
    return this.endCharacter - 1;
  }

  public static CodeSnippetLocation of(int start, int startCharacter, int end, int endCharacter) {
    return of(start, startCharacter, end, endCharacter, null);
  }

  /**
   * Creates a new {@link CodeSnippetLocation}.
   *
   * @param start The {@link CodeSnippetLocation#startLine}
   * @param end The {@link CodeSnippetLocation#endLine}
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
  public static CodeSnippetLocation fromAfterUntilIncluding(Node exclusiveStart, Node inclusiveEnd) {
    return of(calculateEnd(exclusiveStart), calculateEnd(inclusiveEnd));
  }

  /**
   * Creates a new {@link CodeSnippetLocation} for the given {@link Node}s.
   *
   * @param inclusiveStart The beginning of this {@link JavaParser} {@link Node} will be the start of the resulting {@link CodeSnippetLocation}.
   * @param inclusiveEnd The end of this {@link JavaParser} {@link Node} will be the end of the resulting {@link CodeSnippetLocation}.
   * @return new {@link CodeSnippetLocation}.
   */
  public static CodeSnippetLocation fromUntilIncluding(Node inclusiveStart, Node inclusiveEnd) {
    return of(calculateStart(inclusiveStart), calculateEnd(inclusiveEnd), inclusiveStart);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + endLine;
    result = prime * result + startLine;
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
    if (endLine != other.endLine)
      return false;
    if (startLine != other.startLine)
      return false;
    if (startCharacter != other.startCharacter)
      return false;
    if (endCharacter != other.endCharacter)
      return false;
    return true;
  }

  @Override
  public String toString() {
    ToStringBuilder builder = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
    builder.append("start", "(" + startLine + ", " + startCharacter + ")").append("end", "(" + endLine + ", " + endCharacter + ")").append("node", node);
    return builder.toString();
  }

  @Override
  public int compareTo(CodeSnippetLocation that) {
    int compare = this.startLine - that.startLine;
    if (compare == 0) {
      compare = this.startCharacter - that.startCharacter;
    }
    return compare;
  }

  private static Pair<Integer, Integer> calculateEnd(Node node) {
    // We need to do +1 on the column, because it is exclusive. Otherwise we cannot represent an empty CodeSnippetLocation
    return Pair.of(node.getEnd().get().line, node.getEnd().get().column + 1);
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
    return endLine == startLine && getNumberOfColumns() == 0;
  }

  public boolean isNotEmpty() {
    return !this.isEmpty();
  }

  public boolean isBefore(CodeSnippetLocation value) {
    return this.compareTo(value) < 0;
  }

  public static CodeSnippetLocation empty() {
    return of(1, 1, 1, 1);
  }

  public boolean isOnSingleLine() {
    return this.startLine == this.endLine;
  }

  public int getEndCharacterIndexInclusive() {
    return getEndCharacterIndex() - 1;
  }

  public boolean includes(CodeSnippetLocation that) {
    boolean includes = true;
    if (this.startLine > that.startLine) {
      includes = false;
    } else if (this.startLine == that.startLine && this.startCharacter > that.startCharacter) {
      includes = false;
    }
    if (this.endLine < that.endLine) {
      includes = false;
    } else if (this.endLine == that.endLine && this.endCharacter < that.endCharacter) {
      includes = false;
    }
    return includes;
  }

  public static CodeSnippetLocation after(CodeSnippetLocation l) {
    return of(l.endLine, l.endCharacter + 1, l.endLine, l.endCharacter + 1);
  }

}
