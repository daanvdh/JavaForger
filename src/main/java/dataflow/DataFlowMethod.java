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

import com.github.javaparser.ast.Node;

/**
 * DataFlow class representing a method inside a {@link DataFlowGraph}.
 *
 * @author Daan
 */
public class DataFlowMethod {

  /** The name of the method */
  private String name;
  /** The node which this method represents */
  private Node representedNode;
  /** The graph which this method is part of */
  private DataFlowGraph graph;
  /** The input parameters of the method */
  private List<DataFlowNode> inputParameters = new ArrayList<>();
  /** The fields of the class that are read inside this method */
  private List<DataFlowNode> inputFields = new ArrayList<>();
  /** The fields of the class that are written inside this method */
  private List<DataFlowNode> changedFields = new ArrayList<>();
  /** The methods that are called from within this method for which the return is read */
  private List<DataFlowMethod> inputMethods = new ArrayList<>();
  /** The methods that are called from within this method for which the return value is either void or ignored */
  private List<DataFlowMethod> outputMethods = new ArrayList<>();

  public DataFlowMethod(String name) {
    this.name = name;
  }

  public DataFlowMethod(DataFlowGraph graph, Node node, String name) {
    this(name);
    this.representedNode = node;
    this.graph = graph;
    graph.addMethod(this);
  }

  protected DataFlowMethod(Builder builder) {
    this.name = builder.name == null ? this.name : builder.name;
    this.inputParameters.clear();
    this.inputParameters.addAll(builder.inputParameters);
    this.inputFields.clear();
    this.inputFields.addAll(builder.inputFields);
    this.changedFields.clear();
    this.changedFields.addAll(builder.changedFields);
    this.inputMethods.clear();
    this.inputMethods.addAll(builder.inputMethods);
    this.outputMethods.clear();
    this.outputMethods.addAll(builder.outputMethods);
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Node getRepresentedNode() {
    return representedNode;
  }

  public void setRepresentedNode(Node representedNode) {
    this.representedNode = representedNode;
  }

  public List<DataFlowNode> getInputParameters() {
    return inputParameters;
  }

  public void setInputParameters(List<DataFlowNode> inputParameters) {
    this.inputParameters = inputParameters;
    this.graph.addNodes(inputParameters);
  }

  public List<DataFlowNode> getInputFields() {
    return inputFields;
  }

  public void setInputFields(List<DataFlowNode> inputFields) {
    this.inputFields = inputFields;
  }

  public List<DataFlowNode> getChangedFields() {
    return changedFields;
  }

  public void setChangedFields(List<DataFlowNode> changedFields) {
    this.changedFields = changedFields;
  }

  public List<DataFlowMethod> getInputMethods() {
    return inputMethods;
  }

  public void setInputMethods(List<DataFlowMethod> inputMethods) {
    this.inputMethods = inputMethods;
  }

  public List<DataFlowMethod> getOutputMethods() {
    return outputMethods;
  }

  public void setOutputMethods(List<DataFlowMethod> outputMethods) {
    this.outputMethods = outputMethods;
  }

  /**
   * @return List of {@link DataFlowMethod}s containing both the input and output methods.
   */
  public List<DataFlowMethod> getCalledMethods() {
    List<DataFlowMethod> methods = new ArrayList<>();
    methods.addAll(inputMethods);
    methods.addAll(outputMethods);
    return methods;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("method " + name + "{\n");
    sb.append("parameters{\n");
    for (DataFlowNode p : inputParameters) {
      sb.append(p.toStringForward(1, 1));
    }
    sb.append("\n}\n");

    sb.append("changedFields{\n");
    for (DataFlowNode p : changedFields) {
      sb.append(p.toStringForward(1, 1));
    }
    sb.append("\n}");
    sb.append("\n}");
    return sb.toString();
  }

  /**
   * Creates builder to build {@link DataFlowMethod}.
   *
   * @return created builder
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Builder to build {@link DataFlowMethod}.
   */
  public static class Builder {
    protected List<DataFlowNode> inputParameters = new ArrayList<>();
    protected List<DataFlowNode> inputFields = new ArrayList<>();
    protected List<DataFlowNode> changedFields = new ArrayList<>();
    protected List<DataFlowMethod> inputMethods = new ArrayList<>();
    protected List<DataFlowMethod> outputMethods = new ArrayList<>();
    protected String name;
    protected Node representedNode;

    protected Builder() {
      // Builder should only be constructed via the parent class
    }

    public Builder name(String name) {
      this.name = name;
      return this;
    }

    public Builder representedNode(Node representedNode) {
      this.representedNode = representedNode;
      return this;
    }

    public Builder inputParameters(List<DataFlowNode> inputParameters) {
      this.inputParameters.clear();
      this.inputParameters.addAll(inputParameters);
      return this;
    }

    public Builder inputFields(List<DataFlowNode> inputFields) {
      this.inputFields.clear();
      this.inputFields.addAll(inputFields);
      return this;
    }

    public Builder changedFields(List<DataFlowNode> changedFields) {
      this.changedFields.clear();
      this.changedFields.addAll(changedFields);
      return this;
    }

    public Builder inputMethods(List<DataFlowMethod> inputMethods) {
      this.inputMethods.clear();
      this.inputMethods.addAll(inputMethods);
      return this;
    }

    public Builder outputMethods(List<DataFlowMethod> outputMethods) {
      this.outputMethods.clear();
      this.outputMethods.addAll(outputMethods);
      return this;
    }

    public DataFlowMethod build() {
      return new DataFlowMethod(this);
    }

  }

}
