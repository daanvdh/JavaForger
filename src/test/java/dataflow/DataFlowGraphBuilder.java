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

/**
 * Builder for {@link DataFlowGraph}.
 *
 * @author Daan
 */
public class DataFlowGraphBuilder {

  /**
   * Creates builder to build {@link TestDataFlowGraph}.
   *
   * @return created builder
   */
  public static DataFlowGraphBuilder builder() {
    return new DataFlowGraphBuilder();
  }

  private List<DataFlowNode> fields = new ArrayList<>();
  private List<DataFlowMethod> methods = new ArrayList<>();

  private DataFlowGraphBuilder() {
  }

  public DataFlowGraphBuilder fields(List<DataFlowNode> fields) {
    this.fields = fields;
    return this;
  }

  public DataFlowGraphBuilder methods(List<DataFlowMethod> methods) {
    this.methods = methods;
    return this;
  }

  public DataFlowGraph build() {
    DataFlowGraph graph = new DataFlowGraph();
    graph.setFields(fields);
    graph.setMethods(methods);
    return graph;
  }

  public DataFlowGraphBuilder withField(String name) {
    fields.add(new DataFlowNode(name));
    return this;
  }

  public DataFlowGraphBuilder withMethod(DataFlowMethod method) {
    this.methods.add(method);
    return this;
  }
}
