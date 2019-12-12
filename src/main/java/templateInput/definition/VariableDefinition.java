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

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * This represents the definition of a variable inside a java class.
 *
 * @author Daan
 */
public class VariableDefinition extends InitializedTypeDefinition {

  /** The original assignment to this variable */
  private String originalInit;

  public VariableDefinition() {
    // explicitly make constructor visible
  }

  /**
   * Copy constructor
   *
   * @param var
   */
  public VariableDefinition(VariableDefinition var) {
    super(var);
  }

  protected VariableDefinition(Builder<?> builder) {
    super(builder);
    this.originalInit = builder.originalInit;
  }

  /**
   * @return {@link VariableDefinition#originalInit}
   */
  public String getOriginalInit() {
    return originalInit;
  }

  /**
   * Sets the {@link VariableDefinition#originalInit}
   *
   * @param originalInit
   */
  public void setOriginalInit(String originalInit) {
    this.originalInit = originalInit;
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).appendSuper(super.toString()).append("originalInit", originalInit).build();
  }

  @Override
  public boolean equals(Object obj) {
    boolean equals = false;
    if (this == obj) {
      equals = true;
    } else if (obj != null && getClass() == obj.getClass()) {
      VariableDefinition other = (VariableDefinition) obj;
      equals = new EqualsBuilder().appendSuper(super.equals(obj)).append(originalInit, other.originalInit).isEquals();
    }
    return equals;
  }

  /**
   * Creates builder to build {@link VariableDefinition}.
   *
   * @return created builder
   */
  public static Builder<?> builder() {
    return new Builder<>();
  }

  /**
   * Builder to build {@link VariableDefinition}.
   */
  @SuppressWarnings("unchecked")
  public static class Builder<T extends VariableDefinition.Builder<?>> extends InitializedTypeDefinition.Builder<T> {
    private String originalInit;

    protected Builder() {
      super();
    }

    public T originalInit(String originalInit) {
      this.originalInit = originalInit;
      return (T) this;
    }

    public T copy(VariableDefinition field) {
      super.copy(field);
      this.originalInit = field.getOriginalInit();
      return (T) this;
    }

    public VariableDefinition build() {
      return new VariableDefinition(this);
    }

  }

}
