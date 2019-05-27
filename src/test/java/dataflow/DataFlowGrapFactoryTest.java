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
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;

/**
 * Unit test for {@link DataFlowGrapFactory}.
 *
 * @author Daan
 */
public class DataFlowGrapFactoryTest {

  private DataFlowGrapFactory factory = new DataFlowGrapFactory();

  @Test
  public void testCreateGraph_setter() {
    String setter = //
        "public class Claz {\n" + //
            "  private String s;\n" + //
            "  public void setS(String a) {\n" + //
            "    this.s = a;\n" + //
            "  }\n" + //
            "}"; //
    CompilationUnit cu = JavaParser.parse(setter);

    TestDataFlowGraph expected =
        TestDataFlowGraph.builder().withField("s").withMethod(TestDataFlowMethod.builder().withParameter("a").withChangedFieldEdge("a", "s").build()).build();

    DataFlowGraph graph = factory.createGraph(cu);

    System.out.println(expected.toString());
    System.out.println("================");
    System.out.println(graph.toString());

    assertGraph(expected, graph);
  }

  private void assertGraph(TestDataFlowGraph expected, DataFlowGraph graph) {
    assertNodesEqual(expected.getFields(), graph.getFields());
    assertMethodsEqual(expected.getMethods(), graph.getMethods());
    // TODO Auto-generated method stub
  }

  private void assertMethodsEqual(List<TestDataFlowMethod> exp, List<DataFlowMethod> res) {
    // TODO implement
  }

  private void assertNodesEqual(List<TestDataFlowNode> expected, List<DataFlowNode> fields) {
    Map<String, TestDataFlowNode> exp = expected.stream().collect(Collectors.toMap(TestDataFlowNode::getName, Function.identity()));
    Map<String, DataFlowNode> res = fields.stream().collect(Collectors.toMap(DataFlowNode::getName, Function.identity()));
    Assert.assertEquals(exp.keySet(), res.keySet());

    for (String key : exp.keySet()) {
      TestDataFlowNode expNode = exp.get(key);
      DataFlowNode resNode = res.get(key);
      assertNodeEqual(expNode, resNode);
    }
  }

  /**
   * Assert that the incoming and outgoing edges of both nodes are equal
   *
   * @param exp expected
   * @param res result
   */
  private void assertNodeEqual(TestDataFlowNode exp, DataFlowNode res) {
    List<TestDataFlowEdge> expIn = exp.getIn();
    List<DataFlowEdge> resIn = res.getIn();
    Assert.assertEquals("number of edges not equal for nodes", expIn.size(), resIn.size());
    for (int i = 0; i < expIn.size(); i++) {
      String message = assertEdgeEqual(expIn.get(0), resIn.get(0));
      if (message != null) {
        Assert.fail("Incoming edges not equal of expected node " + exp.getName() + " and node " + res.getName() + ": " + message);
      }
    }
    List<TestDataFlowEdge> expOut = exp.getOut();
    List<DataFlowEdge> resOut = res.getOut();
    Assert.assertEquals("number of edges not equal for nodes", expOut.size(), resOut.size());
    for (int i = 0; i < expOut.size(); i++) {
      String message = assertEdgeEqual(expOut.get(0), resOut.get(0));
      if (message != null) {
        Assert.fail("Outgoing edges not equal of expected node " + exp.getName() + " and node " + res.getName() + ": " + message);
      }
    }
  }

  private String assertEdgeEqual(TestDataFlowEdge exp, DataFlowEdge res) {
    String message = null;
    if (!exp.getFrom().getName().equals(res.getFrom().getName()) && !exp.getTo().getName().equals(res.getTo().getName())) {
      message = exp.toString() + " not equal to " + res.toString();
    }
    return message;
  }

}
