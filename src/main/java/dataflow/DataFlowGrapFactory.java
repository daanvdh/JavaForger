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

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;

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
    return null;
  }

}
