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

import java.util.List;

/**
 * TODO javadoc
 *
 * @author Daan
 */
public class TestDataFlowNode {
  private String name;
  private List<TestDataFlowEdge> in;
  private List<TestDataFlowEdge> out;

  public TestDataFlowNode(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<TestDataFlowEdge> getIn() {
    return in;
  }

  public void setIn(List<TestDataFlowEdge> in) {
    this.in = in;
  }

  public List<TestDataFlowEdge> getOut() {
    return out;
  }

  public void setOut(List<TestDataFlowEdge> out) {
    this.out = out;
  }

  public void addEdgeTo(TestDataFlowNode to) {
    TestDataFlowEdge edge = new TestDataFlowEdge(this, to);
    this.addOutgoing(edge);
    to.addIncoming(edge);
  }

  private void addIncoming(TestDataFlowEdge edge) {
    this.out.add(edge);
  }

  private void addOutgoing(TestDataFlowEdge edge) {
    this.in.add(edge);
  }

}
