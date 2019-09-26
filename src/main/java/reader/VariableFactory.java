/*
 * Copyright (c) 2019 by Eyefreight BV (www.eyefreight.com). All rights reserved.
 *
 * This software is provided by the copyright holder and contributors "as is" and any express or implied warranties, including, but
 * not limited to, the implied warranties of merchantability and fitness for a particular purpose are disclaimed. In no event shall
 * Eyefreight BV or contributors be liable for any direct, indirect, incidental, special, exemplary, or consequential damages
 * (including, but not limited to, procurement of substitute goods or services; * loss of use, data, or profits; or business
 * interruption) however caused and on any theory of liability, whether in contract, strict liability, or tort (including
 * negligence or otherwise) arising in any way out of the use of this software, even if advised of the possibility of such damage.
 */
package reader;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.nodeTypes.NodeWithAnnotations;
import com.github.javaparser.ast.nodeTypes.NodeWithSimpleName;
import com.github.javaparser.ast.nodeTypes.NodeWithVariables;

import templateInput.definition.VariableDefinition;
import templateInput.definition.VariableDefinition.Builder;

/**
 * Class for creating {@link VariableDefinition} from parsed fields.
 *
 * @author Daan
 */
public class VariableFactory {
  private static final Logger LOG = LoggerFactory.getLogger(VariableFactory.class);

  private ImportResolver importResolver = new ImportResolver();

  /**
   * Creates a {@link VariableDefinition} from a {@link FieldDeclaration} or a {@link VariableDeclarator} with {@link FieldDeclaration} as a parent. Will return
   * multiple {@link VariableDefinition}s in case multiple fields where defined within a single {@link FieldDeclaration} (<code>int i,j;</code>).
   *
   * @param node The input {@link Node}.
   * @return A {@link List} of {@link VariableDefinition} representing the input node.
   */
  public List<VariableDefinition> create(Node node) {
    List<VariableDefinition> fields = new ArrayList<>();

    if (node instanceof FieldDeclaration) {
      FieldDeclaration fd = (FieldDeclaration) node;
      VariableDefinition.Builder<?> fieldBuilder = createVariable(fd);
      fd.getVariables().stream().map(VariableDeclarator::getNameAsString).map(fieldBuilder::name).map(VariableDefinition.Builder::build).forEach(fields::add);
    } else if (node instanceof VariableDeclarator) {
      fields.add(createSingle(node));
    } else {
      fields.add(createVariable(node).build());
    }

    return fields;
  }

  /**
   * Since a {@link FieldDeclaration} can define multiple variables with (<code>int i,j;</code>), you can use this method to create a single
   * {@link VariableDefinition} from a single {@link VariableDeclarator}.
   *
   * @param node The input {@link VariableDeclarator}
   * @return a {@link VariableDefinition} representing the node.
   */
  public VariableDefinition createSingle(Node node) {
    return createVariable(node).build();
  }

  private Builder<?> createVariable(Node n) {
    VariableDefinition.Builder<?> fieldBuilder = VariableDefinition.builder();
    addType(n, fieldBuilder);
    addName(n, fieldBuilder);
    addAnnotations(n, fieldBuilder);
    addOriginalInit(n, fieldBuilder);
    addLineAndColumn(n, fieldBuilder);
    addAccessModifiers(n, fieldBuilder);
    return fieldBuilder;
  }

  private void addLineAndColumn(Node n, VariableDefinition.Builder<?> fieldBuilder) {
    fieldBuilder.lineNumber(n.getBegin().map(p -> p.line).orElse(-1)).column(n.getBegin().map(p -> p.column).orElse(-1));
  }

  private void addType(Node n, VariableDefinition.Builder<?> fieldBuilder) {
    if (NodeWithVariables.class.isAssignableFrom(n.getClass())) {
      NodeWithVariables<?> cast = (NodeWithVariables<?>) n;
      List<String> imports = importResolver.resolveImport(cast.getElementType());
      fieldBuilder.typeImports(imports).type(cast.getElementType().asString());
    }
  }

  private void addAccessModifiers(Node n, VariableDefinition.Builder<?> fieldBuilder) {
    Optional<FieldDeclaration> fd = Optional.empty();
    if (FieldDeclaration.class.isAssignableFrom(n.getClass())) {
      fd = Optional.of((FieldDeclaration) n);
    } else if (n instanceof VariableDeclarator) {
      Optional<Node> parentNode = n.getParentNode().filter(node -> FieldDeclaration.class.isAssignableFrom(node.getClass())).map(FieldDeclaration.class::cast);
      if (!fd.isPresent()) {
        LOG.warn("VariableDeclarator {} did not have a valid parent, resulting VariableDefinition will only have a name. Parent was {} of type {}", n,
            parentNode.orElse(null), parentNode.map(Node::getClass).orElse(null));
      }
    }
    fd.ifPresent(f -> fieldBuilder.accessModifiers(f.getModifiers().stream().map(modifier -> modifier.asString()).collect(Collectors.toSet())));
  }

  private void addAnnotations(Node n, VariableDefinition.Builder<?> fieldBuilder) {
    if (NodeWithAnnotations.class.isAssignableFrom(n.getClass())) {
      fieldBuilder
          .annotations(((NodeWithAnnotations<?>) n).getAnnotations().stream().map(annotation -> annotation.getName().toString()).collect(Collectors.toSet()));
    }
  }

  private void addName(Node n, VariableDefinition.Builder<?> fieldBuilder) {
    if (NodeWithSimpleName.class.isAssignableFrom(n.getClass())) {
      fieldBuilder.name(((NodeWithSimpleName<?>) n).getNameAsString());
    } else if (FieldDeclaration.class.isAssignableFrom(n.getClass())) {
      // Assign the first possible name in case multiple are defined with syntax: int a, b;
      fieldBuilder.name(((FieldDeclaration) n).getVariable(0).getNameAsString());
    }
  }

  private void addOriginalInit(Node n, VariableDefinition.Builder<?> fieldBuilder) {
    Optional<String> originalInit = depthFirstSearch(n, Expression.class);
    fieldBuilder.originalInit(originalInit.orElse(null));
  }

  private Optional<String> depthFirstSearch(Node node, Class<Expression> claz) {
    if (claz.isAssignableFrom(node.getClass())) {
      return Optional.of(node.toString());
    }
    return node.getChildNodes().stream().map(n -> depthFirstSearch(n, claz)).filter(Optional::isPresent).map(Optional::get).map(Object::toString).findFirst();
  }

}
