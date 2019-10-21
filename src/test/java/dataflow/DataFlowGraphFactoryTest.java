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
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.google.common.base.Functions;

import common.SymbolSolverSetup;
import dataflow.model.DataFlowGraph;
import dataflow.model.DataFlowMethod;
import dataflow.model.DataFlowMethod.Builder;
import dataflow.model.DataFlowNode;
import dataflow.model.DataFlowNodeTest;

/**
 * Unit test for {@link DataFlowGraphFactory}.
 *
 * @author Daan
 */
public class DataFlowGraphFactoryTest {

  private DataFlowNodeTest dfnTest = new DataFlowNodeTest();

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

    DataFlowNode s = dfnTest.createField(cu, "s");
    DataFlowNode a = dfnTest.createParameter(cu, "a");
    DataFlowNode setS_s = dfnTest.createNode(cu, "setS.s", AssignExpr.class);
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
    DataFlowNode s = dfnTest.createField(cu, "s");
    DataFlowNode t = dfnTest.createField(cu, "t");
    DataFlowNode a = dfnTest.createParameter(cu, "a");
    DataFlowNode b = dfnTest.createParameter(cu, "b");
    DataFlowNode setS_s = dfnTest.createNode(cu, "setS.s", AssignExpr.class, 0);
    DataFlowNode setS_t = dfnTest.createNode(cu, "setS.t", AssignExpr.class, 1);

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
    DataFlowNode s = dfnTest.createField(cu, "s");
    DataFlowNode a = dfnTest.createParameter(cu, "a");
    DataFlowNode b = dfnTest.createParameter(cu, "b");
    DataFlowNode setS_s1 = dfnTest.createNode(cu, "setS.s", AssignExpr.class, 0);
    DataFlowNode setS_s2 = dfnTest.createNode(cu, "setS.s.2", AssignExpr.class, 1);

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
    DataFlowNode s = dfnTest.createField(cu, "s");
    DataFlowNode t = dfnTest.createField(cu, "t");
    DataFlowNode a = dfnTest.createParameter(cu, "a");
    DataFlowNode setS_s = dfnTest.createNode(cu, "setS.s", AssignExpr.class, 0);
    DataFlowNode setS_t = dfnTest.createNode(cu, "setS.t", AssignExpr.class, 1);

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
    DataFlowNode a = dfnTest.createParameter(cu, "a");
    DataFlowNode ret1 = dfnTest.createSpecificReturn(cu, "called");
    DataFlowNode methodReturn = dfnTest.createMethodReturn(cu, "called");

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
    DataFlowNode a = dfnTest.createParameter(cu, "a");
    DataFlowNode b_caller = dfnTest.createNode(cu, "b", NameExpr.class);
    DataFlowNode specificReturnCaller = dfnTest.createSpecificReturn(cu, "caller");
    DataFlowNode genericReturnCaller = dfnTest.createMethodReturn(cu, "caller");
    DataFlowNode nodeCallReturn = dfnTest.createNode(cu, "nodeCall_called_return", MethodCallExpr.class);
    DataFlowMethod caller = createMethod("caller").inputParameters(a).nodes(b_caller, specificReturnCaller, nodeCallReturn).returnNode(genericReturnCaller)
        .representedNode(null).build();

    DataFlowNode b_called = dfnTest.createParameter(cu, "b");
    DataFlowNode specificReturnCalled = dfnTest.createSpecificReturn(cu, "called");
    DataFlowNode genericReturnCalled = dfnTest.createMethodReturn(cu, "called");
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
    DataFlowNode a = dfnTest.createParameter(cu, "a");
    DataFlowNode b_caller = dfnTest.createNode(cu, "b", VariableDeclarator.class);
    DataFlowNode specificReturnCaller = dfnTest.createSpecificReturn(cu, "met");
    DataFlowNode genericReturnCaller = dfnTest.createMethodReturn(cu, "met");
    DataFlowMethod caller =
        createMethod("met").inputParameters(a).nodes(b_caller, specificReturnCaller).returnNode(genericReturnCaller).representedNode(null).build();

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
    DataFlowNode a = dfnTest.createParameter(cu, "a");
    DataFlowNode sb_input = dfnTest.createNode(cu, "arg0", NameExpr.class, 1);
    a.addEdgeTo(sb_input);

    DataFlowNode sb_output = dfnTest.createNode(cu, "nodeCall_append_return", MethodCallExpr.class);
    DataFlowNode specificReturnCaller = dfnTest.createSpecificReturn(cu, "met");
    DataFlowNode genericReturnCaller = dfnTest.createMethodReturn(cu, "met");
    this.connectNodesInSquence(sb_output, specificReturnCaller, genericReturnCaller);

    DataFlowMethod caller = createMethod("met").inputParameters(a).nodes(sb_input, sb_output, specificReturnCaller).returnNode(genericReturnCaller).build();

    DataFlowGraph expected = DataFlowGraph.builder().fields(dfnTest.createField(cu, "sb")).methods(caller).build();

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

  private DataFlowGraph executeAndVerify(CompilationUnit cu, DataFlowGraph expected) {
    DataFlowGraph graph = factory.create(cu);
    assertGraph(expected, graph);
    return graph;
  }

  private void assertGraph(DataFlowGraph expected, DataFlowGraph graph) {
    dfnTest.assertNodesEqual(expected.getFields(), graph.getFields()).ifPresent(m -> fail(expected, graph, "Fields not equal: " + m));
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
    Optional<String> notFound =
        expToResMap.entrySet().stream().filter(e -> !e.getValue().isPresent()).map(Map.Entry::getKey).map(dfm -> "no method found for " + dfm).findFirst();
    if (!notFound.isPresent()) {
      notFound = expToResMap.entrySet().stream().map(e -> assertMethodEqual(e.getKey(), e.getValue().get())).filter(Optional::isPresent).map(Optional::get)
          .findFirst();
    }
    return notFound;
  }

  private Optional<String> assertMethodEqual(DataFlowMethod expMethod, DataFlowMethod equalMethod) {
    Optional<String> parametersEqual = dfnTest.assertNodesEqual(expMethod.getInputParameters().getNodes(), equalMethod.getInputParameters().getNodes())
        .map(s -> "for " + expMethod.getName() + " parameters not equal: " + s);
    Optional<String> nodesEqual = parametersEqual.isPresent() ? parametersEqual
        : dfnTest.assertNodesEqual(expMethod.getNodes(), equalMethod.getNodes()).map(s -> "for " + expMethod.getName() + ": " + s);
    Optional<String> changedFieldsEqual = nodesEqual.isPresent() ? nodesEqual
        : dfnTest.assertNodesEqual(expMethod.getChangedFields(), equalMethod.getChangedFields())
            .map(s -> "for " + expMethod.getName() + " changedFields not equal: " + s);
    return changedFieldsEqual;
  }

  private Optional<DataFlowMethod> getEqualMethod(Collection<DataFlowMethod> methods, DataFlowMethod lookup) {
    return methods.stream().filter(m -> createMatcher(lookup).matches(m)).findFirst();
  }

  private Matcher<DataFlowMethod> createMatcher(DataFlowMethod method) {
    EqualFeatureMatcher<DataFlowMethod, String> methodNameMatcher = new EqualFeatureMatcher<>(DataFlowMethod::getName, method.getName(), "methodName");

    EqualFeatureMatcher<DataFlowMethod, List<String>> parameterMatcher =
        new EqualFeatureMatcher<>((m) -> m.getInputParameters().getNodes().stream().map(DataFlowNode::getName).collect(Collectors.toList()),
            method.getInputParameters().getNodes().stream().map(DataFlowNode::getName).collect(Collectors.toList()), "methodParameters");

    return Matchers.allOf(methodNameMatcher, parameterMatcher);
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
