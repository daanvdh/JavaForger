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

import org.junit.Assert;
import org.junit.Test;

import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Modifier.Keyword;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.SimpleName;

/**
 * Unit test for {@link NodeEqualityChecker}.
 *
 * @author Daan
 */
public class NodeEqualityCheckerTest {

  private static final PackageDeclaration PACKAGE = new PackageDeclaration();
  private static final ImportDeclaration IMPORT = new ImportDeclaration("imp", false, false);
  private static final FieldDeclaration FIELD = new FieldDeclaration().addVariable(new VariableDeclarator().setName(new SimpleName("fieldName")));
  private static final ConstructorDeclaration CONSTRUCTOR = new ConstructorDeclaration();
  private static final MethodDeclaration METHOD = new MethodDeclaration().setName("method1");
  private static final MethodDeclaration METHOD2 = new MethodDeclaration().setName("method2");
  private static final ClassOrInterfaceDeclaration CLASS = new ClassOrInterfaceDeclaration().setName("class1");
  private static final ClassOrInterfaceDeclaration CLASS2 = new ClassOrInterfaceDeclaration().setName("class2");

  private NodeEqualityChecker comparator = new NodeEqualityChecker();

  @Test
  public void testCompare_sorting() {
    List<Node> nodes = Arrays.asList(METHOD, FIELD, IMPORT, CLASS, IMPORT, CONSTRUCTOR, PACKAGE);
    List<Node> expected = Arrays.asList(PACKAGE, IMPORT, IMPORT, FIELD, CONSTRUCTOR, METHOD, CLASS);
    // TODO move test to comparator
    // List<Node> sortedNodes = nodes.stream().sorted(comparator).collect(Collectors.toList());
    //
    // Assert.assertEquals(expected, sortedNodes);
  }

  @Test
  public void testCompare_modifiers() {
    FieldDeclaration f1 = createField("f1", Keyword.PUBLIC);
    FieldDeclaration f2 = createField("f2", null);
    FieldDeclaration f3 = createField("f3", Keyword.PROTECTED);
    FieldDeclaration f4 = createField("f4", Keyword.PRIVATE);

    List<Node> nodes = Arrays.asList(f4, f2, f1, f3);
    List<Node> expected = Arrays.asList(f1, f2, f3, f4);

    // TODO move test to comparator
    // List<Node> sortedNodes = nodes.stream().sorted(comparator).collect(Collectors.toList());
    //
    // Assert.assertEquals(expected, sortedNodes);
  }

  @Test
  public void testCompare_equal() {
    Assert.assertTrue(comparator.isEqual(PACKAGE, PACKAGE));
    Assert.assertEquals(-1, comparator.isEqual(IMPORT, IMPORT)); // TODO not supported yet to find equal imports
    Assert.assertTrue(comparator.isEqual(FIELD, FIELD));
    Assert.assertTrue(comparator.isEqual(CONSTRUCTOR, CONSTRUCTOR));
    Assert.assertTrue(comparator.isEqual(METHOD, METHOD));
    Assert.assertTrue(comparator.isEqual(CLASS, CLASS));
  }

  @Test
  public void testCompare_smaller() {
    Assert.assertEquals(-1, comparator.isEqual(PACKAGE, IMPORT));
    Assert.assertEquals(-1, comparator.isEqual(IMPORT, FIELD));
    Assert.assertEquals(-1, comparator.isEqual(FIELD, CONSTRUCTOR));
    Assert.assertEquals(-1, comparator.isEqual(CONSTRUCTOR, METHOD));
    Assert.assertEquals(-1, comparator.isEqual(METHOD, CLASS));
  }

  @Test
  public void testCompare_bigger() {
    Assert.assertEquals(1, comparator.isEqual(IMPORT, PACKAGE));
    Assert.assertEquals(1, comparator.isEqual(FIELD, IMPORT));
    Assert.assertEquals(1, comparator.isEqual(CONSTRUCTOR, FIELD));
    Assert.assertEquals(1, comparator.isEqual(METHOD, CONSTRUCTOR));
    Assert.assertEquals(1, comparator.isEqual(CLASS, METHOD));
  }

  @Test
  public void testCompare_sameClass() {
    Assert.assertEquals(-1, comparator.isEqual(METHOD, METHOD2));
    Assert.assertEquals(-1, comparator.isEqual(METHOD2, METHOD));
    Assert.assertEquals(-1, comparator.isEqual(CLASS, CLASS2));
    Assert.assertEquals(-1, comparator.isEqual(CLASS2, CLASS));
  }

  private FieldDeclaration createField(String name, Keyword modifier) {
    FieldDeclaration f = new FieldDeclaration().addVariable(new VariableDeclarator().setName(new SimpleName(name)));
    if (modifier != null) {
      f.setModifier(modifier, true);
    }
    return f;
  }

}
