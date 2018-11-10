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
import java.util.Set;

/**
 * This represents the definition of a variable inside a java class.
 *
 * @author Daan
 */
public class VariableDefinition extends TypeDefinition {

  // TODO variables below should be moved to new class extending this one. VariableDefinitionInitialization
  // For generating tests we need to be able to initialize the variable defined.
  /** The default initialization for a field, especially used for initializing collections. Field will not be initialized when this is null. */
  private String defaultInit;
  /** init1 and init2 hold two distinct initialization values. */
  private String init1;
  private String init2;
  /** Holds the value to be used when testing if a variable is not initialized. */
  private String noInit = "null";
  /** True if the variable is a collection, false otherwise */
  private boolean collection;

  public VariableDefinition() {
    // explicitly make constructor visible
  }

  private VariableDefinition(Builder builder) {
    this.name = builder.name;
    this.type = builder.type;
    this.lineNumber = builder.lineNumber;
    this.column = builder.column;
    this.annotations = builder.annotations;
    this.accessModifiers = builder.accessModifiers;
    this.init1 = builder.init1;
    this.init2 = builder.init2;
    this.noInit = builder.noInit;
  }

  public boolean isCollection() {
    return collection;
  }

  public void setCollection(boolean collection) {
    this.collection = collection;
  }

  public String getDefaultInit() {
    return defaultInit;
  }

  public void setDefaultInit(String defaultInit) {
    this.defaultInit = defaultInit;
  }

  public String getNoInit() {
    return noInit;
  }

  public void setNoInit(String noInit) {
    this.noInit = noInit;
  }

  // TODO this method should be removed, it is to customly build for the filter in the VariableInitializer.
  public boolean isStatic() {
    return accessModifiers.contains("static");
  }

  public String getInit1() {
    return init1;
  }

  public void setInit1(String init1) {
    this.init1 = init1;
  }

  public String getInit2() {
    return init2;
  }

  public void setInit2(String init2) {
    this.init2 = init2;
  }

  public String getConstantName() {
    String regex = "([A-Z])";
    String replacement = "_$1";
    return name.replaceAll(regex, replacement).toUpperCase();
  }

  public String getGetter() {
    String prefix = this.type.equals("boolean") ? "is" : "get";
    return prefix + getUpperName();
  }

  public String getUpperName() {
    return upperCaseFirstChar(name);
  }

  private String upperCaseFirstChar(String s) {
    char[] c = s.toCharArray();
    c[0] = Character.toUpperCase(c[0]);
    return new String(c);
  }

  public String getTypeWithoutParameters() {
    int indexOf = type.indexOf("<");
    indexOf = indexOf < 1 ? type.length() : indexOf;
    String mainType = type.substring(0, indexOf);
    return mainType;
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
  public static final class Builder {
    private String name;
    private String type;
    private int lineNumber;
    private int column;
    private Set<String> annotations = new HashSet<>();
    private Set<String> accessModifiers = new HashSet<>();
    private String init1;
    private String init2;
    private String noInit;

    private Builder() {
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

    public Builder withInit1(String init1) {
      this.init1 = init1;
      return this;
    }

    public Builder withInit2(String init2) {
      this.init2 = init2;
      return this;
    }

    public Builder withNoInit(String noInit) {
      this.noInit = noInit;
      return this;
    }

    public VariableDefinition build() {
      return new VariableDefinition(this);
    }
  }

}
