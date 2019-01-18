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

import com.github.javaparser.ast.body.CallableDeclaration;

/**
 * A graph representing a java callable such as a method or constructor.This class contains all nodes that are on the boundary of this method such as parameters
 * and return values. This class contains all edges that go over the boundary of this method such as method calls represented by an edge.
 *
 * @author Daan
 */
public class CallableGraph {

  private List<DataFlowNode> parameters = new ArrayList<>();
  private DataFlowNode returnValue;

  /** Contains all edges which start inside this callable and end outside this callable, examples: field-assignments, method-calls. */
  private List<DataFlowEdge> methodExits = new ArrayList<>();

  /** Contains all edges that start from a returnValue from another method, and end in a Node defined within this callable */
  private List<DataFlowEdge> methodEntries = new ArrayList<>();

  private CallableDeclaration<?> astCallable;

  public List<DataFlowEdge> getMethodEntries() {
    return methodEntries;
  }

  public void setMethodEntries(List<DataFlowEdge> methodEntries) {
    this.methodEntries = methodEntries;
  }

  public CallableDeclaration<?> getAstCallable() {
    return astCallable;
  }

  public void setAstCallable(CallableDeclaration<?> astCallable) {
    this.astCallable = astCallable;
  }

  public List<DataFlowNode> getParameters() {
    return parameters;
  }

  public void setParameters(List<DataFlowNode> parameters) {
    this.parameters = parameters;
  }

  public DataFlowNode getReturnValue() {
    return returnValue;
  }

  public void setReturnValue(DataFlowNode returnValue) {
    this.returnValue = returnValue;
  }

  public List<DataFlowEdge> getMethodExits() {
    return methodExits;
  }

  public void setMethodExits(List<DataFlowEdge> calls) {
    this.methodExits = calls;
  }

}
