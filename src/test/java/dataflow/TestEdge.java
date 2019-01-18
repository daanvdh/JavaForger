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

import dataflow.model.CallableGraph;

/**
 * Contains data that is needed for verification of {@link CallableGraph}
 *
 * @author Daan
 */
public class TestEdge extends TestEdgeReceiver {

  /** The node an edge leaves from is always a node. */
  private TestNode from;

  /** Where the edge points to can be a node or an edge. */
  private TestEdgeReceiver to;

  /** only used for knowing which edge this is, not mend for verification */
  private String name;

  public TestEdge(TestNode from, TestEdgeReceiver to) {
    super(from.getName() + "To" + to.getName());
    this.from = from;
    this.to = to;
    from.addOutgoing(this);
    to.addIncoming(this);
  }

  public TestNode getFrom() {
    return from;
  }

  public void setFrom(TestNode from) {
    this.from = from;
  }

  public TestEdgeReceiver getTo() {
    return to;
  }

  public void setTo(TestEdgeReceiver to) {
    this.to = to;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void setName(String name) {
    this.name = name;
  }

}
