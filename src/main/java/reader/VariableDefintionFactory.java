/*
 * Copyright 2018 by Daan van den Heuvel.
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
package reader;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.nodeTypes.NodeWithAnnotations;
import com.github.javaparser.ast.nodeTypes.NodeWithSimpleName;
import com.github.javaparser.ast.nodeTypes.NodeWithType;
import com.github.javaparser.ast.nodeTypes.NodeWithVariables;

import templateInput.definition.VariableDefinition;
import templateInput.definition.VariableDefinition.Builder;

/**
 * Class for creating {@link VariableDefinition} from parsed fields.
 *
 * @author Daan
 */
public class VariableDefintionFactory {
  private static final Logger LOG = LoggerFactory.getLogger(VariableDefintionFactory.class);

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
    addTypeImports(n, fieldBuilder);
    addName(n, fieldBuilder);
    addAnnotations(n, fieldBuilder);
    addOriginalInit(n, fieldBuilder);
    addLineAndColumn(n, fieldBuilder);
    addAccessModifiers(n, fieldBuilder);
    return fieldBuilder;
  }

  private void addType(Node n, Builder<?> fieldBuilder) {
    // TODO Auto-generated method stub
    if (NodeWithType.class.isAssignableFrom(n.getClass())) {
      NodeWithType<?, ?> md = (NodeWithType<?, ?>) n;
      String type = md.getTypeAsString();
      fieldBuilder.type(type);
    }

  }

  private void addLineAndColumn(Node n, VariableDefinition.Builder<?> fieldBuilder) {
    fieldBuilder.lineNumber(n.getBegin().map(p -> p.line).orElse(-1)).column(n.getBegin().map(p -> p.column).orElse(-1));
  }

  private void addTypeImports(Node n, VariableDefinition.Builder<?> fieldBuilder) {
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
      Optional<Node> parentNode = n.getParentNode().filter(FieldDeclaration.class::isInstance).map(FieldDeclaration.class::cast);
      if (!fd.isPresent()) {
        LOG.warn("VariableDeclarator {} did not have a valid parent, resulting VariableDefinition will only have a name. Parent was {} of type {}", n,
            parentNode.orElse(null), parentNode.map(Node::getClass).orElse(null));
      }
    }
    fd.ifPresent(f -> fieldBuilder.accessModifiers(f.getModifiers().stream().map(Modifier::toString).map(String::trim).collect(Collectors.toSet())));
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
