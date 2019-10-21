/*
 * Copyright 2018 by Daan van den Heuvel.
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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * Describes a method that is read from a java file by JavaParser.
 *
 * @author Daan
 */
public class MethodDefinition extends InitializedTypeDefinition {

  private List<VariableDefinition> parameters = new ArrayList<>();
  /** The fields that might be changed after this method call */
  private List<FlowReceiverDefinition> changedFields = new ArrayList<>();
  /** The methods called from this method, where the return value is used. */
  private List<MethodDefinition> inputMethods = new ArrayList<>();
  /** The methods called from this method, where the return value is not used. */
  private List<MethodDefinition> outputMethods = new ArrayList<>();
  /**
   * The complete signature for calling this method, e.g. product.setName(name) for Product::setName. If this {@link MethodDefinition} is constructed from a
   * method call inside another method, the input variables used might depend on other variables defined in the method, e.g. a parameter or a return value given
   * by another method. If this {@link MethodDefinition} was constructed as part of parsing a class, the methodSignature input variables will be filled with
   * random constants.
   */
  private String callSignature;
  /**
   * If this {@link MethodDefinition} represents a method call from inside a parsed method, this holds the name of the variable to which it was assigned. This
   * will be null otherwise.
   */
  private String returnSignature;
  /** The name of the variable that was expected as return value. */
  private String expectedReturn;

  private MethodDefinition(Builder builder) {
    super(builder);
    this.parameters = builder.parameters;
    this.changedFields = builder.changedFields == null ? this.changedFields : builder.changedFields;
    this.inputMethods = builder.inputMethods == null ? this.inputMethods : builder.inputMethods;
    this.outputMethods = builder.outputMethods == null ? this.outputMethods : builder.outputMethods;
    this.callSignature = builder.callSignature == null ? this.callSignature : builder.callSignature;
    this.returnSignature = builder.returnSignature == null ? this.returnSignature : builder.returnSignature;
  }

  public List<VariableDefinition> getParameters() {
    return parameters;
  }

  public void setParameters(List<VariableDefinition> parameters) {
    this.parameters = parameters;
  }

  public List<FlowReceiverDefinition> getChangedFields() {
    return changedFields;
  }

  public void setChangedFields(List<FlowReceiverDefinition> changedFields) {
    this.changedFields = changedFields;
  }

  public List<MethodDefinition> getInputMethods() {
    return inputMethods;
  }

  public void setInputMethods(List<MethodDefinition> inputMethods) {
    this.inputMethods = inputMethods;
  }

  public void addInputMethod(MethodDefinition inputMethod) {
    this.inputMethods.add(inputMethod);
  }

  public List<MethodDefinition> getOutputMethods() {
    return outputMethods;
  }

  public void setOutputMethods(List<MethodDefinition> outputMethods) {
    this.outputMethods = outputMethods;
  }

  public String getCallSignature() {
    return callSignature;
  }

  public void setCallSignature(String callSignature) {
    this.callSignature = callSignature;
  }

  public String getReturnSignature() {
    return returnSignature;
  }

  public void setReturnSignature(String returnSignature) {
    this.returnSignature = returnSignature;
  }

  public String getExpectedReturn() {
    return expectedReturn;
  }

  public void setExpectedReturn(String expectedReturn) {
    this.expectedReturn = expectedReturn;
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).appendSuper(super.toString())
        .append("parameters", "[" + parameters.stream().map(VariableDefinition::getNameAsString).collect(Collectors.joining(",")) + "]").build();
  }

  @Override
  public boolean equals(Object obj) {
    boolean equals = false;
    if (this == obj) {
      equals = true;
    } else if (obj != null && getClass() == obj.getClass()) {
      MethodDefinition other = (MethodDefinition) obj;
      equals = new EqualsBuilder().appendSuper(super.equals(other)).append(parameters, other.parameters).isEquals();
    }
    return equals;
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
  public static final class Builder extends InitializedTypeDefinition.Builder<MethodDefinition.Builder> {
    private List<VariableDefinition> parameters = new ArrayList<>();
    private List<FlowReceiverDefinition> changedFields = new ArrayList<>();
    private List<MethodDefinition> inputMethods = new ArrayList<>();
    private List<MethodDefinition> outputMethods = new ArrayList<>();
    private String callSignature;
    private String returnSignature;

    private Builder() {
    }

    public MethodDefinition build() {
      return new MethodDefinition(this);
    }

    public Builder parameters(List<VariableDefinition> parameters) {
      this.parameters = parameters;
      return this;
    }

    public Builder parameters(VariableDefinition... parameters) {
      this.parameters = Arrays.asList(parameters);
      return this;
    }

    public Builder changedFields(List<FlowReceiverDefinition> changedFields) {
      this.changedFields.clear();
      this.changedFields.addAll(changedFields);
      return this;
    }

    public Builder inputMethods(List<MethodDefinition> inputMethods) {
      this.inputMethods.clear();
      this.inputMethods.addAll(inputMethods);
      return this;
    }

    public Builder outputMethods(List<MethodDefinition> outputMethods) {
      this.outputMethods.clear();
      this.outputMethods.addAll(outputMethods);
      return this;
    }

    public Builder callSignature(String callSignature) {
      this.callSignature = callSignature;
      return this;
    }

    public Builder returnSignature(String returnSignature) {
      this.returnSignature = returnSignature;
      return this;
    }

  }

}
