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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.commons.lang3.builder.EqualsBuilder;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.CallableDeclaration;

/**
 * DataFlow class representing a method inside a {@link DataFlowGraph}.
 *
 * @author Daan
 */
public class DataFlowMethod extends OwnedNode {

  /** All nodes defined within this method. This method should be an (indirect) owner for each of these nodes. */
  private Map<Node, DataFlowNode> nodes = new HashMap<>();
  /** The node which this method represents */
  // TODO remove this field
  private CallableDeclaration<?> representedNode;
  /** The graph which this method is part of */
  private DataFlowGraph graph;
  /**
   * The return value of this method, null if this is a void method. Note that a method can have multiple return statements, we model as if a method only has a
   * single return type with as Node the Node of the whole method. Then all real return statements have an edge to the single return statement.
   */
  private DataFlowNode returnNode;
  /** The input parameters of the method */
  private ParameterList inputParameters;

  private List<NodeCall> calledMethods = new ArrayList<>();

  /** The fields of the class that are read inside this method */
  // TODO Should probably be removed since it's a derivative
  private List<DataFlowNode> inputFields = new ArrayList<>();
  /** The fields of the class that are written inside this method */
  // TODO Should probably be removed since it's a derivative
  private List<DataFlowNode> changedFields = new ArrayList<>();
  /** The methods that are called from within this method for which the return value is read. Key is the return value, value is the method */
  // TODO Should probably be removed since it's a derivative
  private Map<DataFlowNode, DataFlowMethod> inputMethods = new HashMap<>();
  /** The methods that are called from within this method for which the return value is either void or ignored */
  // TODO Should probably be removed since it's a derivative
  private Map<DataFlowNode, DataFlowMethod> outputMethods = new HashMap<>();

  public DataFlowMethod(String name, CallableDeclaration<?> representedNode) {
    super(name, representedNode);
    this.representedNode = representedNode;
  }

  public DataFlowMethod(DataFlowGraph graph, CallableDeclaration<?> node, String name) {
    this(name, node);
    this.graph = graph;
    graph.addMethod(this);
  }

  protected DataFlowMethod(Builder builder) {
    super(builder);
    this.returnNode = builder.returnNode == null ? this.returnNode : builder.returnNode;
    this.inputParameters = builder.inputParameters == null ? this.inputParameters : builder.inputParameters;
    this.inputFields.clear();
    this.inputFields.addAll(builder.inputFields);
    this.changedFields.clear();
    this.changedFields.addAll(builder.changedFields);
    this.inputMethods.clear();
    this.inputMethods.putAll(builder.inputMethods);
    this.outputMethods.clear();
    this.outputMethods.putAll(builder.outputMethods);
    this.graph = builder.graph;
  }

  @Override
  public CallableDeclaration<?> getRepresentedNode() {
    return representedNode;
  }

  public void setRepresentedNode(CallableDeclaration<?> representedNode) {
    this.representedNode = representedNode;
  }

  public Optional<DataFlowNode> getReturnNode() {
    return Optional.ofNullable(returnNode);
  }

  public void setReturnNode(DataFlowNode returnNode) {
    this.returnNode = returnNode;
    returnNode.setOwner(this);
    this.addNode(returnNode);
  }

  public ParameterList getInputParameters() {
    return inputParameters;
  }

  public void setInputParameters(List<DataFlowNode> inputParameters) {
    this.inputParameters = new ParameterList(inputParameters, this);
    this.addNodes(inputParameters);
  }

  public void setInputParameters(ParameterList inputParameters) {
    this.inputParameters = inputParameters;
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

  public Collection<DataFlowMethod> getOutputMethods() {
    return outputMethods.values();
  }

  public void setOutputMethods(Map<DataFlowNode, DataFlowMethod> outputMethods) {
    this.outputMethods = outputMethods;
  }

  @Override
  public Optional<OwnedNode> getOwner() {
    return Optional.ofNullable(this.graph);
  }

  public DataFlowGraph getGraph() {
    return graph;
  }

  public void setGraph(DataFlowGraph graph) {
    this.graph = graph;
  }

  public Collection<DataFlowNode> getNodes() {
    return nodes.values();
  }

  public void setNodes(Map<Node, DataFlowNode> nodes) {
    this.nodes = nodes;
  }

  public void addNodes(List<DataFlowNode> inputParameters) {
    inputParameters.forEach(this::addNode);
  }

  public void addNode(DataFlowNode created) {
    this.nodes.put(created.getRepresentedNode(), created);
  }

  public DataFlowNode createAndAddNode(String name, Node n) {
    DataFlowNode dfn = new DataFlowNode(name, n);
    dfn.setOwner(this);
    this.nodes.put(n, dfn);
    return dfn;
  }

  public DataFlowNode getNode(Node node) {
    return nodes.get(node);
  }

  public void addParameter(DataFlowNode node) {
    this.inputParameters.add(node);
    this.addNode(node);
  }

  public void addChangedField(DataFlowNode node) {
    this.changedFields.add(node);
    this.addNode(node);
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
  public List<NodeCall> getCalledMethods() {
    return this.calledMethods;
  }

  public void setCalledMethods(List<NodeCall> calledMethods) {
    this.calledMethods = calledMethods;
  }

  public void addMethodCall(NodeCall calledMethod) {
    this.calledMethods.add(calledMethod);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("method " + super.getName() + "{\n");

    sb.append("\tparameters{\n");
    for (DataFlowNode p : inputParameters.getParameters()) {
      sb.append(p.toStringForward(1, 2) + "\n");
    }
    sb.append("\t}\n");

    sb.append("\tchangedFields{\n");
    for (DataFlowNode p : changedFields) {
      sb.append(p.toStringForward(1, 2) + "\n");
    }
    sb.append("\t}\n");

    sb.append("\tnodes{\n");
    for (DataFlowNode p : nodes.values()) {
      sb.append("\t\t" + p.toString() + "\n");
    }
    sb.append("\t}\n");

    sb.append("\treturn " + (this.returnNode == null ? "null" : this.returnNode.getName()) + "\n");
    sb.append("}");
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
    return Objects.hash(super.hashCode(), returnNode, inputParameters, inputFields, changedFields, inputMethods, outputMethods);
  }

  @Override
  public boolean equals(Object obj) {
    boolean equals = false;
    if (this == obj) {
      equals = true;
    } else if (obj != null && getClass() == obj.getClass()) {
      DataFlowMethod other = (DataFlowMethod) obj;
      equals = new EqualsBuilder().appendSuper(super.equals(obj)).append(representedNode, other.representedNode).append(returnNode, other.returnNode)
          .append(inputParameters, other.inputParameters).append(inputFields, other.inputFields).append(changedFields, other.changedFields)
          .append(inputMethods, other.inputMethods).append(outputMethods, other.outputMethods).append(graph, other.graph).isEquals();
    }
    return equals;
  }

  /**
   * Builder to build {@link DataFlowMethod}.
   */
  public static class Builder extends NodeRepresenter.Builder<DataFlowMethod.Builder> {
    protected ParameterList inputParameters;
    protected List<DataFlowNode> inputFields = new ArrayList<>();
    protected List<DataFlowNode> changedFields = new ArrayList<>();
    protected Map<DataFlowNode, DataFlowMethod> inputMethods = new HashMap<>();
    protected Map<DataFlowNode, DataFlowMethod> outputMethods = new HashMap<>();
    private DataFlowNode returnNode;
    private DataFlowGraph graph;

    protected Builder() {
      // Builder should only be constructed via the parent class
    }

    public Builder returnNode(DataFlowNode returnNode) {
      this.returnNode = returnNode;
      return this;
    }

    public Builder inputParameters(ParameterList inputParameters) {
      this.inputParameters = inputParameters;
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

    public Builder outputMethods(Map<DataFlowNode, DataFlowMethod> outputMethods) {
      this.outputMethods.clear();
      this.outputMethods.putAll(outputMethods);
      return this;
    }

    public Builder graph(DataFlowGraph graph) {
      this.graph = graph;
      return this;
    }

    public DataFlowMethod build() {
      return new DataFlowMethod(this);
    }

  }

}
