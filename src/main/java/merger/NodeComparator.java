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
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.CallableDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.nodeTypes.modifiers.NodeWithAccessModifiers;
import com.github.javaparser.ast.type.Type;

/**
 * Comparator to determine the order of a {@link Node} from a {@link JavaParser} {@link CompilationUnit}.
 *
 * @author Daan
 */
public class NodeComparator implements Comparator<Node> {
  // TODO support other modifiers like: static & final

  public boolean nodeTypeIsSupported(Node node) {
    return Arrays.asList( //
        PackageDeclaration.class //
        , ImportDeclaration.class //
        , ClassOrInterfaceDeclaration.class //
        , BodyDeclaration.class //
        , FieldDeclaration.class //
    ).stream().anyMatch(claz -> claz.isAssignableFrom(node.getClass()));
  }

  @Override
  public int compare(Node a, Node b) {
    Integer compare = nodeTypeIsSupported(a) && nodeTypeIsSupported(b) ? null : -1;
    compare = compare != null ? compare : comparePackage(a, b);
    compare = compare != null ? compare : compareImport(a, b);
    compare = compare != null ? compare : compareField(a, b);
    compare = compare != null ? compare : compareConstructor(a, b);
    compare = compare != null ? compare : compareMethod(a, b);
    compare = compare != null ? compare : compareClass(a, b);

    return compare == null ? -1 : compare;
  }

  private Integer comparePackage(Node a, Node b) {
    Integer compare;
    if (a instanceof PackageDeclaration) {
      compare = b instanceof PackageDeclaration ? 0 : -1;
    } else if (b instanceof PackageDeclaration) {
      compare = 1;
    } else {
      compare = null;
    }
    return compare;
  }

  private Integer compareImport(Node a, Node b) {
    // TODO find equal imports
    Integer compare;
    if (a instanceof ImportDeclaration) {
      compare = -1;
    } else if (b instanceof ImportDeclaration) {
      compare = 1;
    } else {
      compare = null;
    }
    return compare;
  }

  private Integer compareField(Node a, Node b) {
    BiFunction<Node, Node, Boolean> equals =
        (x, y) -> ((FieldDeclaration) x).getVariable(0).getNameAsString().equals(((FieldDeclaration) y).getVariable(0).getNameAsString());
    return compareNode(a, b, FieldDeclaration.class, equals);
  }

  private Integer compareConstructor(Node a, Node b) {
    return compareNode(a, b, ConstructorDeclaration.class, this::isBodyDeclarationEqual);
  }

  private Integer compareMethod(Node a, Node b) {
    return compareNode(a, b, MethodDeclaration.class, this::isBodyDeclarationEqual);
  }

  private Integer compareClass(Node a, Node b) {
    BiFunction<Node, Node, Boolean> equals =
        (x, y) -> ((ClassOrInterfaceDeclaration) x).getNameAsString().equals(((ClassOrInterfaceDeclaration) y).getNameAsString());
    return compareNode(a, b, ClassOrInterfaceDeclaration.class, equals);
  }

  private Boolean isBodyDeclarationEqual(Node a, Node b) {
    boolean isReplacement = false;
    CallableDeclaration<?> m1 = (CallableDeclaration<?>) a;
    CallableDeclaration<?> m2 = (CallableDeclaration<?>) b;
    isReplacement = m1.getName().equals(m2.getName());
    List<Type> parameterTypes1 = m1.getParameters().stream().map(p -> p.getType()).collect(Collectors.toList());
    List<Type> parameterTypes2 = m2.getParameters().stream().map(p -> p.getType()).collect(Collectors.toList());
    isReplacement = isReplacement && parameterTypes1.equals(parameterTypes2);
    return isReplacement;
  }

  private Integer compareNode(Node a, Node b, Class<?> claz, BiFunction<Node, Node, Boolean> equals) {
    Integer compare;
    if (claz.isAssignableFrom(a.getClass()) && claz.isAssignableFrom(b.getClass())) {
      if (equals.apply(a, b)) {
        compare = 0;
      } else {
        compare = compareModifiers(a, b);
      }
    } else if (claz.isAssignableFrom(a.getClass())) {
      compare = -1;
    } else if (claz.isAssignableFrom(b.getClass())) {
      compare = 1;
    } else {
      compare = null;
    }
    return compare;
  }

  private Integer compareModifiers(Node a, Node b) {
    Integer compare = null;
    if (NodeWithAccessModifiers.class.isAssignableFrom(a.getClass()) && NodeWithAccessModifiers.class.isAssignableFrom(b.getClass())) {
      EnumSet<Modifier> modA = ((NodeWithAccessModifiers<?>) a).getModifiers();
      EnumSet<Modifier> modB = ((NodeWithAccessModifiers<?>) b).getModifiers();
      if (modA.contains(Modifier.PUBLIC)) {
        compare = -1;
      } else if (modA.contains(Modifier.PROTECTED)) {
        compare = !modB.contains(Modifier.PUBLIC) && !isDefaultModifier(modB) ? -1 : 1;
      } else if (modA.contains(Modifier.PRIVATE)) {
        compare = modB.contains(Modifier.PRIVATE) ? -1 : 1;
      } else {
        compare = !modB.contains(Modifier.PUBLIC) ? -1 : 1;
      }
    }
    return compare;
  }

  private boolean isDefaultModifier(EnumSet<Modifier> modifiers) {
    return !modifiers.contains(Modifier.PUBLIC) && !modifiers.contains(Modifier.PROTECTED) && !modifiers.contains(Modifier.PRIVATE);
  }

}
