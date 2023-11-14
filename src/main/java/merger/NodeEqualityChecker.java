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

import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.CallableDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.comments.LineComment;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.NullLiteralExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.expr.ThisExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;

/**
 * Comparator to determine the order of a {@link Node} from a {@link JavaParser} {@link CompilationUnit}.
 *
 * @author Daan
 */
public class NodeEqualityChecker {

  /**
   * Set this value to false if any {@link Node} is only equal if also all it's inner {@link BlockStmt}s are equal. Setting this to true will for example make
   * if statements and loops equal based only on the if/loop itself and not what's defined inside the {@link BlockStmt}. For example "if(x) {y;}" will be equal
   * to "if(x) {z;}.
   */
  private boolean ignoreInnerCodeBlocks = true;

  public boolean isEqualIgnoreChildren(Node a, Node b) {
    Boolean isEqual = false;
    if (a instanceof MethodCallExpr && b instanceof MethodCallExpr) {
      MethodCallExpr m1 = (MethodCallExpr) a;
      MethodCallExpr m2 = (MethodCallExpr) b;
      // TODO only checking the number of arguments is an over-simplification
      isEqual = m1.getNameAsString().equals(m2.getNameAsString()) && m1.getArguments().equals(m2.getArguments());
    }
    return isEqual;
  }

  public boolean isEqual(Node a, Node b) {
    Boolean compare = comparePackage(a, b);
    compare = compare != null ? compare : compareImport(a, b);
    compare = compare != null ? compare : compareField(a, b);
    compare = compare != null ? compare : compareConstructor(a, b);
    compare = compare != null ? compare : compareMethodDeclaration(a, b);
    compare = compare != null ? compare : compareClass(a, b);
    compare = compare != null ? compare : compareLineComment(a, b);

    compare = compareInsideBlockStatement(compare, a, b);
    return compare == null ? false : compare;
  }

  /**
   * This method should only be used if we know the given nodes exists within the same {@link BlockStmt}.
   *
   * @param previousResult Optional previous result, if present, no further calculation will be done.
   * @param a
   * @param b
   * @return
   */
  private Boolean compareInsideBlockStatement(Boolean previousResult, Node a, Node b) {
    Boolean compare = previousResult;
    compare = compare != null ? compare : compareSimpleName(a, b);
    compare = compare != null ? compare : compareNameExpr(a, b);
    compare = compare != null ? compare : compareNullLiteralExpr(a, b);
    compare = compare != null ? compare : compareStringLiteralExpr(a, b);
    // compare = compare != null ? compare : compareMethodCallExpr(a, b);
    compare = compare != null ? compare : compareThisExpr(a, b);
    compare = compare != null ? compare : compareIfStmt(a, b);
    compare = compare != null ? compare : compareChildren(a, b);
    return compare;
  }

  private Boolean comparePackage(Node a, Node b) {
    Boolean compare;
    if (a instanceof PackageDeclaration) {
      compare = b instanceof PackageDeclaration;
    } else if (b instanceof PackageDeclaration) {
      compare = false;
    } else {
      compare = null;
    }
    return compare;
  }

  private Boolean compareImport(Node a, Node b) {
    Boolean compare = null;
    if (a instanceof ImportDeclaration && b instanceof ImportDeclaration) {
      compare = false;
      ImportDeclaration i1 = (ImportDeclaration) a;
      ImportDeclaration i2 = (ImportDeclaration) b;
      if (i1.getNameAsString().equals(i2.getNameAsString())) {
        compare = true;
      }
    }
    return compare;
  }

  private Boolean compareField(Node a, Node b) {
    BiFunction<Node, Node, Boolean> equals =
        (x, y) -> ((FieldDeclaration) x).getVariable(0).getNameAsString().equals(((FieldDeclaration) y).getVariable(0).getNameAsString());
    return compareNodeWithModifiers(a, b, FieldDeclaration.class, equals);
  }

  private Boolean compareConstructor(Node a, Node b) {
    return compareNodeWithModifiers(a, b, ConstructorDeclaration.class, this::isBodyDeclarationEqual);
  }

  /**
   * Compares if 2 method names and parameters for both are equal. Ignores any logic defined inside the body.
   *
   * @param a input {@link Node}
   * @param b input {@link Node}
   * @return 0 if name and parameters are equal, 1 or -1 otherwise.
   */
  private Boolean compareMethodDeclaration(Node a, Node b) {
    return compareNodeWithModifiers(a, b, MethodDeclaration.class, this::isBodyDeclarationEqual);
  }

  private Boolean compareClass(Node a, Node b) {
    BiFunction<Node, Node, Boolean> equals =
        (x, y) -> ((ClassOrInterfaceDeclaration) x).getNameAsString().equals(((ClassOrInterfaceDeclaration) y).getNameAsString());
    return compareNodeWithModifiers(a, b, ClassOrInterfaceDeclaration.class, equals);
  }

  private Boolean compareLineComment(Node a, Node b) {
    BiFunction<Node, Node, Boolean> equals = (x, y) -> ((LineComment) x).getContent().equals(((LineComment) y).getContent());
    return compareNode(a, b, LineComment.class, equals);
  }

  private Boolean compareSimpleName(Node a, Node b) {
    return compareNode(a, b, SimpleName.class, (x, y) -> ((SimpleName) x).asString().equals(((SimpleName) y).asString()));
  }

  private Boolean compareNameExpr(Node a, Node b) {
    return compareNode(a, b, NameExpr.class, (x, y) -> ((NameExpr) x).getNameAsString().equals(((NameExpr) y).getNameAsString()));
  }

  private Boolean compareStringLiteralExpr(Node a, Node b) {
    return compareNode(a, b, StringLiteralExpr.class, (x, y) -> ((StringLiteralExpr) x).getValue().equals(((StringLiteralExpr) y).getValue()));
  }

  // private Boolean compareMethodCallExpr(Node a, Node b) {
  // return compareNode(a, b, MethodCallExpr.class, (x, y) -> ((MethodCallExpr) x).getNameAsString().equals(((MethodCallExpr) y).getNameAsString()));
  // }

  private Boolean compareNullLiteralExpr(Node a, Node b) {
    return compareNode(a, b, NullLiteralExpr.class, (x, y) -> true);
  }

  private Boolean compareThisExpr(Node a, Node b) {
    return compareNode(a, b, ThisExpr.class, (x, y) -> true);
  }

  private Boolean compareIfStmt(Node a, Node b) {
    return compareNode(a, b, IfStmt.class, (x, y) -> this.compareInsideBlockStatement(null, ((IfStmt) x).getCondition(), ((IfStmt) y).getCondition()));
  }

  /**
   * Compares if 2 method names and parameters for both are equal. Ignores any logic defined inside the body.
   *
   * @param a input {@link CallableDeclaration}
   * @param b input {@link CallableDeclaration}
   * @return true if name and parameters are equal, false otherwise.
   */
  private Boolean isBodyDeclarationEqual(Node a, Node b) {
    CallableDeclaration<?> m1 = (CallableDeclaration<?>) a;
    CallableDeclaration<?> m2 = (CallableDeclaration<?>) b;
    List<Type> parameterTypes1 = m1.getParameters().stream().map(p -> p.getType()).collect(Collectors.toList());
    List<Type> parameterTypes2 = m2.getParameters().stream().map(p -> p.getType()).collect(Collectors.toList());
    boolean isReplacement = m1.getName().equals(m2.getName()) && compareMethodParameters(parameterTypes1, parameterTypes2);
    return isReplacement;
  }

  private boolean compareMethodParameters(List<Type> parameterTypes1, List<Type> parameterTypes2) {
    boolean isReplacement = (parameterTypes1.size() == parameterTypes2.size());
    for (int i = 0; i < parameterTypes1.size() && isReplacement; i++) {
      Type t1 = parameterTypes1.get(i);
      Type t2 = parameterTypes2.get(i);
      if (t1 instanceof ClassOrInterfaceType && t2 instanceof ClassOrInterfaceType) {
        isReplacement = ((ClassOrInterfaceType) t1).getNameAsString().equals(((ClassOrInterfaceType) t2).getNameAsString());
      } else {
        isReplacement = t1.equals(t2);
      }
    }
    return isReplacement;
  }

  private Boolean compareNodeWithModifiers(Node a, Node b, Class<?> claz, BiFunction<Node, Node, Boolean> equals) {
    Boolean compare;
    if (claz.isAssignableFrom(a.getClass()) && claz.isAssignableFrom(b.getClass())) {
      compare = equals.apply(a, b);
    } else if (claz.isAssignableFrom(a.getClass()) || claz.isAssignableFrom(b.getClass())) {
      compare = false;
    } else {
      compare = null;
    }
    return compare;
  }

  private Boolean compareNode(Node a, Node b, Class<?> claz, BiFunction<Node, Node, Boolean> equals) {
    Boolean compare;
    if (claz.isAssignableFrom(a.getClass()) && claz.isAssignableFrom(b.getClass())) {
      if (equals.apply(a, b)) {
        compare = true;
      } else {
        compare = false;
      }
    } else if (claz.isAssignableFrom(a.getClass())) {
      compare = false;
    } else if (claz.isAssignableFrom(b.getClass())) {
      compare = false;
    } else {
      compare = null;
    }
    return compare;
  }

  private Boolean compareChildren(Node a, Node b) {
    List<Node> as = a.getChildNodes();
    List<Node> bs = b.getChildNodes();
    if (as.isEmpty() || bs.isEmpty()) {
      // Cannot determine equality if no children are present.
      return null;
    } else if (as.size() != bs.size()) {
      return false;
    }
    Boolean equalUnitNow = null; // null : undetermined, 0 : yes, 1 : no, -1 : no.
    for (int i = 0; i < as.size(); i++) {
      Node x = as.get(i);
      Node y = bs.get(i);
      if (!x.getClass().equals(y.getClass())) {
        equalUnitNow = false;
        break;
      } else if (this.ignoreInnerCodeBlocks && BlockStmt.class.isAssignableFrom(x.getClass())) {
        // Skip inner BockStmt, equality only depends on the main node.
        continue;
      }
      // Recursive call
      Boolean compare = compareInsideBlockStatement(null, x, y);
      if (compare != null) {
        equalUnitNow = compare;
        if (!compare) {
          // not equal
          break;
        }
      }
    }
    return equalUnitNow;

  }

  public boolean isIgnoreInnerCodeBlocks() {
    return ignoreInnerCodeBlocks;
  }

  public void setIgnoreInnerCodeBlocks(boolean ignoreInnerCodeBlocks) {
    this.ignoreInnerCodeBlocks = ignoreInnerCodeBlocks;
  }

}
