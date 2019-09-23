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
package dataflow;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.javaparser.JavaParser;
import com.github.javaparser.Position;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.CallableDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.type.VoidType;

/**
 * Factory for creating a {@link DataFlowGraph} from a {@link JavaParser} {@link CompilationUnit}.
 *
 * @author Daan
 */
public class DataFlowGraphFactory {
  private static final Logger LOG = LoggerFactory.getLogger(DataFlowGraphFactory.class);

  private DataFlowResolver resolver = new DataFlowResolver();

  /**
   * Creates a {@link DataFlowGraph} for the given {@link CompilationUnit}.
   *
   * @param cu The {@link CompilationUnit} containing the parsed class.
   * @return A {@link DataFlowGraph}
   */
  public DataFlowGraph create(CompilationUnit cu) {
    DataFlowGraph graph = new DataFlowGraph();
    executeForEachChildNode(cu, (node) -> this.addField(graph, node));
    executeForEachChildNode(cu, (node) -> this.createMethod(graph, node));
    executeForEachChildNode(cu, (node) -> this.fillMethod(graph, node));
    return graph;
  }

  private void executeForEachChildNode(CompilationUnit cu, Consumer<Node> consumer) {
    for (TypeDeclaration<?> type : cu.getTypes()) {
      List<Node> childNodes = type.getChildNodes();
      for (Node node : childNodes) {
        consumer.accept(node);
      }
    }
  }

  private void addField(DataFlowGraph graph, Node node) {
    if (node instanceof FieldDeclaration) {
      parseField((FieldDeclaration) node).forEach(graph::addField);
    }
  }

  private DataFlowMethod createMethod(DataFlowGraph graph, Node node) {
    DataFlowMethod method = null;
    if (node instanceof CallableDeclaration) {
      CallableDeclaration<?> cd = (CallableDeclaration<?>) node;
      method = new DataFlowMethod(graph, node, cd.getNameAsString());
      method.setInputParameters(parseParameters(cd));
      if (node instanceof MethodDeclaration) {
        MethodDeclaration md = (MethodDeclaration) node;
        if (!(md.getType() instanceof VoidType)) {
          method.setReturnNode(new DataFlowNode(cd.getNameAsString() + ".return", node));
        }
      } else {
        // Always add a return statement for a constructor.
        method.setReturnNode(new DataFlowNode(node));
      }
    }
    return method;
  }

  private void fillMethod(DataFlowGraph graph, Node node) {
    if (node instanceof MethodDeclaration) {
      MethodDeclaration md = (MethodDeclaration) node;
      parseCallable(graph, md);
    }
  }

  /**
   * Returns a list of DataFlowNodes that represent all fields defined with this fieldDeclaration. (With the syntax <code>int i,j; </code> one FieldDeclaration
   * can define multiple fields.
   *
   * @param node
   * @return
   */
  private List<DataFlowNode> parseField(FieldDeclaration node) {
    return node.getVariables().stream().map(n -> DataFlowNode.builder().representedNode(n).name(n.getNameAsString()).build()).collect(Collectors.toList());
  }

  private void parseCallable(DataFlowGraph graph, CallableDeclaration<?> cd) {
    // TODO we need this method later to add outgoing and incoming nodes too.
    DataFlowMethod method = graph.getMethod(cd);
    // The values that are overwridden inside this method, for example assigning a field.
    Map<Node, DataFlowNode> overwriddenValues = new HashMap<>();

    Optional<BlockStmt> callableBody =
        cd.getChildNodes().stream().filter(n -> BlockStmt.class.isAssignableFrom(n.getClass())).findFirst().map(BlockStmt.class::cast);

    if (callableBody.isPresent()) {
      handleBlockStmt(graph, method, overwriddenValues, callableBody.get());
    }

    // Each overwridden value has to receive the value that it was overwridden with
    overwriddenValues.forEach((javaParserNode, dataFlowNode) -> dataFlowNode.addEdgeTo(graph.getNode(javaParserNode)));

    // Add changed fields
    overwriddenValues.keySet().stream().filter(javaParserNode -> VariableDeclarator.class.isAssignableFrom(javaParserNode.getClass())).map(graph::getNode)
        .filter(n -> n != null).forEach(method::addChangedField);
  }

  private Optional<DataFlowNode> handleNode(DataFlowGraph graph, DataFlowMethod method, Map<Node, DataFlowNode> overwriddenValues, Node n) {
    Optional<DataFlowNode> created = Optional.empty();
    if (n instanceof BlockStmt) {
      created = handleBlockStmt(graph, method, overwriddenValues, (BlockStmt) n);
    } else if (n instanceof ExpressionStmt) {
      created = handleExpressionStmt(graph, method, overwriddenValues, (ExpressionStmt) n);
    } else if (n instanceof AssignExpr) {
      created = handleAssignExpr(graph, method, overwriddenValues, (AssignExpr) n);
    } else if (n instanceof ReturnStmt) {
      created = handleReturnStmt(graph, method, overwriddenValues, (ReturnStmt) n);
    } else if (n instanceof NameExpr) {
      created = handleNameExpr(graph, method, overwriddenValues, (NameExpr) n);
    } else if (n instanceof MethodCallExpr) {
      created = handleMethodCallExpr(graph, method, overwriddenValues, (MethodCallExpr) n);
    } else if (n instanceof VariableDeclarationExpr) {
      created = handleVariableDeclarationExpr(graph, method, overwriddenValues, (VariableDeclarationExpr) n);
    } else if (n instanceof VariableDeclarator) {
      created = handleVariableDeclarator(graph, method, overwriddenValues, (VariableDeclarator) n);
    } else {
      LOG.warn("In method {} could not handle node [{}] of type {}", method.getName(), n, n.getClass());
    }
    return created;
  }

  private Optional<DataFlowNode> handleVariableDeclarator(DataFlowGraph graph, DataFlowMethod method, Map<Node, DataFlowNode> overwriddenValues,
      VariableDeclarator n) {
    DataFlowNode created = method.addNode(n.getNameAsString(), n);
    Optional<Expression> initializer = n.getInitializer();
    if (initializer.isPresent()) {
      Optional<DataFlowNode> assigner = handleNode(graph, method, overwriddenValues, initializer.get());
      if (assigner.isPresent()) {
        assigner.get().addEdgeTo(created);
      } else {
        LOG.warn("In method {} was not able to resolve {} of type {}", method.getName(), initializer.get(), initializer.get().getClass());
      }
    }

    return Optional.ofNullable(created);
  }

  private Optional<DataFlowNode> handleVariableDeclarationExpr(DataFlowGraph graph, DataFlowMethod method, Map<Node, DataFlowNode> overwriddenValues,
      VariableDeclarationExpr n) {
    NodeList<VariableDeclarator> variables = n.getVariables();
    for (VariableDeclarator vd : variables) {
      handleNode(graph, method, overwriddenValues, vd);
    }
    return Optional.empty();
  }

  private Optional<DataFlowNode> handleMethodCallExpr(DataFlowGraph graph, DataFlowMethod method, Map<Node, DataFlowNode> overwriddenValues, MethodCallExpr n) {
    Optional<DataFlowMethod> optionalCalledMethod = resolver.getDataFlowMethod(graph, method, n);
    if (!optionalCalledMethod.isPresent()) {
      LOG.warn("In method {} could not resolve method call {} of type {}", method.getName(), n, n.getClass());
      return Optional.empty();
    }
    DataFlowMethod calledMethod = optionalCalledMethod.get();

    NodeList<Expression> arguments = n.getArguments();
    if (arguments.size() != calledMethod.getInputParameters().size()) {
      LOG.warn("In method {} for called method {} the used nof arguments {} is not equal to the expected nof arguments {}", method.getName(), arguments.size(),
          calledMethod.getInputParameters().size());
      return Optional.empty();
    }

    for (int i = 0; i < arguments.size(); i++) {
      Optional<DataFlowNode> arg = handleNode(graph, calledMethod, overwriddenValues, arguments.get(i));
      if (arg.isPresent()) {
        arg.get().addEdgeTo(calledMethod.getInputParameters().get(i));
      }
    }

    // Return the return node of the called method so that the return value can be assigned to the caller.
    return Optional.ofNullable(calledMethod.getReturnNode());
  }

  private Optional<DataFlowNode> handleBlockStmt(DataFlowGraph graph, DataFlowMethod method, Map<Node, DataFlowNode> overwriddenValues, BlockStmt node) {
    for (Node n : node.getChildNodes()) {
      handleNode(graph, method, overwriddenValues, n);
    }
    return Optional.empty();
  }

  private Optional<DataFlowNode> handleReturnStmt(DataFlowGraph graph, DataFlowMethod method, Map<Node, DataFlowNode> overwriddenValues, ReturnStmt n) {
    DataFlowNode createdReturn = null;
    if (n.getExpression().isPresent()) {
      Expression expression = n.getExpression().get();
      Optional<DataFlowNode> assignToReturn = handleNode(graph, method, overwriddenValues, expression);

      if (assignToReturn.isPresent()) {
        createdReturn = method.addNode(method.getName() + ".return" + n.getBegin().map(Position::toString).orElse("?"), n);
        assignToReturn.get().addEdgeTo(createdReturn);
        createdReturn.addEdgeTo(method.getReturnNode());
      } else {
        LOG.warn("In method {} could not find node for assigning to the return value for node {} of type {}", method.getName(), expression,
            expression.getClass());
      }
    }
    return Optional.ofNullable(createdReturn);
  }

  /**
   * Only gets an existing node for the given {@link NameExpr}.
   */
  private Optional<DataFlowNode> handleNameExpr(DataFlowGraph graph, DataFlowMethod method, Map<Node, DataFlowNode> overwriddenValues, NameExpr n) {
    return resolver.getDataFlowNode(graph, method, overwriddenValues, n);
  }

  private Optional<DataFlowNode> handleExpressionStmt(DataFlowGraph graph, DataFlowMethod method, Map<Node, DataFlowNode> overwriddenValues, ExpressionStmt n) {
    for (Node c : n.getChildNodes()) {
      handleNode(graph, method, overwriddenValues, c);
    }
    return null;
  }

  private Optional<DataFlowNode> handleAssignExpr(DataFlowGraph graph, DataFlowMethod method, Map<Node, DataFlowNode> overwriddenValues, AssignExpr expr) {
    Expression assignedJP = expr.getTarget();
    Expression assignerJP = expr.getValue();
    // This is the original field
    Optional<DataFlowNode> assignedDF = resolver.getDataFlowNode(graph, method, overwriddenValues, assignedJP);
    Optional<DataFlowNode> assignerDF = resolver.getDataFlowNode(graph, method, overwriddenValues, assignerJP);
    if (assignedDF.isPresent() && assignerDF.isPresent()) {
      // This is the version of the field that will receive the assigner edge.
      // If this is the last assignment to the field, an edge to the original field will be created.
      DataFlowNode flowNode = method.addNode(assignedJP.toString(), assignedJP);
      if (isField(assignedDF.get())) {
        overwriddenValues.put(assignedDF.get().getRepresentedNode(), flowNode);
      }

      flowNode.setName(method.getName() + "." + assignedDF.get().getName());

      assignerDF.get().addEdgeTo(flowNode);
    }
    return null;
  }

  private boolean isField(DataFlowNode assignedDF) {
    // TODO handle the fact that this can represent a field in an in-between state, in that case we have to walk back to figure that out.
    // The question is how far do we have to walk back...
    Node representedNode = assignedDF.getRepresentedNode();
    boolean isField = false;
    if (representedNode instanceof VariableDeclarator) {
      VariableDeclarator vd = (VariableDeclarator) representedNode;
      if (vd.getParentNode().isPresent() && vd.getParentNode().get() instanceof FieldDeclaration) {
        isField = true;
      }
    }
    return isField;
  }

  private List<DataFlowNode> parseParameters(CallableDeclaration<?> cd) {
    return cd.getParameters().stream().map(n -> DataFlowNode.builder().representedNode(n).name(n.getNameAsString()).build()).collect(Collectors.toList());
  }

}
