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
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.resolution.declarations.ResolvedValueDeclaration;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserFieldDeclaration;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserParameterDeclaration;

/**
 * Factory for creating a {@link DataFlowGraph} from a {@link JavaParser} {@link CompilationUnit}.
 *
 * @author Daan
 */
public class DataFlowGraphFactory {

  public DataFlowGraph createGraph(CompilationUnit cu) {
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
      graph.addField(parseField((FieldDeclaration) node));
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

  private DataFlowNode parseField(FieldDeclaration node) {
    return DataFlowNode.builder().representedNode(node).name(node.getVariable(0).getNameAsString()).build();
  }

  private void parseCallable(DataFlowGraph graph, CallableDeclaration<?> cd) {
    // TODO we need this method later to add outgoing and incoming nodes to.
    DataFlowMethod method = graph.getMethod(cd);
    // The values that are overwridden inside this method, for example assigning a field.
    Map<Node, DataFlowNode> overwriddenValues = new HashMap<>();

    Optional<Node> callableBody = cd.getChildNodes().stream().filter(n -> BlockStmt.class.isAssignableFrom(n.getClass())).findFirst();

    if (callableBody.isPresent()) {
      List<Node> bodyNodes = callableBody.get().getChildNodes();
      for (Node n : bodyNodes) {
        if (n instanceof ExpressionStmt) {

          for (Node c : n.getChildNodes()) {
            if (c instanceof AssignExpr) {
              DataFlowNode flowNode = new DataFlowNode(c);
              List<Node> assignExpr = c.getChildNodes();
              // TODO this is not used, which is fine for now, this has to be extracted at some point and in different cases we might need it
              DataFlowNode assigned = null;
              DataFlowNode assigner = null;
              Node assignedNode = assignExpr.get(0);

              if (assignedNode instanceof FieldAccessExpr) {
                ResolvedValueDeclaration resolve = ((FieldAccessExpr) assignedNode).resolve();
                if (resolve instanceof JavaParserFieldDeclaration) {
                  FieldDeclaration resolvedNode = ((JavaParserFieldDeclaration) resolve).getWrappedNode();
                  assigned = graph.getNode(resolvedNode);
                  overwriddenValues.put(resolvedNode, flowNode);
                }
              }

              flowNode.setName(method.getName() + "." + assigned.getName());
              // TODO fill other fields of the flow node

              Node assignerNode = assignExpr.get(1);
              if (assignerNode instanceof NameExpr) {
                ResolvedValueDeclaration resolve = ((NameExpr) assignerNode).resolve();
                if (resolve instanceof JavaParserParameterDeclaration) {
                  Parameter resolvedNode = ((JavaParserParameterDeclaration) resolve).getWrappedNode();
                  assigner = graph.getNode(resolvedNode);
                }
              }
              // TODO make this null safe
              assigner.addEdgeTo(flowNode);
              // TODO this code should be located somewhere else, because there might be something else happening in between two assignments.
              // flowNode.addEdgeTo(assigned);
            }
          }
        }
      }
    }

    // Each overwridden value has to receive the value that it was overwridden with
    overwriddenValues.forEach((javaParserNode, dataFlowNode) -> dataFlowNode.addEdgeTo(graph.getNode(javaParserNode)));

    // Add changed fields
    overwriddenValues.keySet().stream().filter(javaParserNode -> FieldDeclaration.class.isAssignableFrom(javaParserNode.getClass())).map(graph::getNode)
        .filter(n -> n != null).forEach(method::addChangedField);
  }

  private List<DataFlowNode> parseParameters(CallableDeclaration<?> cd) {
    return cd.getParameters().stream().map(n -> DataFlowNode.builder().representedNode(n).name(n.getNameAsString()).build()).collect(Collectors.toList());
  }

}
