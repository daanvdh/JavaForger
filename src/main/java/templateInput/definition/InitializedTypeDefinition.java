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

import java.util.LinkedHashSet;
import java.util.List;

/**
 * Contains fields required to initialize a type inside a template.
 *
 * @author Daan
 */
public class InitializedTypeDefinition extends TypeDefinition {

  /** The default initialization for a field, especially used for initializing collections. Field will not be initialized when this is null. */
  protected String defaultInit;
  /** init1 and init2 hold two distinct initialization values. */
  protected String init1;
  protected String init2;
  /** Holds the value to be used when testing if a variable is not initialized. */
  protected String noInit = "null";
  /** True if the variable is a collection, false otherwise */
  protected boolean collection;
  /** The imports required for initializing this variable. */
  protected LinkedHashSet<String> initImports = new LinkedHashSet<>();

  public InitializedTypeDefinition() {
    // explicitly make constructor visible
  }

  /**
   * Copy constructor
   *
   * @param var
   */
  public InitializedTypeDefinition(InitializedTypeDefinition var) {
    super(var);
    this.defaultInit = var.defaultInit;
    this.init1 = var.init1;
    this.init2 = var.init2;
    this.noInit = var.noInit;
    this.collection = var.collection;
    this.initImports = var.initImports;
    this.typeImports = var.typeImports;
  }

  protected InitializedTypeDefinition(Builder<?> builder) {
    super(builder);
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

  public String getGetter() {
    String prefix = this.type.equals("boolean") ? "is" : "get";
    return prefix + getName().getUpperFirst();
  }

  public String getSetter() {
    return "set" + getName().getUpperFirst();
  }

  public String getTypeWithoutParameters() {
    int indexOf = type.indexOf("<");
    indexOf = indexOf < 1 ? type.length() : indexOf;
    String mainType = type.substring(0, indexOf);
    return mainType;
  }

  public LinkedHashSet<String> getInitImports() {
    return initImports;
  }

  public void setInitImports(LinkedHashSet<String> imports) {
    this.initImports = imports;
  }

  public void addInitImports(List<String> imports) {
    this.initImports.addAll(imports);
  }

  public void addInitImports(LinkedHashSet<String> imports) {
    this.initImports.addAll(imports);
  }

  /**
   * Builder to build {@link VariableDefinition}.
   *
   * @param <T> The class extending this builder
   */
  @SuppressWarnings("unchecked")
  public static class Builder<T extends Builder<?>> extends TypeDefinition.Builder<T> {
    private String init1;
    private String init2;
    private String noInit;

    protected Builder() {
    }

    public T withInit1(String init1) {
      this.init1 = init1;
      return (T) this;
    }

    public T withInit2(String init2) {
      this.init2 = init2;
      return (T) this;
    }

    public T withNoInit(String noInit) {
      this.noInit = noInit;
      return (T) this;
    }

  }

}
