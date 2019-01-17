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
package dataflow.model;

import java.util.ArrayList;
import java.util.List;

/**
 * DataFlowGraph containing all entry points for a class.
 *
 * @author Daan
 */
public class ClassGraph {

  private List<DataFlowNode> fields = new ArrayList<>();
  private List<CallableGraph> constructors = new ArrayList<>();
  private List<CallableGraph> methods = new ArrayList<>();

  public List<DataFlowNode> getFields() {
    return fields;
  }

  public void setFields(List<DataFlowNode> fields) {
    this.fields = fields;
  }

  public List<CallableGraph> getConstructors() {
    return constructors;
  }

  public void setConstructors(List<CallableGraph> constructors) {
    this.constructors = constructors;
  }

  public List<CallableGraph> getMethods() {
    return methods;
  }

  public void setMethods(List<CallableGraph> methods) {
    this.methods = methods;
  }

  public void addField(DataFlowNode field) {
    fields.add(field);
  }

  public void addMethod(CallableGraph method) {
    this.methods.add(method);
  }

  public void addConstructor(CallableGraph constructor) {
    this.constructors.add(constructor);
  }

}
