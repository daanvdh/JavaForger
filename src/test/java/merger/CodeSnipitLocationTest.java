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
 * Unit test for {@link CodeSnipitLocation}
 *
 * @author Daan
 */
public class CodeSnipitLocationTest {

  private Parser parser = new Parser();

  @Test
  public void testOf_NodeInput() {
    Node node = createNode();

    CodeSnipitLocation location = CodeSnipitLocation.of(node);

    Assert.assertEquals(CodeSnipitLocation.of(2, 3), location);
  }

  @Test
  public void testOf_NodeInputWithJavaDoc() {

    Assert.fail("Currently javadoc is not taken into account for assigning the first line. ");

  }

  @Test
  public void testBefore_NodeInput() {
    Node node = createNode();

    CodeSnipitLocation location = CodeSnipitLocation.before(node);

    Assert.assertEquals(CodeSnipitLocation.of(2, 2), location);
  }

  private Node createNode() {
    String code = "import my.impord;\nimport my.secondimpord;\n\npublic class ClassToMerge {\n\n}";
    CompilationUnit cu = parser.parse(code);
    Node node = cu.getChildNodes().get(1);
    return node;
  }

}
