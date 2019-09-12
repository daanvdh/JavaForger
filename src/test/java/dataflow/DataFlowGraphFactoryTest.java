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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.collection.IsIterableContainingInAnyOrder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;

import common.SymbolSolverSetup;

/**
 * Unit test for {@link DataFlowGraphFactory}.
 *
 * @author Daan
 */
public class DataFlowGraphFactoryTest {

  private DataFlowGraphFactory factory = new DataFlowGraphFactory();

  @Before
  public void setup() {
    // TODO remove dependency to JavaForger
    SymbolSolverSetup.setup();
  }

  @Test
  public void testCreateGraph_setter() {
    String setter = //
        "public class Claz {\n" + //
            "  private String s;\n" + //
            "  public void setS(String a) {\n" + //
            "    this.s = a;\n" + //
            "  }\n" + //
            "}"; //
    DataFlowGraph expected = GraphBuilder.withStartingNodes(NodeBuilder.ofParameter("setS", "a").to("setS.s").to(NodeBuilder.ofField("s"))).build();

    CompilationUnit cu = JavaParser.parse(setter);
    DataFlowGraph graph = factory.createGraph(cu);

    assertGraph(expected, graph);
  }

  private void assertGraph(DataFlowGraph expected, DataFlowGraph graph) {
    assertNodesEqual(expected.getFields(), graph.getFields()).ifPresent(m -> Assert.fail("Fields not equal: " + m));
    assertMethodsEqual(expected.getMethods(), graph.getMethods()).ifPresent(m -> Assert.fail("Methods not equal: " + m));
    assertMethodsEqual(expected.getConstructors(), graph.getConstructors()).ifPresent(m -> Assert.fail("Constructors not equal: " + m));
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private Optional<String> assertMethodsEqual(Collection<DataFlowMethod> exp, Collection<DataFlowMethod> res) {
    List<Matcher<DataFlowMethod>> collect = exp.stream().map(this::createMatcher).collect(Collectors.toList());
    IsIterableContainingInAnyOrder<DataFlowMethod> containsInAnyOrder = new IsIterableContainingInAnyOrder(collect);

    // TODO the assert and the return value seem to do the same thing, maybe remove one of them.
    Assert.assertThat(res, containsInAnyOrder);
    return exp.stream().map(expMethod -> assertMethodEqual(expMethod, getEqualMethod(res, expMethod))).filter(Optional::isPresent).map(Optional::get)
        .findFirst();
  }

  private Optional<String> assertMethodEqual(DataFlowMethod expMethod, DataFlowMethod equalMethod) {
    return assertNodesEqual(expMethod.getInputParameters(), equalMethod.getInputParameters());
  }

  private DataFlowMethod getEqualMethod(Collection<DataFlowMethod> methods, DataFlowMethod lookup) {
    return methods.stream().filter(m -> createMatcher(lookup).matches(m)).findFirst().get();
  }

  private Matcher<DataFlowMethod> createMatcher(DataFlowMethod method) {
    EqualFeatureMatcher<DataFlowMethod, String> methodNameMatcher = new EqualFeatureMatcher<>(DataFlowMethod::getName, method.getName(), "methodName");

    EqualFeatureMatcher<DataFlowMethod, List<String>> parameterMatcher =
        new EqualFeatureMatcher<>((m) -> m.getInputParameters().stream().map(DataFlowNode::getName).collect(Collectors.toList()),
            method.getInputParameters().stream().map(DataFlowNode::getName).collect(Collectors.toList()), "methodParameters");

    EqualFeatureMatcher<DataFlowMethod, List<String>> changedFieldsMatcher =
        new EqualFeatureMatcher<>((m) -> m.getChangedFields().stream().map(DataFlowNode::getName).collect(Collectors.toList()),
            method.getChangedFields().stream().map(DataFlowNode::getName).collect(Collectors.toList()), "changedFields");

    return Matchers.allOf(methodNameMatcher, parameterMatcher, changedFieldsMatcher);
  }

  private Optional<String> assertNodesEqual(Collection<DataFlowNode> expected, Collection<DataFlowNode> fields) {
    Map<String, DataFlowNode> exp = expected.stream().collect(Collectors.toMap(DataFlowNode::getName, Function.identity()));
    Map<String, DataFlowNode> res = fields.stream().collect(Collectors.toMap(DataFlowNode::getName, Function.identity()));
    Assert.assertEquals(exp.keySet(), res.keySet());

    return exp.keySet().stream().map(key -> assertNodeEqual(exp.get(key), res.get(key))).filter(Optional::isPresent).map(Optional::get).findFirst();
  }

  /**
   * Assert that the incoming and outgoing edges of both nodes are equal
   *
   * @param exp expected
   * @param res result
   */
  private Optional<String> assertNodeEqual(DataFlowNode exp, DataFlowNode res) {
    List<DataFlowEdge> expIn = exp.getIn();
    List<DataFlowEdge> resIn = res.getIn();

    String message = !(exp.getName().equals(res.getName())) ? "Names are not equal of expected node " + exp + " and result node " + res : null;
    message =
        (message == null && expIn.size() != resIn.size()) ? "number of incoming edges not equal for expected node " + exp + " and resultNode " + res : message;
    for (int i = 0; i < expIn.size() && message == null; i++) {
      String edgeMessage = assertEdgeEqual(expIn.get(0), resIn.get(0));
      if (edgeMessage != null) {
        message = "Incoming edges not equal of expected node " + exp + " and result node " + res + ": " + edgeMessage;
      }
    }

    List<DataFlowEdge> expOut = exp.getOut();
    List<DataFlowEdge> resOut = res.getOut();
    message = (message == null && expOut.size() != resOut.size()) ? "number of outgoing edges not equal for expected node " + exp + " and resultNode " + res
        : message;
    for (int i = 0; i < expOut.size() && message == null; i++) {
      String edgeMessage = assertEdgeEqual(expOut.get(0), resOut.get(0));
      if (edgeMessage != null) {
        message = "Outgoing edges not equal of expected node " + exp + " and result node " + res + ": " + edgeMessage;
      }
    }
    return Optional.ofNullable(message);
  }

  private String assertEdgeEqual(DataFlowEdge exp, DataFlowEdge res) {
    String message = null;
    if (!exp.getFrom().getName().equals(res.getFrom().getName()) && !exp.getTo().getName().equals(res.getTo().getName())) {
      message = exp.toString() + " not equal to " + res.toString();
    }
    return message;
  }

  public class EqualFeatureMatcher<T, U> extends FeatureMatcher<T, U> {
    private final Function<T, U> mapper;

    /**
     * Constructs an instance of {@link EqualFeatureMatcher}.
     *
     * @param mapper a {@link Function} to maps object to its feature
     * @param expected the expected value
     * @param description the description of the feature
     */
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
