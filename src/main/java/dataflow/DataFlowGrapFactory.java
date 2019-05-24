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

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.CallableDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;

/**
 * TODO javadoc
 *
 * @author Daan
 */
public class DataFlowGrapFactory {

  public DataFlowGraph createGraph(CompilationUnit cu) {
    DataFlowGraph graph = new DataFlowGraph();

    for (TypeDeclaration<?> type : cu.getTypes()) {
      List<Node> childNodes = type.getChildNodes();
      for (Node node : childNodes) {
        if (node instanceof FieldDeclaration) {
          // fields.add(parseField(node));
        } else if (node instanceof MethodDeclaration) {
          graph.addMethod(parseMethod(node));
        } else if (node instanceof ConstructorDeclaration) {
          // constructors.add(parseConstructor(node));
        }
      }
    }

    return graph;
  }

  private DataFlowMethod parseMethod(Node node) {
    MethodDeclaration md = (MethodDeclaration) node;
    DataFlowMethod method = parseCallable(md);
    return method;
  }

  private DataFlowMethod parseCallable(CallableDeclaration<?> cd) {
    DataFlowMethod m = new DataFlowMethod();
    m.setInputParameters(parseParameters(cd));

    Optional<Node> callableBody = cd.getChildNodes().stream().filter(n -> BlockStmt.class.isAssignableFrom(n.getClass())).findFirst();

    if (callableBody.isPresent()) {
      List<Node> bodyNodes = callableBody.get().getChildNodes();
      for (Node n : bodyNodes) {
        if (n instanceof ExpressionStmt) {
          DataFlowNode flowNode = new DataFlowNode(n);
          // TODO now couple this node with incoming parameter
          // TODO also couple this node to the changedFields inside the DataFlowMethod
          // TODO store this node in a HashMap somewhere so that in next iterations we will select this node if we access the field.
        }
      }
    }

    return m;
  }

  private List<DataFlowNode> parseParameters(CallableDeclaration<?> cd) {
    return cd.getParameters().stream().map(n -> DataFlowNode.builder().javaParserNode(n).name(n.getNameAsString()).build()).collect(Collectors.toList());
  }

}
