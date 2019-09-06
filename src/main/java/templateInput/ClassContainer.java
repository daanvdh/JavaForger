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
import java.util.List;

import templateInput.definition.ClassDefinition;
import templateInput.definition.MethodDefinition;
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

  public List<? extends MethodDefinition> getConstructors() {
    return constructors;
  }

  public void setConstructors(List<MethodDefinition> constructors) {
    this.constructors = constructors;
  }

}
