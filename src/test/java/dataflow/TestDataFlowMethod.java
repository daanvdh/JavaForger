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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TODO javadoc
 *
 * @author Daan
 */
public class TestDataFlowMethod {

  private List<TestDataFlowNode> parameters;
  private List<TestDataFlowNode> changedFields;
  private List<TestDataFlowNode> inputFields;

  private TestDataFlowMethod(Builder builder) {
    this.parameters = builder.parameters;
    this.changedFields = builder.changedFields;
    this.inputFields = builder.inputFields;
  }

  public List<TestDataFlowNode> getParameters() {
    return parameters;
  }

  public void setParameters(List<TestDataFlowNode> parameters) {
    this.parameters = parameters;
  }

  public List<TestDataFlowNode> getChangedFields() {
    return changedFields;
  }

  public void setChangedFields(List<TestDataFlowNode> changedFields) {
    this.changedFields = changedFields;
  }

  /**
   * Creates builder to build {@link TestDataFlowMethod}.
   *
   * @return created builder
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Builder to build {@link TestDataFlowMethod}.
   */
  public static final class Builder {
    private List<TestDataFlowNode> parameters = new ArrayList<>();
    private List<TestDataFlowNode> changedFields = new ArrayList<>();
    private List<TestDataFlowNode> inputFields = new ArrayList<>();

    private Map<String, TestDataFlowNode> currentNodes = new HashMap<>();

    private Builder() {
    }

    public TestDataFlowMethod build() {
      return new TestDataFlowMethod(this);
    }

    public Builder withParameter(String name) {
      parameters.add(getNode(name));
      return this;
    }

    public Builder withInputField(String name) {
      this.inputFields.add(new TestDataFlowNode(name));
      return this;
    }

    public Builder withChangedFieldEdge(String input, String changedField) {
      TestDataFlowNode a = getNode(input);
      TestDataFlowNode b = getNode("this." + changedField);
      a.addEdgeTo(b);
      this.changedFields.add(b);
      return this;
    }

    private TestDataFlowNode getNode(String name) {
      if (!this.currentNodes.containsKey(name)) {
        TestDataFlowNode node = new TestDataFlowNode(name);
        this.currentNodes.put(name, node);
      }
      return currentNodes.get(name);
    }
  }

}
