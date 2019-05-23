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
 * TODO javadoc
 *
 * @author Daan
 */
public class TestDataFlowNode {
  private String name;
  private List<TestDataFlowEdge> in = new ArrayList<>();
  private List<TestDataFlowEdge> out = new ArrayList<>();

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
    this.in.add(edge);
  }

  private void addOutgoing(TestDataFlowEdge edge) {
    this.out.add(edge);
  }

  public String toStringForward() {
    return toStringForward(0);
  }

  public String toStringForward(int tabs) {
    if (tabs > 10) {
      return "TestDataFlowNode::toStringForward tabs>10";
    }

    return toStringForward(tabs, 0);
  }

  public String toStringForward(int tabs, int firstTabs) {
    StringBuilder sb = new StringBuilder();
    sb.append(tabs(firstTabs) + this.getName());
    boolean first = true;

    for (TestDataFlowEdge e : out) {
      if (first) {
        first = false;
        sb.append("\t-> " + e.getOut().toStringForward(tabs + 1));
      } else {
        sb.append(tabs(tabs + 1) + "-> " + e.getOut().toStringForward(tabs + 1, tabs + 1));
      }
    }

    return sb.toString();
  }

  private String tabs(int tabs) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < tabs; i++) {
      sb.append("\t");
    }
    return sb.toString();
  }

}
