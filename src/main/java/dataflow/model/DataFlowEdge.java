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

/**
 * This directed edge models a node influencing the data in another node. If the from node indirectly influences data, for instance via an if statement, this
 * edge will point to the actual edge that will or will not be executed based on the value of the 'from' node from this edge.
 *
 * @author Daan
 */
public class DataFlowEdge extends EdgeReceiver {

  /** The node an edge leaves from is always a node. */
  private DataFlowNode from;

  /** Where the edge points to can be a node or an edge. */
  private EdgeReceiver to;

  public DataFlowNode getFrom() {
    return from;
  }

  public void setFrom(DataFlowNode from) {
    this.from = from;
  }

  public EdgeReceiver getTo() {
    return to;
  }

  public void setTo(EdgeReceiver to) {
    this.to = to;
  }

}
