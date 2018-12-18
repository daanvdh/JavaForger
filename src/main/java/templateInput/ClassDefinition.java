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
package templateInput;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * Describes a class. This includes attributes defined ON the class (className, implemented interfaces, annotations ...). It excludes attributes defined INSIDE
 * the class (fields, methods ...).
 *
 * @author Daan
 */
public class ClassDefinition extends TypeDefinition {

  private String extend;
  private List<String> interfaces;

  public ClassDefinition() {
    // public constructor to make it possible to extend this class.
  }

  protected ClassDefinition(Builder builder) {
    this.name = builder.name;
    this.type = builder.type;
    this.lineNumber = builder.lineNumber;
    this.column = builder.column;
    this.annotations = builder.annotations;
    this.accessModifiers = builder.accessModifiers;
    this.extend = builder.extend;
    this.interfaces = builder.interfaces;
  }

  public String getExtend() {
    return extend;
  }

  public void setExtend(String extend) {
    this.extend = extend;
  }

  public List<String> getInterfaces() {
    return interfaces;
  }

  public void setInterfaces(List<String> interfaces) {
    this.interfaces = interfaces;
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).appendSuper(super.toString()).append("extend", extend).append("interfaces", interfaces)
        .build();
  }

  @Override
  public boolean equals(Object obj) {
    boolean equals = false;
    if (this == obj) {
      equals = true;
    } else if (obj != null && getClass() == obj.getClass()) {
      ClassDefinition other = (ClassDefinition) obj;
      equals = new EqualsBuilder().appendSuper(super.equals(obj)).append(extend, other.extend).append(interfaces, other.interfaces).isEquals();
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
   * Creates builder to build {@link VariableDefinition} filled with data from the input {@link ClassDefinition}.
   *
   * @param copy The {@link ClassDefinition} to copy.
   * @return created builder
   */
  public static Builder builder(ClassDefinition copy) {
    return new Builder(copy);
  }

  /**
   * Builder to build {@link VariableDefinition}.
   */
  public static final class Builder {
    private String name;
    private String type;
    private int lineNumber;
    private int column;
    private Set<String> annotations = new HashSet<>();
    private Set<String> accessModifiers = new HashSet<>();
    private String extend;
    public List<String> interfaces;

    private Builder() {
    }

    private Builder(ClassDefinition copy) {
      this.name = copy.name;
      this.type = copy.type;
      this.lineNumber = copy.lineNumber;
      this.column = copy.column;
      this.annotations = copy.annotations;
      this.accessModifiers = copy.accessModifiers;
      this.extend = copy.extend;
      this.interfaces = copy.interfaces;
    }

    public Builder withName(String name) {
      this.name = name;
      return this;
    }

    public Builder withType(String type) {
      this.type = type;
      return this;
    }

    public Builder withLineNumber(int lineNumber) {
      this.lineNumber = lineNumber;
      return this;
    }

    public Builder withColumn(int column) {
      this.column = column;
      return this;
    }

    public Builder withAnnotations(Set<String> annotations) {
      this.annotations = annotations;
      return this;
    }

    public Builder withAccessModifiers(Set<String> accessModifiers) {
      this.accessModifiers = accessModifiers;
      return this;
    }

    public Builder withInterfaces(List<String> interfaces) {
      this.interfaces = interfaces;
      return this;
    }

    public Builder withExtend(String extend) {
      this.extend = extend;
      return this;
    }

    public ClassDefinition build() {
      return new ClassDefinition(this);
    }
  }

}
