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

/**
 * A graph representing a java callable such as a method or constructor.This class contains all nodes that are on the boundary of this method such as parameters
 * and return values. This class contains all edges that go over the boundary of this method such as method calls represented by an edge.
 *
 * @author Daan
 */
public class CallableGraph {

  private List<DataFlowNode> parameters = new ArrayList<>();
  private DataFlowNode returnValue;
  private List<DataFlowEdge> calls = new ArrayList<>();

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

  public List<DataFlowEdge> getCalls() {
    return calls;
  }

  public void setCalls(List<DataFlowEdge> calls) {
    this.calls = calls;
  }

}
