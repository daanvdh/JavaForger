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

import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
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

    DataFlowGraph expected = DataFlowGraphBuilder.builder().withField("s")
        .withMethod(DataFlowMethodBuilder.builder().withParameter("a").withChangedFieldEdge("a", "s").name("setS").build()).build();

    DataFlowGraph graph = factory.createGraph(cu);

    System.out.println("========expected========");
    System.out.println(expected.toString());
    System.out.println("========result========");
    System.out.println(graph.toString());

    assertGraph(expected, graph);
  }

  private void assertGraph(DataFlowGraph expected, DataFlowGraph graph) {
    assertNodesEqual(expected.getFields(), graph.getFields());
    assertMethodsEqual(expected.getMethods(), graph.getMethods());
    // TODO Auto-generated method stub
  }

  @SuppressWarnings("unchecked")
  private void assertMethodsEqual(List<DataFlowMethod> exp, List<DataFlowMethod> res) {
    Assert.assertThat(res, Matchers.containsInAnyOrder(exp.stream().map(this::createMatcher).toArray(Matcher[]::new)));
    for (DataFlowMethod expMethod : exp) {
      DataFlowMethod resMethod = getEqualMethod(res, expMethod);
      assertNodesEqual(expMethod.getInputParameters(), resMethod.getInputParameters());
    }
  }

  private DataFlowMethod getEqualMethod(List<DataFlowMethod> methods, DataFlowMethod lookup) {
    return methods.stream().filter(m -> createMatcher(lookup).matches(m)).findFirst().get();
  }

  private Matcher<? extends DataFlowMethod> createMatcher(DataFlowMethod method) {
    EqualFeatureMatcher<DataFlowMethod, String> methodNameMatcher = new EqualFeatureMatcher<>(DataFlowMethod::getName, method.getName(), "methodName");
    EqualFeatureMatcher<DataFlowMethod, List<String>> parameterMatcher =
        new EqualFeatureMatcher<>((m) -> m.getInputParameters().stream().map(DataFlowNode::getName).collect(Collectors.toList()),
            method.getInputParameters().stream().map(DataFlowNode::getName).collect(Collectors.toList()), "methodParameters");
    return Matchers.allOf(methodNameMatcher, parameterMatcher);
  }

  private void assertNodesEqual(List<DataFlowNode> expected, List<DataFlowNode> fields) {
    Map<String, DataFlowNode> exp = expected.stream().collect(Collectors.toMap(DataFlowNode::getName, Function.identity()));
    Map<String, DataFlowNode> res = fields.stream().collect(Collectors.toMap(DataFlowNode::getName, Function.identity()));
    Assert.assertEquals(exp.keySet(), res.keySet());

    for (String key : exp.keySet()) {
      DataFlowNode expNode = exp.get(key);
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
  private void assertNodeEqual(DataFlowNode exp, DataFlowNode res) {
    List<DataFlowEdge> expIn = exp.getIn();
    List<DataFlowEdge> resIn = res.getIn();
    Assert.assertEquals("number of edges not equal for nodes", expIn.size(), resIn.size());
    for (int i = 0; i < expIn.size(); i++) {
      String message = assertEdgeEqual(expIn.get(0), resIn.get(0));
      if (message != null) {
        Assert.fail("Incoming edges not equal of expected node " + exp.getName() + " and node " + res.getName() + ": " + message);
      }
    }
    List<DataFlowEdge> expOut = exp.getOut();
    List<DataFlowEdge> resOut = res.getOut();
    Assert.assertEquals("number of edges not equal for nodes", expOut.size(), resOut.size());
    for (int i = 0; i < expOut.size(); i++) {
      String message = assertEdgeEqual(expOut.get(0), resOut.get(0));
      if (message != null) {
        Assert.fail("Outgoing edges not equal of expected node " + exp.getName() + " and node " + res.getName() + ": " + message);
      }
    }
  }

  private String assertEdgeEqual(DataFlowEdge exp, DataFlowEdge res) {
    String message = null;
    if (!exp.getFrom().getName().equals(res.getFrom().getName()) && !exp.getTo().getName().equals(res.getTo().getName())) {
      message = exp.toString() + " not equal to " + res.toString();
    }
    return message;
  }

  private class EqualFeatureMatcher<T, U> extends FeatureMatcher<T, U> {
    private final Function<T, U> mapper;

    public EqualFeatureMatcher(Function<T, U> mapper, U expected, String description) {
      super(Matchers.equalTo(expected), description, description);
      this.mapper = mapper;
    }

    @Override
    protected U featureValueOf(T actual) {
      return mapper.apply(actual);
    }
  }

}
