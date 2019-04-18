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

  private List<VariableDefinition> parameters;

  private MethodDefinition(Builder builder) {
    super(builder);
    this.parameters = builder.parameters;
  }

  public List<VariableDefinition> getParameters() {
    return parameters;
  }

  public void setParameters(List<VariableDefinition> parameters) {
    this.parameters = parameters;
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

    private Builder() {
    }

    public MethodDefinition build() {
      return new MethodDefinition(this);
    }

    public Builder withParameters(List<VariableDefinition> parameters) {
      this.parameters = parameters;
      return this;
    }

    public Builder withParameters(VariableDefinition... parameters) {
      this.parameters = Arrays.asList(parameters);
      return this;
    }

  }

}
