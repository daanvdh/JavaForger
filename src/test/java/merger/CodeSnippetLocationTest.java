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

import org.junit.Assert;
import org.junit.Test;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;

import reader.Parser;

/**
 * Unit test for {@link CodeSnippetLocation}
 *
 * @author Daan
 */
public class CodeSnippetLocationTest {

  private Parser parser = new Parser();

  @Test
  public void testOf_NodeInput() {
    Node node = createNode();

    CodeSnippetLocation location = CodeSnippetLocation.of(node);

    Assert.assertEquals(CodeSnippetLocation.of(2, 3), location);
  }

  @Test
  public void testBefore_NodeInput() {
    Node node = createNode();

    CodeSnippetLocation location = CodeSnippetLocation.before(node);

    Assert.assertEquals(CodeSnippetLocation.of(2, 2), location);
  }

  @Test
  public void testOf_NodeInputWithJavaDoc() {
    String field = "/**\n Very nice javadoc.\n * With mulitple lines.\n */\nprivate String someField;\n";
    Node node = wrapInsideCompilationUnit(field);

    CodeSnippetLocation location = CodeSnippetLocation.of(node);

    Assert.assertEquals(CodeSnippetLocation.of(6, 11), location);
  }

  @Test
  public void testOf_NodeInputAnnotation() {
    String field = //
        "@Anno1\n" //
            + "@Anno2\n" //
            + "@Anno3\n" //
            + "private String someField;\n"; //
    Node node = wrapInsideCompilationUnit(field);

    CodeSnippetLocation location = CodeSnippetLocation.of(node);

    Assert.assertEquals(CodeSnippetLocation.of(6, 10), location);
  }

  @Test
  public void testOf_NodeInputWithJavaDocAndAnnotation() {
    String field = "/**\n Very nice javadoc.\n * With mulitple lines.\n */\n@Anno1\n@Anno2\nprivate String someField;\n";
    Node node = wrapInsideCompilationUnit(field);

    CodeSnippetLocation location = CodeSnippetLocation.of(node);

    Assert.assertEquals(CodeSnippetLocation.of(6, 13), location);
  }

  @Test
  public void testOf_NodeInputWithAnnotationAndJavaDoc() {
    String field = //
        "@Anno1\n@Anno2\n" //
            + "/**\n " //
            + "Very nice javadoc.\n " //
            + "* With mulitple lines.\n " //
            + "*/\n" //
            + "private String someField;\n"; //
    Node node = wrapInsideCompilationUnit(field);

    CodeSnippetLocation location = CodeSnippetLocation.of(node);

    Assert.assertEquals(CodeSnippetLocation.of(6, 13), location);
  }

  private Node createNode() {
    String code = "import my.impord;\nimport my.secondimpord;\n\npublic class ClassToMerge {\n\n}";
    CompilationUnit cu = parser.parse(code);
    Node node = cu.getChildNodes().get(1);
    return node;
  }

  private Node wrapInsideCompilationUnit(String field) {
    String code = "import my.impord;\nimport my.secondimpord;\n\npublic class ClassToMerge {\n\n" + field + "\n}";
    CompilationUnit cu = parser.parse(code);
    Node node = cu.getChildNodes().get(2).getChildNodes().get(2);
    return node;
  }

}
