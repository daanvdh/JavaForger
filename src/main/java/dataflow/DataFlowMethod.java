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
public class DataFlowMethod {

  private List<DataFlowNode> inputParameters;
  private List<DataFlowNode> changedFields;

  public List<DataFlowNode> getInputParameters() {
    return inputParameters;
  }

  public void setInputParameters(List<DataFlowNode> inputParameters) {
    this.inputParameters = inputParameters;
  }

  public List<DataFlowNode> getChangedFields() {
    return changedFields;
  }

  public void setChangedFields(List<DataFlowNode> changedFields) {
    this.changedFields = changedFields;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("parameters{\n");
    for (DataFlowNode p : inputParameters) {
      sb.append(p.toStringForward(1, 1));
    }
    return sb.toString();
  }

}
