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
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.stmt.ReturnStmt;
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
    CompilationUnit cu = JavaParser.parse(//
        "public class Claz {\n" + //
            "  private String s;\n" + //
            "  public void setS(String a) {\n" + //
            "    this.s = a;\n" + //
            "  }\n" + //
            "}");

    DataFlowNode s = createField(cu, "s");
    DataFlowNode a = createParameter(cu, "a");
    DataFlowNode setS_s = createNode(cu, "setS.s", AssignExpr.class);
    connectNodesInSquence(a, setS_s, s);
    DataFlowMethod setS = createMethod("setS").inputParameters(a).nodes(setS_s).changedFields(s).build();
    DataFlowGraph expected = DataFlowGraph.builder().fields(s).methods(setS).build();

    executeAndVerify(cu, expected);
  }

  @Test
  public void testCreate_setterMultipleInput() {
    CompilationUnit cu = JavaParser.parse(//
        "public class Claz {\n" + //
            "  private String s,t;\n" + //
            "  public void setS(String a, String b) {\n" + //
            "    this.s = a;\n" + //
            "    this.t = b;\n" + //
            "  }\n" + //
            "}"); //
    DataFlowNode s = createField(cu, "s");
    DataFlowNode t = createField(cu, "t");
    DataFlowNode a = createParameter(cu, "a");
    DataFlowNode b = createParameter(cu, "b");
    DataFlowNode setS_s = createNode(cu, "setS.s", AssignExpr.class, 0);
    DataFlowNode setS_t = createNode(cu, "setS.t", AssignExpr.class, 1);

    connectNodesInSquence(a, setS_s, s);
    connectNodesInSquence(b, setS_t, t);

    DataFlowMethod setS = createMethod("setS").inputParameters(a, b).nodes(setS_s, setS_t).changedFields(s, t).build();
    DataFlowGraph expected = DataFlowGraph.builder().fields(s, t).methods(setS).build();

    executeAndVerify(cu, expected);
  }

  @Test
  public void testCreate_setterAssignFieldTwice() {

    CompilationUnit cu = JavaParser.parse(//
        "public class Claz {\n" + //
            "  private String s;\n" + //
            "  public void setS(String a, String b) {\n" + //
            "    this.s = a;\n" + //
            "    this.s = b;\n" + //
            "  }\n" + //
            "}"); //
    DataFlowNode s = createField(cu, "s");
    DataFlowNode a = createParameter(cu, "a");
    DataFlowNode b = createParameter(cu, "b");
    DataFlowNode setS_s1 = createNode(cu, "setS.s", AssignExpr.class, 0);
    DataFlowNode setS_s2 = createNode(cu, "setS.s.2", AssignExpr.class, 1);

    connectNodesInSquence(a, setS_s1);
    connectNodesInSquence(b, setS_s2, s);

    DataFlowMethod setS = createMethod("setS").inputParameters(a, b).nodes(setS_s1, setS_s2).changedFields(s).build();
    DataFlowGraph expected = DataFlowGraph.builder().fields(s).methods(setS).build();

    executeAndVerify(cu, expected);
  }

  @Test
  public void testCreate_setterAssignFieldToField() {
    CompilationUnit cu = JavaParser.parse(//
        "public class Claz {\n" + //
            "  private String s,t;\n" + //
            "  public void setS(String a) {\n" + //
            "    this.s = a;\n" + //
            "    this.t = this.s;\n" + //
            "  }\n" + //
            "}"); //
    DataFlowNode s = createField(cu, "s");
    DataFlowNode t = createField(cu, "t");
    DataFlowNode a = createParameter(cu, "a");
    DataFlowNode setS_s = createNode(cu, "setS.s", AssignExpr.class, 0);
    DataFlowNode setS_t = createNode(cu, "setS.t", AssignExpr.class, 1);

    connectNodesInSquence(a, setS_s, s);
    connectNodesInSquence(setS_s, setS_t, t);

    DataFlowMethod setS = createMethod("setS").inputParameters(a).nodes(setS_s, setS_t).changedFields(s, t).build();
    DataFlowGraph expected = DataFlowGraph.builder().fields(s, t).methods(setS).build();

    executeAndVerify(cu, expected);
  }

  @Test
  public void testCreate_return() {
    CompilationUnit cu = JavaParser.parse(//
        "public class Claz {\n" + //
            "  public int called(int a) {\n" + //
            "    return a;\n" + //
            "  }\n" + //
            "}"); //
    DataFlowNode a = createParameter(cu, "a");
    DataFlowNode ret1 = createSpecificReturn(cu, "called");
    DataFlowNode methodReturn = createMethodReturn(cu, "called");

    connectNodesInSquence(a, ret1, methodReturn);

    DataFlowMethod setS = createMethod("called").inputParameters(a).nodes(ret1).returnNode(methodReturn).build();
    DataFlowGraph expected = DataFlowGraph.builder().methods(setS).build();

    executeAndVerify(cu, expected);
  }

  @Test
  public void testCreate_methodCallingMethod() {
    CompilationUnit cu = JavaParser.parse(//
        "public class Claz {\n" + //
            "  public int caller(int a) {\n" + //
            "    return called(a);\n" + //
            "  }\n" + //
            "  public int called(int b) {\n" + //
            "    return b;\n" + //
            "  }\n" + //
            "}"); //
    DataFlowNode a = createParameter(cu, "a");
    DataFlowNode b_caller = createNode(cu, "b", NameExpr.class);
    DataFlowNode specificReturnCaller = createSpecificReturn(cu, "caller");
    DataFlowNode genericReturnCaller = createMethodReturn(cu,"caller");
    DataFlowNode nodeCallReturn = createNode(cu, "nodeCall_called_return", MethodCallExpr.class);
    DataFlowMethod caller = createMethod("caller").inputParameters(a).nodes(b_caller, specificReturnCaller, nodeCallReturn).returnNode(genericReturnCaller)
        .representedNode(null).build();

    DataFlowNode b_called = createParameter(cu, "b");
    DataFlowNode specificReturnCalled = createSpecificReturn(cu, "called");
    DataFlowNode genericReturnCalled = createMethodReturn(cu, "called");
    DataFlowMethod called = createMethod("called").inputParameters(b_called).nodes(specificReturnCalled).returnNode(genericReturnCalled).build();

    connectNodesInSquence(a, b_caller, b_called, specificReturnCalled, genericReturnCalled, nodeCallReturn, specificReturnCaller, genericReturnCaller);
    DataFlowGraph expected = DataFlowGraph.builder().methods(caller, called).build();

    executeAndVerify(cu, expected);
  }

  @Test
  public void testCreate_createVar() {
    CompilationUnit cu = JavaParser.parse(//
        "public class Claz {\n" + //
            "  public int met(int a) {\n" + //
            "    int b = a;\n" + //
            "    return b;\n" + //
            "  }\n" + //
            "}"); //
    DataFlowNode a = createParameter(cu, "a");
    DataFlowNode b_caller = createNode(cu, "b", VariableDeclarator.class);
    DataFlowNode specificReturnCaller = createSpecificReturn(cu, "met");
    DataFlowNode genericReturnCaller = createMethodReturn(cu,"met");
    DataFlowMethod caller = createMethod("met").inputParameters(a).nodes(b_caller, specificReturnCaller).returnNode(genericReturnCaller)
        .representedNode(null).build();

    connectNodesInSquence(a, b_caller, specificReturnCaller, genericReturnCaller);
    DataFlowGraph expected = DataFlowGraph.builder().methods(caller).build();

    executeAndVerify(cu, expected);
  }

  @Test
  public void testCreate_inputMethods() {
    CompilationUnit cu = JavaParser.parse(//
        "public class Claz {\n" + //
            "  StringBuilder sb = new StringBuilder(); \n" + //
            "  public StringBuilder met(String a) {\n" + //
            "    return sb.append(a);\n" + //
            "  }\n" + //
            "}"); //
    DataFlowNode a = createParameter(cu, "a");
    DataFlowNode sb_input = createNode(cu, "arg0", NameExpr.class, 1);
    a.addEdgeTo(sb_input);

    DataFlowNode sb_output = createNode(cu, "nodeCall_append_return", MethodCallExpr.class);
    DataFlowNode specificReturnCaller = createSpecificReturn(cu, "met");
    DataFlowNode genericReturnCaller = createMethodReturn(cu, "met");
    this.connectNodesInSquence(sb_output, specificReturnCaller, genericReturnCaller);

    DataFlowMethod caller = createMethod("met").inputParameters(a).nodes(sb_input, sb_output, specificReturnCaller).returnNode(genericReturnCaller).build();

    DataFlowGraph expected = DataFlowGraph.builder().fields(createField(cu, "sb")).methods(caller).build();

    executeAndVerify(cu, expected);
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

  private DataFlowNode createField(CompilationUnit cu, String name) {
    VariableDeclarator represented = cu.findAll(VariableDeclarator.class).stream().filter(v -> v.getNameAsString().equals(name)).findFirst().get();
    return createNodeBuilder(name).representedNode(represented).build();
  }

  private DataFlowNode createMethodReturn(CompilationUnit cu, String methodName) {
    MethodDeclaration method = cu.findAll(MethodDeclaration.class).stream().filter(v -> v.getNameAsString().equals(methodName)).findFirst().get();
    return createNodeBuilder(methodName + "_return").representedNode(method).build();
  }

  private DataFlowNode createSpecificReturn(CompilationUnit cu, String methodName) {
    ReturnStmt ret = cu.findAll(MethodDeclaration.class).stream().filter(v -> v.getNameAsString().equals(methodName)).findFirst().get()
        .findAll(ReturnStmt.class).get(0);
    return createNodeBuilder(methodName + "_return_line" + ret.getBegin().get().line + "_col" + ret.getBegin().get().column).representedNode(ret).build();
  }

  private DataFlowNode createParameter(CompilationUnit cu, String name) {
    Parameter represented = cu.findAll(MethodDeclaration.class).stream().map(md -> md.getParameterByName(name)).filter(Optional::isPresent).findFirst().get()
        .get();
    return createNodeBuilder(name).representedNode(represented).build();
  }

  private DataFlowNode createNode(CompilationUnit cu, String name, Class<? extends Node> claz) {
    return createNode(cu, name, claz, 0);
  }

  private DataFlowNode createNode(CompilationUnit cu, String name, Class<? extends Node> claz, int index) {
    Node represented = cu.findAll(claz).get(index);
    return createNodeBuilder(name).representedNode(represented).build();
  }

  private DataFlowNode createNode(String name) {
    return createNodeBuilder(name).build();
  }

  private DataFlowNode.Builder createNodeBuilder(String name) {
    return DataFlowNode.builder().name(name).representedNode(new SimpleName(name));
  }

  private DataFlowGraph executeAndVerify(CompilationUnit cu, DataFlowGraph expected) {
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
    Optional<String> changedFieldsEqual = nodesEqual.isPresent() ? nodesEqual
        : assertNodesEqual(expMethod.getChangedFields(), equalMethod.getChangedFields())
            .map(s -> "for " + expMethod.getName() + " changedFields not equal: " + s);
    return changedFieldsEqual;
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

    if (message == null) {
      String s = "Owner not equal for node " + exp.getName() + " expected " + exp.getOwner() + " but was " + res.getOwner();
      if (exp.getOwner().isPresent() && res.getOwner().isPresent()) {
        if (!(exp.getOwner().get().getName().equals(res.getOwner().get().getName())) || //
            !(exp.getOwner().get().getClass().equals(res.getOwner().get().getClass()))) {
          message = s;
        }
      } else if (exp.getOwner().isPresent() != res.getOwner().isPresent()) {
        message = s;
      }
    }

    if (message == null && !exp.getRepresentedNode().equals(res.getRepresentedNode())) {
      message = "RepresentedNode not equal for node " + exp.getName() + " expected " + exp.getRepresentedNode() + " (" + exp.getRepresentedNode().getClass()
          + ")" + " but was " + res.getRepresentedNode() + " (" + res.getRepresentedNode().getClass() + ")";
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
