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
package templateInput.definition;

import java.util.ArrayList;
import java.util.List;

/**
 * VariableDefinition receiving a flow
 *
 * @author Daan
 */
public class FlowReceiverDefinition extends VariableDefinition {

  private List<String> receivedValues = new ArrayList<>();

  public FlowReceiverDefinition() {
    // explicitly make constructor visible
  }

  protected FlowReceiverDefinition(Builder builder) {
    super(builder);
    this.receivedValues = builder.receivedValues;
  }

  public List<String> getReceivedValues() {
    return receivedValues;
  }

  /**
   * @return If the list of received values is not empty, get the first received value, otherwise get null as string.
   */
  public String getReceivedValue() {
    return receivedValues.isEmpty() ? "null" : receivedValues.get(0);
  }

  public String getAllReceivedValues() {
    return String.join("_", receivedValues);
  }

  public void setReceivedValues(List<String> receivedValues) {
    this.receivedValues = receivedValues;
  }

  /**
   * Creates builder to build {@link VariableDefinition}.
   *
   * @return created builder
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Builder to build {@link VariableDefinition}.
   */
  public static class Builder extends VariableDefinition.Builder<FlowReceiverDefinition.Builder> {
    private List<String> receivedValues;

    protected Builder() {
      super();
    }

    public Builder receivedValues(List<String> receivedValues) {
      this.receivedValues = receivedValues;
      return this;
    }

    public Builder copy(FlowReceiverDefinition field) {
      super.copy(field);
      this.receivedValues = field.getReceivedValues();
      return this;
    }

    @Override
    public FlowReceiverDefinition build() {
      return new FlowReceiverDefinition(this);
    }

  }

}
