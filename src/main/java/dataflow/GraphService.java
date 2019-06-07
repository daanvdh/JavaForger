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

/**
 * Service for methods to be executed on a {@link DataFlowGraph}.
 *
 * @author Daan
 */
public class GraphService {

  /**
   * This method should probably return a list of DataFlowNode instead. Not sure yet. It is not
   *
   * @param dfn
   * @param method
   * @return
   */
  public DataFlowNode walkBackUntil(DataFlowNode dfn, DataFlowMethod method) {

    DataFlowNode current = dfn;

    return dfn.getIn().get(0).getFrom();
  }

}
