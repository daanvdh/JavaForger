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

import java.util.LinkedHashMap;

import dataflow.model.CallableGraph;

/**
 * Contains data for verification of {@link CallableGraph}
 *
 * @author Daan
 */
public class TestCallableGraph {

  /** The names and parameters */
  private LinkedHashMap<String, TestNode> parameters = new LinkedHashMap<>();

  /** The return value of the method */
  private TestNode returnValue = new TestNode("return");

  /** The names and exiting edges, names are only used to setup the graph, not mend for validation */
  private LinkedHashMap<String, TestEdge> methodExits = new LinkedHashMap<>();

  /** The name of the callable (method or constructor) */
  private String name;

  public TestCallableGraph() {
    // empty constructor which would otherwise be invisible due to the constructor receiving the builder.
  }

  private TestCallableGraph(Builder builder) {
    this.parameters = builder.parameters == null ? this.parameters : builder.parameters;
    this.methodExits = builder.methodExits == null ? this.methodExits : builder.methodExits;
    this.name = builder.name == null ? this.name : builder.name;
    this.returnValue = builder.returnValue;
  }

  public LinkedHashMap<String, TestNode> getParameters() {
    return parameters;
  }

  public void setParameters(LinkedHashMap<String, TestNode> parameters) {
    this.parameters = parameters;
  }

  public TestNode getReturnValue() {
    return returnValue;
  }

  public void setReturnValue(TestNode returnValue) {
    this.returnValue = returnValue;
  }

  public LinkedHashMap<String, TestEdge> getMethodExits() {
    return methodExits;
  }

  public void setMethodExits(LinkedHashMap<String, TestEdge> methodExits) {
    this.methodExits = methodExits;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private LinkedHashMap<String, TestNode> parameters = new LinkedHashMap<>();
    private LinkedHashMap<String, TestEdge> methodExits = new LinkedHashMap<>();
    private String name;
    private TestNode returnValue = new TestNode("return");

    private LinkedHashMap<String, TestNode> existingNodes = new LinkedHashMap<>();
    private LinkedHashMap<String, TestEdge> existingEdges = new LinkedHashMap<>();

    private Builder() {
      // Builder should only be constructed via the parent class
    }

    public Builder parameters(LinkedHashMap<String, TestNode> parameters) {
      this.parameters = parameters;
      return this;
    }

    public Builder methodExits(LinkedHashMap<String, TestEdge> methodExits) {
      this.methodExits = methodExits;
      return this;
    }

    public Builder name(String name) {
      this.name = name;
      return this;
    }

    public Builder withParameter(String name) {
      TestNode node = new TestNode(name);
      this.parameters.put(name, node);
      this.existingNodes.put(name, node);
      return this;
    }

    public TestCallableGraph build() {
      return new TestCallableGraph(this);
    }

    public Builder withReturn(String from) {
      TestEdge edge = getNode(from).addEdgeTo(this.returnValue);
      existingEdges.put(edge.getName(), edge);
      return this;
    }

    private TestNode getNode(String name) {
      TestNode node = new TestNode(name);
      if (this.parameters.containsKey(name)) {
        node = parameters.get(name);
      } else if (this.existingNodes.containsKey(name)) {
        node = existingNodes.get(name);
      } else {
        existingNodes.put(name, node);
      }
      return node;
    }

    public Builder withExitingEdge(String from, String to) {
      TestEdge edge = new TestEdge(getNode(from), getNode(to));
      this.existingEdges.put(edge.getName(), edge);
      return this;
    }

  }

}
