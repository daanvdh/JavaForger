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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Builder for {@link DataFlowGraph}.
 *
 * @author User
 */
public class GraphBuilder {

  private List<NodeBuilder> startNodes = new ArrayList<>();

  public static GraphBuilder withStartingNodes(NodeBuilder... nodes) {
    GraphBuilder graphBuilder = new GraphBuilder();
    Arrays.stream(nodes).map(NodeBuilder::getRoot).forEach(graphBuilder.startNodes::add);
    return graphBuilder;
  }

  public DataFlowGraph build() {
    DataFlowGraph graph = new DataFlowGraph();
    Map<String, DataFlowNode> nodes = new HashMap<>();
    Map<String, DataFlowMethod> methods = new HashMap<>();
    startNodes.forEach(node -> this.addNode(graph, node, nodes, methods));
    return graph;
  }

  private void addNode(DataFlowGraph graph, NodeBuilder nodeBuilder, Map<String, DataFlowNode> nodes, Map<String, DataFlowMethod> methods) {
    addNode(graph, nodeBuilder, nodes, methods, null, null);
  }

  /**
   * Recursively adds a new nodes to the given {@link DataFlowGraph}.
   *
   * @param graph The {@link DataFlowGraph} to add Nodes to.
   * @param nodeBuilder The next Node to add.
   * @param nodes Previously added nodes.
   * @param methods previously added methods.
   * @param previousNode The node that was added right before the given input node, can be null.
   * @param currentMethod The current method that is being added.
   */
  private void addNode(DataFlowGraph graph, NodeBuilder nodeBuilder, Map<String, DataFlowNode> nodes, Map<String, DataFlowMethod> methods,
      DataFlowNode previousNode, DataFlowMethod currentMethod) {

    DataFlowNode node = nodeBuilder.build();
    if (previousNode != null) {
      previousNode.addEdgeTo(node);
    }

    DataFlowMethod method = null;
    switch (nodeBuilder.getType()) {
    case CLASS_FIELD:
      graph.addField(node);
      if (currentMethod != null) {
        currentMethod.addChangedField(node);
      }
      break;
    case METHOD_PARAMETER:
      method = getOrCreateMethod(graph, methods, nodeBuilder.getMethod());
      // TODO if we want to influence the order of the parameters,
      // we need to create a NodeBuilder.ofParameter method with a parameter index as input and handle it here.
      method.addParameter(node);
      break;
    case IN_BETWEEN:
    default:
      // Do nothing
    }

    DataFlowMethod nextMethod = method == null ? currentMethod : method;
    nodeBuilder.getOut().forEach(nb -> addNode(graph, nb, nodes, methods, node, nextMethod));
  }

  private DataFlowMethod getOrCreateMethod(DataFlowGraph graph, Map<String, DataFlowMethod> methods, String methodName) {
    DataFlowMethod method;
    if (methods.containsKey(methodName)) {
      method = methods.get(methodName);
    } else {
      method = new DataFlowMethod(methodName);
      graph.addMethod(method);
    }
    return method;
  }

}
