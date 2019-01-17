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
package dataflow.model;

import java.util.ArrayList;
import java.util.List;

import com.github.javaparser.ast.Node;

/**
 * A node containing data, such as class-fields, method parameters, return values, or locally defined variables.
 *
 * @author Daan
 */
public class DataFlowNode extends EdgeReceiver {

  private Node astNode;
  private List<DataFlowEdge> outgoing = new ArrayList<>();
  private DataFlowNodeType type;

  public Node getAstNode() {
    return astNode;
  }

  public void setAstNode(Node astNode) {
    this.astNode = astNode;
  }

  public List<DataFlowEdge> getOutgoing() {
    return outgoing;
  }

  public void setOutgoing(List<DataFlowEdge> outgoing) {
    this.outgoing = outgoing;
  }

  public DataFlowNodeType getType() {
    return type;
  }

  public void setType(DataFlowNodeType type) {
    this.type = type;
  }

}
