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
package dataflow;

import java.util.List;

/**
 * TODO javadoc
 *
 * @author Daan
 */
public class DataFlowGraph {

  private List<DataFlowNode> fields;
  private List<DataFlowMethod> constructors;
  private List<DataFlowMethod> methods;

  public List<DataFlowNode> getFields() {
    return fields;
  }

  public void setFields(List<DataFlowNode> fields) {
    this.fields = fields;
  }

  public List<DataFlowMethod> getConstructors() {
    return constructors;
  }

  public void setConstructors(List<DataFlowMethod> constructors) {
    this.constructors = constructors;
  }

  public List<DataFlowMethod> getMethods() {
    return methods;
  }

  public void setMethods(List<DataFlowMethod> methods) {
    this.methods = methods;
  }

  public void addMethod(DataFlowMethod parseMethod) {
    this.methods.add(parseMethod);
  }

}
