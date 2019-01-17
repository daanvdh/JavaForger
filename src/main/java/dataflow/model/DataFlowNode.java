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

  public DataFlowNode() {
    // empty constructor which would otherwise be invisible due to the constructor receiving the builder.
  }

  private DataFlowNode(Builder builder) {
    this.astNode = builder.astNode == null ? this.astNode : builder.astNode;
    this.outgoing = builder.outgoing == null ? this.outgoing : builder.outgoing;
    this.type = builder.type == null ? this.type : builder.type;
  }

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

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private Node astNode;
    private List<DataFlowEdge> outgoing = new ArrayList<>();
    private DataFlowNodeType type;

    private Builder() {
      // Builder should only be constructed via the parent class
    }

    public Builder astNode(Node astNode) {
      this.astNode = astNode;
      return this;
    }

    public Builder outgoing(List<DataFlowEdge> outgoing) {
      this.outgoing.clear();
      this.outgoing.addAll(outgoing);
      return this;
    }

    public Builder type(DataFlowNodeType type) {
      this.type = type;
      return this;
    }

    public DataFlowNode build() {
      return new DataFlowNode(this);
    }
  }

}
