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
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.SimpleName;

/**
 * Unit test for {@link NodeComparator}.
 *
 * @author Daan
 */
public class NodeComparatorTest {

  private static final PackageDeclaration PACKAGE = new PackageDeclaration();
  private static final ImportDeclaration IMPORT = new ImportDeclaration("imp", false, false);
  private static final FieldDeclaration FIELD = new FieldDeclaration().addVariable(new VariableDeclarator().setName(new SimpleName("fieldName")));
  private static final ConstructorDeclaration CONSTRUCTOR = new ConstructorDeclaration();
  private static final MethodDeclaration METHOD = new MethodDeclaration();
  private static final ClassOrInterfaceDeclaration CLASS = new ClassOrInterfaceDeclaration().setName("class1");
  private static final ClassOrInterfaceDeclaration CLASS2 = new ClassOrInterfaceDeclaration().setName("class2");

  private NodeComparator comparator = new NodeComparator();

  @Test
  public void testCompare_sorting() {
    List<Node> nodes = Arrays.asList(METHOD, FIELD, IMPORT, CLASS, IMPORT, CONSTRUCTOR, PACKAGE);
    List<Node> expected = Arrays.asList(PACKAGE, IMPORT, IMPORT, FIELD, CONSTRUCTOR, METHOD, CLASS);

    List<Node> sortedNodes = nodes.stream().sorted(comparator).collect(Collectors.toList());

    Assert.assertEquals(expected, sortedNodes);
  }

  @Test
  public void testCompare_equal() {
    Assert.assertEquals(0, comparator.compare(PACKAGE, PACKAGE));
    Assert.assertEquals(-1, comparator.compare(IMPORT, IMPORT)); // TODO not supported yet to find equal imports
    Assert.assertEquals(0, comparator.compare(FIELD, FIELD));
    Assert.assertEquals(0, comparator.compare(CONSTRUCTOR, CONSTRUCTOR));
    Assert.assertEquals(0, comparator.compare(METHOD, METHOD));
    Assert.assertEquals(0, comparator.compare(CLASS, CLASS));
  }

  @Test
  public void testCompare_smaller() {
    Assert.assertEquals(-1, comparator.compare(PACKAGE, IMPORT));
    Assert.assertEquals(-1, comparator.compare(IMPORT, FIELD));
    Assert.assertEquals(-1, comparator.compare(FIELD, CONSTRUCTOR));
    Assert.assertEquals(-1, comparator.compare(CONSTRUCTOR, METHOD));
    Assert.assertEquals(-1, comparator.compare(METHOD, CLASS));
  }

  @Test
  public void testCompare_bigger() {
    Assert.assertEquals(1, comparator.compare(IMPORT, PACKAGE));
    Assert.assertEquals(1, comparator.compare(FIELD, IMPORT));
    Assert.assertEquals(1, comparator.compare(CONSTRUCTOR, FIELD));
    Assert.assertEquals(1, comparator.compare(METHOD, CONSTRUCTOR));
    Assert.assertEquals(1, comparator.compare(CLASS, METHOD));
  }

  @Test
  public void testCompare_sameClass() {
    Assert.assertEquals(-1, comparator.compare(CLASS, CLASS2));
    Assert.assertEquals(-1, comparator.compare(CLASS2, CLASS));
  }

}
