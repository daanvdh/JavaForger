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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.SimpleName;
import com.google.common.base.Functions;

import common.SymbolSolverSetup;
import dataflow.model.DataFlowEdge;
import dataflow.model.DataFlowGraph;
import dataflow.model.DataFlowMethod;
import dataflow.model.DataFlowMethod.Builder;
import dataflow.model.DataFlowNode;

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
  public void testCreate_setter() {
    String setter = //
        "public class Claz {\n" + //
            "  private String s;\n" + //
            "  public void setS(String a) {\n" + //
            "    this.s = a;\n" + //
            "  }\n" + //
            "}"; //
    DataFlowGraph expected = GraphBuilder.withStartingNodes(NodeBuilder.ofParameter("setS", "a").to("setS.s").to(NodeBuilder.ofField("s"))).build();

    executeAndVerify(setter, expected);
  }

  @Test
  public void testCreate_setterMultipleInput() {
    String setter = //
        "public class Claz {\n" + //
            "  private String s,t;\n" + //
            "  public void setS(String a, String b) {\n" + //
            "    this.s = a;\n" + //
            "    this.t = b;\n" + //
            "  }\n" + //
            "}"; //
    DataFlowGraph expected = GraphBuilder.withStartingNodes( //
        NodeBuilder.ofParameter("setS", "a").to("setS.s").to(NodeBuilder.ofField("s")), //
        NodeBuilder.ofParameter("setS", "b").to("setS.t").to(NodeBuilder.ofField("t")) //
    ).build();

    executeAndVerify(setter, expected);
  }

  @Test
  public void testCreate_setterAssignFieldTwice() {
    String setter = //
        "public class Claz {\n" + //
            "  private String s;\n" + //
            "  public void setS(String a, String b) {\n" + //
            "    this.s = a;\n" + //
            "    this.s = b;\n" + //
            "  }\n" + //
            "}"; //
    DataFlowGraph expected = GraphBuilder.withStartingNodes( //
        NodeBuilder.ofParameter("setS", "a").to("setS.s"), //
        NodeBuilder.ofParameter("setS", "b").to("setS.s.2").to(NodeBuilder.ofField("s")) //
    ).build();

    executeAndVerify(setter, expected);
  }

  @Test
  public void testCreate_setterAssignFieldToField() {
    String setter = //
        "public class Claz {\n" + //
            "  private String s,t;\n" + //
            "  public void setS(String a) {\n" + //
            "    this.s = a;\n" + //
            "    this.t = this.s;\n" + //
            "  }\n" + //
            "}"; //

    NodeBuilder inBetween = NodeBuilder.ofInBetween("setS.s");
    inBetween.to( //
        NodeBuilder.ofField("s"), //
        NodeBuilder.ofInBetween("setS.t").to(NodeBuilder.ofField("t")).getRoot() //
    ); //
    DataFlowGraph expected = GraphBuilder.withStartingNodes(NodeBuilder.ofParameter("setS", "a").to(inBetween)).build();

    executeAndVerify(setter, expected);
  }

  @Test
  public void testCreate_return() {
    String claz = //
        "public class Claz {\n" + //
            "  public int called(int a) {\n" + //
            "    return a;\n" + //
            "  }\n" + //
            "}"; //

    DataFlowGraph expected = GraphBuilder.withStartingNodes(NodeBuilder.ofParameter("called", "a").to(NodeBuilder.ofReturn("called", "line3", "col5"))).build();

    executeAndVerify(claz, expected);
  }

  @Test
  public void testCreate_methodCallingMethod() {
    String claz = //
        "public class Claz {\n" + //
            "  public int caller(int a) {\n" + //
            "    return called(a);\n" + //
            "  }\n" + //
            "  public int called(int b) {\n" + //
            "    return b;\n" + //
            "  }\n" + //
            "}"; //

    DataFlowNode a = createNode("a");
    DataFlowNode b_caller = createNode("b");
    DataFlowNode specificReturnCaller = createNode("caller_return_line3_col5");
    DataFlowNode genericReturnCaller = createNode("caller_return");
    DataFlowNode nodeCallReturn = createNode("nodeCall_called_return");
    DataFlowMethod caller = createMethod("caller").inputParameters(a).nodes(b_caller, specificReturnCaller, nodeCallReturn).returnNode(genericReturnCaller)
        .representedNode(null).build();

    DataFlowNode b_called = createNode("b");
    DataFlowNode specificReturnCalled = createNode("called_return_line6_col5");
    DataFlowNode genericReturnCalled = createNode("called_return");
    DataFlowMethod called = createMethod("called").inputParameters(b_called).nodes(specificReturnCalled).returnNode(genericReturnCalled).build();

    connectNodesInSquence(a, b_caller, b_called, specificReturnCalled, genericReturnCalled, nodeCallReturn, specificReturnCaller, genericReturnCaller);
    DataFlowGraph exp = DataFlowGraph.builder().methods(caller, called).build();

    executeAndVerify(claz, exp);
  }

  @Test
  public void testCreate_createVar() {
    String claz = //
        "public class Claz {\n" + //
            "  public int met(int a) {\n" + //
            "    int b = a;\n" + //
            "    return b;\n" + //
            "  }\n" + //
            "}"; //

    DataFlowGraph expected = GraphBuilder.withStartingNodes(NodeBuilder.ofParameter("met", "a").to("b").to(NodeBuilder.ofReturn("met", "line4", "col5")))
        .build();

    executeAndVerify(claz, expected);
  }

  @Test
  public void testCreate_inputMethods() {
    String claz = //
        "public class Claz {\n" + //
            "  StringBuilder sb = new StringBuilder(); \n" + //
            "  public StringBuilder met(String a) {\n" + //
            "    return sb.append(a);\n" + //
            "  }\n" + //
            "}"; //

    DataFlowNode a = createNode("a");
    DataFlowNode sb_input = createNode("arg0");
    a.addEdgeTo(sb_input);

    DataFlowNode sb_output = createNode("nodeCall_append_return");
    DataFlowNode specificReturnCaller = createNode("met_return_line4_col5");
    DataFlowNode genericReturnCaller = createNode("met_return");
    this.connectNodesInSquence(sb_output, specificReturnCaller, genericReturnCaller);

    DataFlowMethod caller = createMethod("met").inputParameters(a).nodes(sb_input, sb_output, specificReturnCaller).returnNode(genericReturnCaller).build();

    DataFlowGraph expected = DataFlowGraph.builder().fields(createNode("sb")).methods(caller).build();

    executeAndVerify(claz, expected);
  }

  private Builder createMethod(String name) {
    MethodDeclaration m = new MethodDeclaration();
    m.setName(new SimpleName(name));
    return DataFlowMethod.builder().name(name).representedNode(m);
  }

  private void connectNodesInSquence(DataFlowNode... nodes) {
    for (int i = 0; i < nodes.length - 1; i++) {
      nodes[i].addEdgeTo(nodes[i + 1]);
    }
  }

  private DataFlowNode createNode(String name) {
    return DataFlowNode.builder().name(name).representedNode(new SimpleName(name)).build();
  }

  private DataFlowGraph executeAndVerify(String setter, DataFlowGraph expected) {
    CompilationUnit cu = JavaParser.parse(setter);
    DataFlowGraph graph = factory.create(cu);
    assertGraph(expected, graph);
    return graph;
  }

  private void assertGraph(DataFlowGraph expected, DataFlowGraph graph) {
    assertNodesEqual(expected.getFields(), graph.getFields()).ifPresent(m -> fail(expected, graph, "Fields not equal: " + m));
    assertMethodsEqual(expected.getMethods(), graph.getMethods()).ifPresent(m -> fail(expected, graph, "Methods not equal: " + m));
    assertMethodsEqual(expected.getConstructors(), graph.getConstructors()).ifPresent(m -> fail(expected, graph, "Constructors not equal: " + m));
  }

  private void fail(DataFlowGraph expected, DataFlowGraph graph, String message) {
    System.out.println("============================== Expected ==============================");
    System.out.println(expected);
    System.out.println("=============================== Actual ===============================");
    System.out.println(graph.toString());
    Assert.fail(message);
  }

  private Optional<String> assertMethodsEqual(Collection<DataFlowMethod> exp, Collection<DataFlowMethod> res) {
    Map<DataFlowMethod, Optional<DataFlowMethod>> expToResMap = exp.stream().collect(Collectors.toMap(Functions.identity(), e -> getEqualMethod(res, e)));
    Optional<String> notFound = expToResMap.entrySet().stream().filter(e -> !e.getValue().isPresent()).map(Map.Entry::getKey)
        .map(dfm -> "no method found for " + dfm).findFirst();
    if (!notFound.isPresent()) {
      notFound = expToResMap.entrySet().stream().map(e -> assertMethodEqual(e.getKey(), e.getValue().get())).filter(Optional::isPresent).map(Optional::get)
          .findFirst();
    }
    return notFound;
  }

  private Optional<String> assertMethodEqual(DataFlowMethod expMethod, DataFlowMethod equalMethod) {
    Optional<String> parametersEqual = assertNodesEqual(expMethod.getInputParameters().getNodes(), equalMethod.getInputParameters().getNodes())
        .map(s -> "for " + expMethod.getName() + " parameters not equal: " + s);
    Optional<String> nodesEqual = parametersEqual.isPresent() ? parametersEqual
        : assertNodesEqual(expMethod.getNodes(), equalMethod.getNodes()).map(s -> "for " + expMethod.getName() + ": " + s);
    return nodesEqual;
  }

  private Optional<DataFlowMethod> getEqualMethod(Collection<DataFlowMethod> methods, DataFlowMethod lookup) {
    return methods.stream().filter(m -> createMatcher(lookup).matches(m)).findFirst();
  }

  private Matcher<DataFlowMethod> createMatcher(DataFlowMethod method) {
    EqualFeatureMatcher<DataFlowMethod, String> methodNameMatcher = new EqualFeatureMatcher<>(DataFlowMethod::getName, method.getName(), "methodName");

    EqualFeatureMatcher<DataFlowMethod, List<String>> parameterMatcher = new EqualFeatureMatcher<>(
        (m) -> m.getInputParameters().getNodes().stream().map(DataFlowNode::getName).collect(Collectors.toList()),
        method.getInputParameters().getNodes().stream().map(DataFlowNode::getName).collect(Collectors.toList()), "methodParameters");

    return Matchers.allOf(methodNameMatcher, parameterMatcher);
  }

  /**
   * Assert that the names and all incoming and outgoing edges are equal, regardless of the order.
   *
   * @param expected
   * @param fields
   * @return Empty optional if assertion passed, optional containing an error message otherwise.
   */
  private Optional<String> assertNodesEqual(Collection<DataFlowNode> expected, Collection<DataFlowNode> fields) {
    Map<String, DataFlowNode> exp = expected.stream().collect(Collectors.toMap(DataFlowNode::getName, Function.identity()));
    Map<String, DataFlowNode> res = fields.stream().collect(Collectors.toMap(DataFlowNode::getName, Function.identity()));
    Optional<String> equal = exp.keySet().equals(res.keySet()) ? Optional.empty()
        : Optional.of("Nodes not equal, expected: " + exp.keySet() + " but was: " + res.keySet());
    if (!equal.isPresent()) {
      equal = exp.keySet().stream().map(key -> assertNodeEqual(exp.get(key), res.get(key))).filter(Optional::isPresent).map(Optional::get).findFirst();
    }
    return equal;
  }

  /**
   * Assert that the incoming and outgoing edges of both nodes are equal
   *
   * @param exp expected
   * @param res result
   * @return Empty optional if assertion passed, optional containing an error message otherwise.
   */
  private Optional<String> assertNodeEqual(DataFlowNode exp, DataFlowNode res) {
    List<DataFlowEdge> expIn = exp.getIn();
    List<DataFlowEdge> resIn = res.getIn();

    String message = !(exp.getName().equals(res.getName())) ? "Names are not equal of expected node " + exp + " and result node " + res : null;
    message = (message == null && expIn.size() != resIn.size()) ? "number of incoming edges not equal for expected node " + exp + " and resultNode " + res
        : message;
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
     * @param mapper      a {@link Function} to maps object to its feature
     * @param expected    the expected value
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
