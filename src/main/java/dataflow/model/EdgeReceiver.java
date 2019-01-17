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
 * Defines an entity that can receive incoming edges, in other words an entity which a directed edge can point to. In a DataFlowGraph both edges and nodes can
 * receive edges.
 *
 * @author Daan
 */
public abstract class EdgeReceiver {

  private List<DataFlowEdge> incoming = new ArrayList<>();

  public List<DataFlowEdge> getIncoming() {
    return incoming;
  }

  public void setIncoming(List<DataFlowEdge> incoming) {
    this.incoming = incoming;
  }

}
