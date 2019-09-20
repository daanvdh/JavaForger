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

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.CallableDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.resolution.Resolvable;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserFieldDeclaration;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserParameterDeclaration;

/**
 * Factory for creating a {@link DataFlowGraph} from a {@link JavaParser} {@link CompilationUnit}.
 *
 * @author Daan
 */
public class DataFlowGraphFactory {

  /**
   * Creates a {@link DataFlowGraph} for the given {@link CompilationUnit}.
   *
   * @param cu The {@link CompilationUnit} containing the parsed class.
   * @return A {@link DataFlowGraph}
   * @throws DataFlowException If types inside the {@link CompilationUnit} are not supported yet.
   */
  public DataFlowGraph create(CompilationUnit cu) throws DataFlowException {
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
    if (node instanceof MethodDeclaration) {
      CallableDeclaration<?> cd = (CallableDeclaration<?>) node;
      method = new DataFlowMethod(graph, node, cd.getNameAsString());
      method.setInputParameters(parseParameters(cd));
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

    Optional<Node> callableBody = cd.getChildNodes().stream().filter(n -> BlockStmt.class.isAssignableFrom(n.getClass())).findFirst();

    if (callableBody.isPresent()) {
      List<Node> bodyNodes = callableBody.get().getChildNodes();
      for (Node n : bodyNodes) {
        // TODO handle defining a variable and add it to the graph
        if (n instanceof ExpressionStmt) {
          handleExpressionStmt(graph, method, overwriddenValues, n);
        }
      }
    }

    // Each overwridden value has to receive the value that it was overwridden with
    overwriddenValues.forEach((javaParserNode, dataFlowNode) -> dataFlowNode.addEdgeTo(graph.getNode(javaParserNode)));

    // Add changed fields
    overwriddenValues.keySet().stream().filter(javaParserNode -> VariableDeclarator.class.isAssignableFrom(javaParserNode.getClass())).map(graph::getNode)
        .filter(n -> n != null).forEach(method::addChangedField);
  }

  private void handleExpressionStmt(DataFlowGraph graph, DataFlowMethod method, Map<Node, DataFlowNode> overwriddenValues, Node n) {
    for (Node c : n.getChildNodes()) {
      if (c instanceof AssignExpr) {
        handleAssignExpr(graph, method, overwriddenValues, (AssignExpr) c);
      }
    }
  }

  private void handleAssignExpr(DataFlowGraph graph, DataFlowMethod method, Map<Node, DataFlowNode> overwriddenValues, AssignExpr expr) {
    Expression assignedJP = expr.getTarget();
    Node assignerJP = expr.getValue();
    // This is the original field
    DataFlowNode assignedDF = getDataFlowNode(graph, overwriddenValues, method, assignedJP);
    DataFlowNode assignerDF = getDataFlowNode(graph, overwriddenValues, method, assignerJP);

    // This is the version of the field that will receive the assigner edge.
    // If this is the last assignment to the field, an edge to the original field will be created.
    DataFlowNode flowNode = new DataFlowNode(assignedJP);
    if (isField(assignedDF)) {
      overwriddenValues.put(assignedDF.getRepresentedNode(), flowNode);
    }

    flowNode.setName(method.getName() + "." + assignedDF.getName());

    assignerDF.addEdgeTo(flowNode);
  }

  // TODO should not throw exceptions, return optional instead and log warnings.
  private DataFlowNode getDataFlowNode(DataFlowGraph graph, Map<Node, DataFlowNode> overwriddenValues, DataFlowMethod method, Node node) {
    Node resolvedNode = getJavaParserNode(method, node);
    DataFlowNode flowNode = overwriddenValues.containsKey(resolvedNode) ? overwriddenValues.get(resolvedNode) : graph.getNode(resolvedNode);

    if (flowNode == null) {
      throw new DataFlowException("In method %s, did not resolve the type for assignedNode for expression %s of type %s", method.getName(), node,
          node.getClass());
    }
    return flowNode;
  }

  private Node getJavaParserNode(DataFlowMethod method, Node node) {
    if (!Resolvable.class.isAssignableFrom(node.getClass())) {
      throw new DataFlowException("In method %s, node is not Resolvable for expression %s of type %s", method.getName(), node, node.getClass());
    }

    Object resolved = ((Resolvable<?>) node).resolve();
    Node resolvedNode = null;
    if (resolved instanceof JavaParserFieldDeclaration) {
      resolvedNode = ((JavaParserFieldDeclaration) resolved).getVariableDeclarator();
    } else if (resolved instanceof JavaParserParameterDeclaration) {
      resolvedNode = ((JavaParserParameterDeclaration) resolved).getWrappedNode();
    }
    return resolvedNode;
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
