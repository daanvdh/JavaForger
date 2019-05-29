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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.javaparser.ast.Node;

/**
 * TODO javadoc
 *
 * @author Daan
 */
public class DataFlowGraph {

  private List<DataFlowNode> fields = new ArrayList<>();
  private List<DataFlowMethod> constructors = new ArrayList<>();
  private Map<Node, DataFlowMethod> methods = new HashMap<>();
  private Map<Node, DataFlowNode> nodes = new HashMap<>();

  public List<DataFlowNode> getFields() {
    return fields;
  }

  public void setFields(List<DataFlowNode> fields) {
    this.fields = fields;
  }

  public List<DataFlowMethod> getConstructors() {
    return constructors;
  }

  public void setConstructors(List<DataFlowMethod> constructors) {
    this.constructors = constructors;
  }

  public Collection<DataFlowMethod> getMethods() {
    return methods.values();
  }

  public void setMethods(List<DataFlowMethod> methods) {
    this.methods.clear();
    methods.forEach(this::addMethod);
  }

  public void addMethod(DataFlowMethod method) {
    this.methods.put(method.getRepresentedNode(), method);
  }

  public DataFlowMethod getMethod(Node node) {
    return methods.get(node);
  }

  public void addField(DataFlowNode node) {
    this.fields.add(node);
    this.nodes.put(node.getJavaParserNode(), node);
  }

  public void addNodes(List<DataFlowNode> nodes) {
    nodes.forEach(this::addNode);
  }

  public void addNode(DataFlowNode node) {
    this.nodes.put(node.getJavaParserNode(), node);
  }

  public DataFlowNode getNode(Node node) {
    return nodes.get(node);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();

    sb.append("fields{\n->");
    fields.forEach(f -> sb.append(f.toStringForward(1)));
    sb.append("\n<-");
    fields.forEach(f -> sb.append(f.toStringBackward(1)));
    sb.append("\n}\n");

    sb.append("methods{\n");
    for (DataFlowMethod m : methods.values()) {
      sb.append(m.toString());
    }
    sb.append("\n}");
    return sb.toString();
  }

}
