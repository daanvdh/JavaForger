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
import java.util.HashMap;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;

import dataflow.model.DataFlowGraph;
import dataflow.model.DataFlowMethod;
import dataflow.model.DataFlowNode;
import dataflow.model.NodeCall;
import dataflow.model.ParameterList;

/**
 * Unit test for {@link MethodNodeHandler}.
 *
 * @author Daan
 */
@RunWith(MockitoJUnitRunner.class)
public class MethodNodeHandlerTest {

  @Mock
  private DataFlowResolver resolver;

  @InjectMocks
  MethodNodeHandler sut = new MethodNodeHandler();

  @Test
  public void testHandleMethodCallExpr_inputMethod() {
    String claz = //
        "public class Claz {\n" + //
            "  StringBuilder sb = new StringBuilder(); \n" + //
            "  public StringBuilder met(String a) {\n" + //
            "    return sb.append(a);\n" + //
            "  }\n" + //
            "}"; //
    CompilationUnit cu = JavaParser.parse(claz);
    MethodCallExpr node = cu.findAll(MethodCallExpr.class).iterator().next();

    DataFlowMethod method = DataFlowMethod.builder().build();
    DataFlowGraph graph = DataFlowGraph.builder().build();
    HashMap<Node, DataFlowNode> overriddenValues = new HashMap<>();

    DataFlowNode returnNode = DataFlowNode.builder().name("niceName").build();
    NodeCall methodCall = NodeCall.builder().in(ParameterList.builder().nodes(Arrays.asList(DataFlowNode.builder().name("param1").build())).build())
        .returnNode(returnNode).build();
    Mockito.when(resolver.createNodeCall(graph, method, node)).thenReturn(Optional.of(methodCall));

    Node parameterNode = node.findAll(NameExpr.class).stream().filter(expr -> expr.getNameAsString().equals("a")).findFirst().get();
    Mockito.when(resolver.getDataFlowNode(graph, method, overriddenValues, parameterNode)).thenReturn(Optional.of(DataFlowNode.builder().build()));

    Optional<DataFlowNode> resultNode = sut.handleNode(graph, method, overriddenValues, node);

    Assert.assertTrue(resultNode.isPresent());
    Assert.assertEquals(returnNode, resultNode.get());
    Assert.assertEquals(Arrays.asList(methodCall), method.getCalledMethods());
  }

  @Test
  public void testHandleMethodCallExpr_outputMethod() {
    String claz = //
        "public class Claz {\n" + //
            "  StringBuilder sb = new StringBuilder(); \n" + //
            "  public void met(String a) {\n" + //
            "    sb.append(a);\n" + //
            "  }\n" + //
            "}"; //
    CompilationUnit cu = JavaParser.parse(claz);
    MethodCallExpr node = cu.findAll(MethodCallExpr.class).iterator().next();

    DataFlowMethod method = DataFlowMethod.builder().build();
    DataFlowGraph graph = DataFlowGraph.builder().build();
    HashMap<Node, DataFlowNode> overriddenValues = new HashMap<>();

    DataFlowNode returnNode = DataFlowNode.builder().name("niceName").build();
    NodeCall methodCall = NodeCall.builder().in(ParameterList.builder().nodes(Arrays.asList(DataFlowNode.builder().name("param1").build())).build())
        .returnNode(returnNode).build();
    Mockito.when(resolver.createNodeCall(graph, method, node)).thenReturn(Optional.of(methodCall));

    Node parameterNode = node.findAll(NameExpr.class).stream().filter(expr -> expr.getNameAsString().equals("a")).findFirst().get();
    Mockito.when(resolver.getDataFlowNode(graph, method, overriddenValues, parameterNode)).thenReturn(Optional.of(DataFlowNode.builder().build()));

    Optional<DataFlowNode> resultNode = sut.handleNode(graph, method, overriddenValues, node);

    Assert.assertTrue(resultNode.isPresent());
    Assert.assertEquals(returnNode, resultNode.get());
    Assert.assertEquals(Arrays.asList(methodCall), method.getCalledMethods());
  }

}
