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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.CallableDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;

import dataflow.model.CallableGraph;
import dataflow.model.ClassGraph;
import dataflow.model.DataFlowNode;
import dataflow.model.DataFlowNodeType;

/**
 * Class for creating a dataFlowGraph from an AbstractSyntaxTree created by {@link JavaParser}.
 *
 * @author Daan
 */
public class DataFlowGraphGenerator {

  public ClassGraph createClassGraph(CompilationUnit cu) {
    ClassGraph g = new ClassGraph();
    g.setFields(createFields(cu));

    Map<Node, CallableGraph> existingCallables = new HashMap<>();
    Map<Node, DataFlowNode> fields = g.getFields().stream().collect(Collectors.toMap(DataFlowNode::getAstNode, Function.identity()));

    for (TypeDeclaration<?> type : cu.getTypes()) {
      for (Node node : type.getChildNodes()) {
        if (node instanceof MethodDeclaration) {
          g.addMethod(createMethod(node, existingCallables, fields));
        } else if (node instanceof ConstructorDeclaration) {
          g.addConstructor(createConstructor(node, existingCallables, fields));
        }
      }
    }
    return g;
  }

  private List<DataFlowNode> createFields(CompilationUnit cu) {
    List<DataFlowNode> nodes = new ArrayList<>();
    for (TypeDeclaration<?> type : cu.getTypes()) {
      for (Node node : type.getChildNodes()) {
        if (node instanceof FieldDeclaration) {
          nodes.add(createField(node));
        }
      }
    }
    return nodes;
  }

  private DataFlowNode createField(Node node) {
    return DataFlowNode.builder().type(DataFlowNodeType.CLASS_FIELD).astNode(node).build();
  }

  public CallableGraph createMethod(Node node, Map<Node, CallableGraph> existingCallables, Map<Node, DataFlowNode> fields) {
    MethodDeclaration md = (MethodDeclaration) node;
    CallableGraph c = createCallable(md, existingCallables, fields);
    return c;
  }

  public CallableGraph createConstructor(Node node, Map<Node, CallableGraph> existingCallables, Map<Node, DataFlowNode> fields) {
    ConstructorDeclaration cd = (ConstructorDeclaration) node;
    CallableGraph c = createCallable(cd, existingCallables, fields);
    return c;
  }

  public CallableGraph createCallable(CallableDeclaration<?> node, Map<Node, CallableGraph> existingCallables, Map<Node, DataFlowNode> fields) {
    CallableGraph c = existingCallables.containsKey(node) ? existingCallables.get(node) : new CallableGraph();
    c.setAstCallable(node);

    return c;
  }

}
