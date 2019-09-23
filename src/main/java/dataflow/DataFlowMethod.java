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
import java.util.Objects;
import java.util.stream.Stream;

import org.apache.commons.lang3.builder.EqualsBuilder;

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
  /**
   * The return value of this method, null if this is a void method. Note that a method can have multiple return statements, we model as if a method only has a
   * single return type with as Node the Node of the whole method. Then all real return statements have an edge to the single return statement.
   */
  private DataFlowNode returnNode;
  /** The input parameters of the method */
  private List<DataFlowNode> inputParameters = new ArrayList<>();
  /** The fields of the class that are read inside this method */
  private List<DataFlowNode> inputFields = new ArrayList<>();
  /** The fields of the class that are written inside this method */
  private List<DataFlowNode> changedFields = new ArrayList<>();
  /** The methods that are called from within this method for which the return value is read. Key is the return value, value is the method */
  private Map<DataFlowNode, DataFlowMethod> inputMethods = new HashMap<>();
  /** The methods that are called from within this method for which the return value is either void or ignored */
  private List<DataFlowMethod> outputMethods = new ArrayList<>();

  public DataFlowMethod(String name, Node representedNode) {
    this.name = name;
    this.representedNode = representedNode;
  }

  public DataFlowMethod(DataFlowGraph graph, Node node, String name) {
    this(name, node);
    this.graph = graph;
    graph.addMethod(this);
  }

  protected DataFlowMethod(Builder builder) {
    this(builder.name, builder.representedNode);
    this.returnNode = builder.returnNode == null ? this.returnNode : builder.returnNode;
    this.inputParameters.clear();
    this.inputParameters.addAll(builder.inputParameters);
    this.inputFields.clear();
    this.inputFields.addAll(builder.inputFields);
    this.changedFields.clear();
    this.changedFields.addAll(builder.changedFields);
    this.inputMethods.clear();
    this.inputMethods.putAll(builder.inputMethods);
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

  public DataFlowNode getReturnNode() {
    return returnNode;
  }

  public void setReturnNode(DataFlowNode returnNode) {
    this.returnNode = returnNode;
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

  public Collection<DataFlowMethod> getInputMethods() {
    return inputMethods.values();
  }

  public void setInputMethods(Map<DataFlowNode, DataFlowMethod> inputMethods) {
    this.inputMethods = inputMethods;
  }

  public void addInputMethods(List<DataFlowMethod> inputMethods) {

    inputMethods.forEach(m -> this.inputMethods.put(m.getReturnNode(), m));
  }

  public List<DataFlowMethod> getOutputMethods() {
    return outputMethods;
  }

  public void setOutputMethods(List<DataFlowMethod> outputMethods) {
    this.outputMethods = outputMethods;
  }

  public void addParameter(DataFlowNode node) {
    this.inputParameters.add(node);
  }

  public void addChangedField(DataFlowNode node) {
    this.changedFields.add(node);
  }

  public void addChangedFields(DataFlowNode... fields) {
    Stream.of(fields).forEach(this::addChangedField);
  }

  /**
   * An input boundary node of a {@link DataFlowMethod} is a {@link DataFlowNode} representing an object that is defined outside the scope of the method and is
   * direct input to a method. A boundary node can be one of the following: 1) class field, 2) method parameter, 3) return value from a method called inside
   * this method.
   *
   * @param dfn The {@link DataFlowNode} to check if it is a boundary node.
   * @return True if the given {@link DataFlowNode} is an input boundary node, false otherwise.
   */
  public boolean isInputBoundaryNode(DataFlowNode dfn) {
    return this.inputFields.contains(dfn) || this.inputParameters.contains(dfn) || this.inputMethods.containsKey(dfn);
  }

  /**
   * An output boundary node of a {@link DataFlowMethod} is a {@link DataFlowNode} representing an object that is defined or changed inside the scope of the
   * method and can be used outside the scope of the method. An output boundary node can be one of the following: 1) a changed class field, 2) an input
   * parameter to a method called inside this method, 3) the method return value.
   *
   * @param dfn The {@link DataFlowNode} to check if it is an output boundary node.
   * @return True if the given {@link DataFlowNode} is an output boundary node, false otherwise.
   */
  public boolean isOutputBoundaryNode(DataFlowNode dfn) {
    return this.returnNode.equals(dfn) || this.changedFields.contains(dfn);
  }

  /**
   * @return List of {@link DataFlowMethod}s containing both the input and output methods.
   */
  public List<DataFlowMethod> getCalledMethods() {
    List<DataFlowMethod> methods = new ArrayList<>();
    methods.addAll(inputMethods.values());
    methods.addAll(outputMethods);
    return methods;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("method " + name + "{\n");
    sb.append("\tparameters{\n");
    for (DataFlowNode p : inputParameters) {
      sb.append(p.toStringForward(1, 2) + "\n");
    }
    sb.append("\t}\n");

    sb.append("\tchangedFields{\n");
    for (DataFlowNode p : changedFields) {
      sb.append(p.toStringForward(1, 2) + "\n");
    }
    sb.append("\t}");
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

  @Override
  public int hashCode() {
    return Objects.hash(name, representedNode, returnNode, inputParameters, inputFields, changedFields, inputMethods, outputMethods);
  }

  @Override
  public boolean equals(Object obj) {
    boolean equals = false;
    if (this == obj) {
      equals = true;
    } else if (obj != null && getClass() == obj.getClass()) {
      DataFlowMethod other = (DataFlowMethod) obj;
      equals = new EqualsBuilder().append(name, other.name).append(representedNode, other.representedNode).append(returnNode, other.returnNode)
          .append(inputParameters, other.inputParameters).append(inputFields, other.inputFields).append(changedFields, other.changedFields)
          .append(inputMethods, other.inputMethods).append(outputMethods, other.outputMethods).isEquals();
    }
    return equals;
  }

  /**
   * Builder to build {@link DataFlowMethod}.
   */
  public static class Builder {
    protected List<DataFlowNode> inputParameters = new ArrayList<>();
    protected List<DataFlowNode> inputFields = new ArrayList<>();
    protected List<DataFlowNode> changedFields = new ArrayList<>();
    protected Map<DataFlowNode, DataFlowMethod> inputMethods = new HashMap<>();
    protected List<DataFlowMethod> outputMethods = new ArrayList<>();
    protected String name;
    protected Node representedNode;
    private DataFlowNode returnNode;

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

    public Builder returnNode(DataFlowNode returnNode) {
      this.returnNode = returnNode;
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

    public Builder inputMethods(Map<DataFlowNode, DataFlowMethod> inputMethods) {
      this.inputMethods.clear();
      this.inputMethods.putAll(inputMethods);
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
