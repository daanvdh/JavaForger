/*
 * Copyright (c) 2019 by Eyefreight BV (www.eyefreight.com). All rights reserved.
 *
 * This software is provided by the copyright holder and contributors "as is" and any express or implied warranties, including, but
 * not limited to, the implied warranties of merchantability and fitness for a particular purpose are disclaimed. In no event shall
 * Eyefreight BV or contributors be liable for any direct, indirect, incidental, special, exemplary, or consequential damages
 * (including, but not limited to, procurement of substitute goods or services; * loss of use, data, or profits; or business
 * interruption) however caused and on any theory of liability, whether in contract, strict liability, or tort (including
 * negligence or otherwise) arising in any way out of the use of this software, even if advised of the possibility of such damage.
 */
package dataflow;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;

import common.SymbolSolverSetup;
import dataflow.model.DataFlowGraph;
import dataflow.model.DataFlowMethod;
import dataflow.model.DataFlowNode;
import dataflow.model.DataFlowNodeTest;
import dataflow.model.NodeCall;
import dataflow.model.ParameterList;

/**
 * Unit test for {@link NodeCallFactory}.
 *
 * @author Daan
 */
public class NodeCallFactoryTest {

  private DataFlowNodeTest dfnTest = new DataFlowNodeTest();

  private NodeCallFactory sut = new NodeCallFactory();

  @Before
  public void setup() {
    // TODO remove dependency to JavaForger
    SymbolSolverSetup.setup();
  }

  @Test
  public void testCreate() {
    String claz = //
        "public class Claz {\n" + //
            "  StringBuilder sb = new StringBuilder(); \n" + //
            "  public void met(String a) {\n" + //
            "    sb.append(a);\n" + //
            "  }\n" + //
            "}"; //
    CompilationUnit cu = JavaParser.parse(claz);
    List<MethodCallExpr> methodCalls = cu.findAll(MethodCallExpr.class);

    DataFlowGraph graph = DataFlowGraph.builder().build();
    DataFlowMethod method = DataFlowMethod.builder().name("met").build();
    MethodCallExpr node = methodCalls.get(0);

    Optional<NodeCall> resultMethod = sut.create(method, node);

    Assert.assertTrue(resultMethod.isPresent());
    Assert.assertEquals("append", resultMethod.get().getName());

    MethodCallExpr expectedRepresentedNode = cu.findAll(MethodCallExpr.class).get(0);
    DataFlowNode expectedDfn = DataFlowNode.builder().name("nodeCall_append_return").representedNode(expectedRepresentedNode).build();
    DataFlowNode expectedInputNode = DataFlowNode.builder().name("arg0").representedNode(cu.findAll(NameExpr.class).get(1)).type("java.lang.String").build();
    ParameterList expectedParameters = ParameterList.builder().name("met_inputParams").nodes(Arrays.asList(expectedInputNode)).build();
    expectedInputNode.setOwner(expectedParameters);
    NodeCall expectedDfm = NodeCall.builder().name("append").representedNode(expectedRepresentedNode).claz("StringBuilder").peckage("java.lang")
        .returnNode(expectedDfn).in(expectedParameters).build();

    Assert.assertEquals(expectedDfn, resultMethod.get().getReturnNode().get());

    Optional<String> m = dfnTest.assertNodeEqual(expectedInputNode, resultMethod.get().getIn().get().getNodes().get(0));
    Assert.assertFalse(m.orElse(""), m.isPresent());

    Assert.assertEquals("Unexpected instanceName", "sb", resultMethod.get().getInstanceName().get());

    Assert.assertEquals(expectedDfm, resultMethod.get());
  }

}
