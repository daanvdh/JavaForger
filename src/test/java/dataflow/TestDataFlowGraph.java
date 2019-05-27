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
public class TestDataFlowGraph {

  private List<TestDataFlowNode> fields;
  private List<TestDataFlowMethod> methods;

  private TestDataFlowGraph(Builder builder) {
    this.fields = builder.fields;
    this.methods = builder.methods;
  }

  public List<TestDataFlowNode> getFields() {
    return fields;
  }

  public void setFields(List<TestDataFlowNode> fields) {
    this.fields = fields;
  }

  public List<TestDataFlowMethod> getMethods() {
    return methods;
  }

  public void setMethods(List<TestDataFlowMethod> methods) {
    this.methods = methods;
  }

  /**
   * Creates builder to build {@link TestDataFlowGraph}.
   *
   * @return created builder
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Builder to build {@link TestDataFlowGraph}.
   */
  public static final class Builder {
    private List<TestDataFlowNode> fields = new ArrayList<>();
    private List<TestDataFlowMethod> methods = new ArrayList<>();

    private Builder() {
    }

    public Builder fields(List<TestDataFlowNode> fields) {
      this.fields = fields;
      return this;
    }

    public Builder methods(List<TestDataFlowMethod> methods) {
      this.methods = methods;
      return this;
    }

    public TestDataFlowGraph build() {
      return new TestDataFlowGraph(this);
    }

    public Builder withField(String name) {
      fields.add(new TestDataFlowNode(name));
      return this;
    }

    public Builder withMethod(TestDataFlowMethod method) {
      this.methods.add(method);
      return this;
    }
  }

  public boolean equalsGraph(DataFlowGraph graph) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();

    sb.append("fields{\n");
    for (TestDataFlowNode f : fields) {
      sb.append(f.toStringForward());
    }
    sb.append("\n}\n");

    sb.append("methods{\n");
    for (TestDataFlowMethod m : methods) {
      sb.append(m.toString());
    }
    sb.append("\n}");
    return sb.toString();
  }

}
