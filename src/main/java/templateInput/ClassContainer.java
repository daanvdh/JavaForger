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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import templateInput.definition.ClassDefinition;
import templateInput.definition.MethodDefinition;
import templateInput.definition.TypeDefinition;
import templateInput.definition.VariableDefinition;

/**
 * Container class for everything defined within a class.
 *
 * @author Daan
 */
public class ClassContainer extends ClassDefinition {

  /** The fields defined within the class */
  private List<? extends VariableDefinition> fields = new ArrayList<>();
  /** The methods defined within the class */
  private List<? extends MethodDefinition> methods = new ArrayList<>();
  /** The constructors defined within the class */
  private List<? extends MethodDefinition> constructors = new ArrayList<>();

  public ClassContainer(ClassDefinition def) {
    super(builder(def));
  }

  public ClassContainer() {
    // empty constructor so that everything can be filled in later.
  }

  public List<? extends VariableDefinition> getFields() {
    return fields;
  }

  public void setFields(List<? extends VariableDefinition> fields) {
    this.fields = fields;
  }

  public List<? extends MethodDefinition> getMethods() {
    return methods;
  }

  public void setMethods(List<? extends MethodDefinition> methods) {
    this.methods = methods;
  }

  public List<? extends MethodDefinition> getGetters() {
    return methods.stream().filter(this::isGetter).collect(Collectors.toList());
  }

  public List<? extends MethodDefinition> getSetters() {
    return methods.stream().filter(this::isSetter).collect(Collectors.toList());
  }

  public List<? extends MethodDefinition> getConstructors() {
    return constructors;
  }

  public void setConstructors(List<MethodDefinition> constructors) {
    this.constructors = constructors;
  }

  public List<String> getFieldImports() {
    return getTypeImports(fields);
  }

  public List<String> getMethodImports() {
    return getTypeImports(methods);
  }

  public Set<String> getImports() {
    Set<String> set = new HashSet<>();
    set.addAll(getMethodImports());
    set.addAll(getFieldImports());
    return set;
  }

  private List<String> getTypeImports(final List<? extends TypeDefinition> methods2) {
    return methods2.stream().map(TypeDefinition::getTypeImports).flatMap(Collection::stream).distinct().collect(Collectors.toList());
  }

  private boolean isGetter(MethodDefinition m) {
    String n = m.getName().toString();
    return n.startsWith("get") || n.startsWith("is");
  }

  private boolean isSetter(MethodDefinition m) {
    return m.getName().toString().startsWith("set");
  }

}
