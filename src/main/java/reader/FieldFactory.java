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
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.Expression;

import templateInput.definition.VariableDefinition;
import templateInput.definition.VariableDefinition.Builder;

/**
 * Class for creating {@link VariableDefinition} from parsed fields.
 *
 * @author Daan
 */
public class FieldFactory {
  private static final Logger LOG = LoggerFactory.getLogger(FieldFactory.class);

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
    VariableDefinition field = null;

    if (node instanceof FieldDeclaration) {
      FieldDeclaration fd = (FieldDeclaration) node;
      VariableDefinition.Builder<?> fieldBuilder = createField(fd);
      fd.getVariables().stream().map(VariableDeclarator::getNameAsString).map(fieldBuilder::name).map(VariableDefinition.Builder::build).forEach(fields::add);
    } else if (node instanceof VariableDeclarator) {
      field = createSingle((VariableDeclarator) node);
      fields.add(field);
    }

    return fields;
  }

  /**
   * Since a {@link FieldDeclaration} can define multiple variables with (<code>int i,j;</code>), you can use this method to create a single
   * {@link VariableDefinition} from a single {@link VariableDeclarator}.
   *
   * @param vd The input {@link VariableDeclarator}
   * @return a {@link VariableDefinition} representing the node.
   */
  public VariableDefinition createSingle(VariableDeclarator vd) {
    Optional<Node> parentNode = vd.getParentNode();
    VariableDefinition.Builder<?> fieldBuilder;
    if (parentNode.isPresent() && parentNode.get() instanceof FieldDeclaration) {
      fieldBuilder = createField((FieldDeclaration) parentNode.get());
    } else {
      LOG.warn("VariableDeclarator {} did not have a valid parent, resulting VariableDefinition will only have a name. Parent was {} of type {}", vd,
          parentNode.orElse(null), parentNode.map(Node::getClass).orElse(null));
      fieldBuilder = VariableDefinition.builder();
    }
    return fieldBuilder.name(vd.getNameAsString()).build();
  }

  private Builder<?> createField(FieldDeclaration fd) {
    Set<String> annotations = fd.getAnnotations().stream().map(annotation -> annotation.getName().toString()).collect(Collectors.toSet());
    Set<String> accessModifiers = fd.getModifiers().stream().map(modifier -> modifier.asString()).collect(Collectors.toSet());
    Optional<String> originalInit = depthFirstSearch(fd, Expression.class);
    List<String> imports = importResolver.resolveImport(fd.getElementType());
    VariableDefinition.Builder<?> fieldBuilder =
        VariableDefinition.builder().type(fd.getElementType().asString()).annotations(annotations).lineNumber(fd.getBegin().map(p -> p.line).orElse(-1))
            .column(fd.getBegin().map(p -> p.column).orElse(-1)).accessModifiers(accessModifiers).originalInit(originalInit.orElse(null)).typeImports(imports);
    return fieldBuilder;
  }

  private Optional<String> depthFirstSearch(Node node, Class<Expression> claz) {
    if (claz.isAssignableFrom(node.getClass())) {
      return Optional.of(node.toString());
    }
    return node.getChildNodes().stream().map(n -> depthFirstSearch(n, claz)).filter(Optional::isPresent).map(Optional::get).map(Object::toString).findFirst();
  }
}
