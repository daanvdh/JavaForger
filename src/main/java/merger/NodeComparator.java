package merger;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.nodeTypes.modifiers.NodeWithAccessModifiers;

public class NodeComparator implements Comparator<Node> {

  List<Class<?>> classOrder = Arrays.asList( //
      PackageDeclaration.class //
      , ImportDeclaration.class //
      , FieldDeclaration.class //
      , ConstructorDeclaration.class //
      , MethodDeclaration.class //
      , ClassOrInterfaceDeclaration.class //
  // , ExplicitConstructorInvocationStmt.class //
  // , BlockStmt.class //
  // , ExpressionStmt.class //
  // , LineComment.class //
  // , ReturnStmt.class //
  );

  @Override
  public int compare(Node o1, Node o2) {
    Integer compare = null;
    for (int i = 0; i < classOrder.size() && compare == null; i++) {
      Class<?> claz = classOrder.get(i);
      boolean o2Assignable = claz.isAssignableFrom(o2.getClass());
      boolean o1Assignable = claz.isAssignableFrom(o1.getClass());
      if (o1Assignable && o2Assignable) {
        compare = 0; // TODO call compare modifiers here
      } else if (o1Assignable) {
        compare = -1;
      } else if (o2Assignable) {
        compare = 1;
      }
    }
    if (compare == null) {
      // If no ordering was found, the caller needs to determine the ordering itself.
      compare = 0;
    }
    return compare;
  }

  private Integer compareModifiers(Node a, Node b) {
    Integer compare = null;
    if (NodeWithAccessModifiers.class.isAssignableFrom(a.getClass()) && NodeWithAccessModifiers.class.isAssignableFrom(b.getClass())) {
      NodeList<Modifier> modA = ((NodeWithAccessModifiers<?>) a).getModifiers();
      NodeList<Modifier> modB = ((NodeWithAccessModifiers<?>) b).getModifiers();
      if (modA.contains(Modifier.publicModifier())) {
        compare = -1;
      } else if (modA.contains(Modifier.protectedModifier())) {
        compare = !modB.contains(Modifier.publicModifier()) && !isDefaultModifier(modB) ? -1 : 1;
      } else if (modA.contains(Modifier.privateModifier())) {
        compare = modB.contains(Modifier.privateModifier()) ? -1 : 1;
      } else {
        compare = !modB.contains(Modifier.publicModifier()) ? -1 : 1;
      }
    }
    return compare;
  }

  private boolean isDefaultModifier(NodeList<Modifier> modifiers) {
    return !modifiers.contains(Modifier.publicModifier()) && !modifiers.contains(Modifier.protectedModifier())
        && !modifiers.contains(Modifier.privateModifier());
  }

}
