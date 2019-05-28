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
import java.util.List;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.Node;

/**
 * A node inside the {@link DataFlowGraph} containing a {@link JavaParser} {@link Node}. The incoming {@link DataFlowEdge}s are {@link DataFlowNode}s that
 * influence the state of this node. The outgoing {@link DataFlowEdge}s point to {@link DataFlowNode}s which state is influenced by this {@link DataFlowNode}.
 *
 * @author Daan
 */
public class DataFlowNode {

  /** The name of this node */
  private String name;
  /** The {@link JavaParser} {@link Node} */
  private Node javaParserNode;
  /** The {@link DataFlowEdge}s from {@link DataFlowNode}s that influence the state of this node */
  private List<DataFlowEdge> in = new ArrayList<>();
  /** The {@link DataFlowEdge}s to {@link DataFlowNode}s who's state is influenced by this node */
  private List<DataFlowEdge> out = new ArrayList<>();

  public DataFlowNode(Node n) {
    this.javaParserNode = n;
  }

  public DataFlowNode(String name) {
    this.name = name;
  }

  private DataFlowNode(Builder builder) {
    this.javaParserNode = builder.javaParserNode == null ? this.javaParserNode : builder.javaParserNode;
    this.in.clear();
    this.in.addAll(builder.in);
    this.out.clear();
    this.out.addAll(builder.out);
    this.name = builder.name == null ? this.name : builder.name;
  }

  public Node getJavaParserNode() {
    return javaParserNode;
  }

  public void setJavaParserNode(Node javaParserNode) {
    this.javaParserNode = javaParserNode;
  }

  public List<DataFlowEdge> getIn() {
    return in;
  }

  public void setIn(List<DataFlowEdge> in) {
    this.in = in;
  }

  public List<DataFlowEdge> getOut() {
    return out;
  }

  public void setOut(List<DataFlowEdge> out) {
    this.out = out;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void addEdgeTo(DataFlowNode to) {
    DataFlowEdge edge = new DataFlowEdge(this, to);
    this.addOutgoing(edge);
    to.addIncoming(edge);
  }

  private void addIncoming(DataFlowEdge edge) {
    this.in.add(edge);
  }

  private void addOutgoing(DataFlowEdge edge) {
    this.out.add(edge);
  }

  @Override
  public String toString() {
    return javaParserNode.toString();
  }

  public String toStringForward(int tabs) {
    if (tabs > 10) {
      return "TestDataFlowNode::toStringForward tabs>10";
    }
    return toStringForward(tabs, 0);
  }

  public String toStringForward(int tabs, int firstTabs) {
    StringBuilder sb = new StringBuilder();
    sb.append(tabs(firstTabs) + this.getName());
    boolean first = true;

    for (DataFlowEdge e : out) {
      if (first) {
        first = false;
        sb.append("\t-> " + e.getTo().toStringForward(tabs + 1));
      } else {
        sb.append(tabs(tabs + 1) + "-> " + e.getTo().toStringForward(tabs + 1, tabs + 1));
      }
    }

    return sb.toString();
  }

  /**
   * Creates builder to build {@link DataFlowNode}.
   *
   * @return created builder
   */
  public static Builder builder() {
    return new Builder();
  }

  private String tabs(int tabs) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < tabs; i++) {
      sb.append("\t");
    }
    return sb.toString();
  }

  /**
   * Builder to build {@link DataFlowNode}.
   */
  public static final class Builder {
    private Node javaParserNode;
    private List<DataFlowEdge> in = new ArrayList<>();
    private List<DataFlowEdge> out = new ArrayList<>();
    private String name;

    private Builder() {
      // Builder should only be constructed via the parent class
    }

    public Builder javaParserNode(Node javaParserNode) {
      this.javaParserNode = javaParserNode;
      return this;
    }

    public Builder in(List<DataFlowEdge> in) {
      this.in.clear();
      this.in.addAll(in);
      return this;
    }

    public Builder out(List<DataFlowEdge> out) {
      this.out.clear();
      this.out.addAll(out);
      return this;
    }

    public Builder name(String name) {
      this.name = name;
      return this;
    }

    public DataFlowNode build() {
      return new DataFlowNode(this);
    }
  }

}