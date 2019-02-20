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

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;

/**
 * Unit test for {@link NodeComparator}.
 *
 * @author Daan
 */
public class NodeComparatorTest {

  private static final PackageDeclaration PACKAGE = new PackageDeclaration();
  private static final ImportDeclaration IMPORT = new ImportDeclaration("imp", false, false);
  private static final FieldDeclaration FIELD = new FieldDeclaration();
  private static final ConstructorDeclaration CONSTRUCTOR = new ConstructorDeclaration();
  private static final MethodDeclaration METHOD = new MethodDeclaration();
  private static final ClassOrInterfaceDeclaration CLASS = new ClassOrInterfaceDeclaration();

  private NodeComparator comparator = new NodeComparator();

  @Test
  public void testCompare() {
    List<Node> nodes = Arrays.asList(METHOD, FIELD, IMPORT, CLASS, IMPORT, CONSTRUCTOR, PACKAGE);
    List<Node> expected = Arrays.asList(PACKAGE, IMPORT, IMPORT, FIELD, CONSTRUCTOR, METHOD, CLASS);

    executeAndVerify(nodes, expected);
  }

  private void executeAndVerify(List<Node> nodes, List<Node> expected) {
    List<Node> sortedNodes = nodes.stream().sorted(comparator).collect(Collectors.toList());

    Assert.assertEquals(expected, sortedNodes);
  }

}
