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

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

/**
 * Unit test for {@link DataFlowGraphGenerator}.
 *
 * @author Daan
 */
public class DataFlowGraphGeneratorTest {

  private DataFlowGraphGenerator sut = new DataFlowGraphGenerator();

  @Test
  public void testCreateClassGraph_fields() {
    String code = "int a=3; String b;";

    List<String> fields = Arrays.asList("a", "b");

    executeAndVerify(code, fields);
  }

  @Test
  public void testCreateClassGraph_getter() {
    String code = "int a=3; getA() { return a; }";

    TestCallableGraph c = TestCallableGraph.builder().withParameter("b").withReturn("b").build();

    // executeAndVerify(code);
  }

  @Test
  public void testCreateClassGraph_setter() {
    String code = "int a=3; setA(int b) {a = b; }";

    TestCallableGraph c = TestCallableGraph.builder().withParameter("b").withExitingEdge("b", "a").build();

    executeAndVerify(code, c);
  }

  private void executeAndVerify(String code, TestCallableGraph c) {
    // TODO Auto-generated method stub

    sut.createMethod(node, existingCallables, fields)Callable();

  }

  private void executeAndVerify(String code, List<String> fields) {
    // TODO Auto-generated method stub

  }

}
