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

import java.util.ArrayList;
import java.util.List;

/**
 * TODO javadoc
 *
 * @author Daan
 */
public class TestEdgeReceiver {

  private List<TestEdge> incoming = new ArrayList<>();

  private String name;

  public TestEdgeReceiver(String name) {
    name = name;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<TestEdge> getIncoming() {
    return incoming;
  }

  public void setIncoming(List<TestEdge> incoming) {
    this.incoming = incoming;
  }

  public void addIncoming(TestEdge edge) {
    this.incoming.add(edge);
  }

}
