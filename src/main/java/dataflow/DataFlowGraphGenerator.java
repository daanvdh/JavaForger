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

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
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
    for (TypeDeclaration<?> type : cu.getTypes()) {
      for (Node node : type.getChildNodes()) {
        if (node instanceof FieldDeclaration) {
          g.addField(createField(node));
        } else if (node instanceof MethodDeclaration) {
          g.addMethod(createMethod(node));
        } else if (node instanceof ConstructorDeclaration) {
          g.addConstructor(createConstructor(node));
        }
      }
    }
    return g;
  }

  private DataFlowNode createField(Node node) {
    DataFlowNode n = new DataFlowNode();
    n.setType(DataFlowNodeType.CLASS_FIELD);
    n.setAstNode(node);

    // TODO Auto-generated method stub
    return n;
  }

  private CallableGraph createMethod(Node node) {
    CallableGraph c = new CallableGraph();
    // TODO Auto-generated method stub
    return c;
  }

  private CallableGraph createConstructor(Node node) {
    CallableGraph c = new CallableGraph();
    // TODO Auto-generated method stub
    return c;
  }

}
