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
import java.util.Arrays;
import java.util.List;

/**
 * Builder for {@link DataFlowNode}.
 *
 * @author User
 */
public class NodeBuilder {

  protected enum NodeType {
    IN_BETWEEN,
    METHOD_PARAMETER,
    CLASS_FIELD
  }

  private String method;
  private String name;
  private List<NodeBuilder> out = new ArrayList<>();
  private final NodeType type;
  private NodeBuilder root;

  public NodeBuilder(String name, NodeType type) {
    this.name = name;
    this.type = type;
  }

  /**
   * Create a {@link NodeBuilder} for given method and parameter.
   *
   * @param method Name of the method
   * @param name Name of the parameter
   * @return {@link NodeBuilder}
   */
  public static NodeBuilder ofParameter(String method, String name) {
    NodeBuilder builder = new NodeBuilder(name, NodeType.METHOD_PARAMETER);
    builder.method = method;
    return builder;
  }

  public static NodeBuilder ofField(String name) {
    NodeBuilder builder = new NodeBuilder(name, NodeType.CLASS_FIELD);

    return builder;
  }

  public NodeBuilder to(String name) {
    NodeBuilder next = new NodeBuilder(name, NodeType.IN_BETWEEN);
    next.setRoot(this);
    out.add(next);
    return next;
  }

  public void to(String... names) {
    Arrays.stream(names).forEach(this::to);
  }

  public NodeBuilder to(NodeBuilder next) {
    out.add(next);
    next.setRoot(this);
    return next;
  }

  public void to(NodeBuilder... names) {
    Arrays.stream(names).forEach(this::to);
  }

  private void setRoot(NodeBuilder root) {
    this.root = root.getRoot();
  }

  public NodeBuilder getRoot() {
    return this.root == null ? this : root;
  }

  public String getMethod() {
    return method;
  }

  public void setMethod(String method) {
    this.method = method;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<NodeBuilder> getOut() {
    return out;
  }

  public void setOut(List<NodeBuilder> out) {
    this.out = out;
  }

  public NodeType getType() {
    return type;
  }

  public DataFlowNode build() {
    return DataFlowNode.builder().name(name).build();
  }

}
