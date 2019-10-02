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
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.util.collections.Sets;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;

import common.SymbolSolverSetup;
import dataflow.model.DataFlowGraph;
import dataflow.model.DataFlowMethod;
import dataflow.model.DataFlowNode;
import dataflow.model.ParameterList;

/**
 * Unit test for {@link DataFlowResolver}.
 *
 * @author Daan
 */
public class DataFlowResolverTest {

  DataFlowResolver sut = new DataFlowResolver();

  @Before
  public void setup() {
    // TODO remove dependency to JavaForger
    SymbolSolverSetup.setup();
  }

  @Test
  public void testGetDataFlowMethod() {
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
    DataFlowMethod method = DataFlowMethod.builder().build();
    MethodCallExpr node = methodCalls.get(0);

    Optional<DataFlowMethod> resultMethod = sut.getDataFlowMethod(graph, method, node);

    Assert.assertTrue(resultMethod.isPresent());
    Assert.assertEquals("append", resultMethod.get().getName());
    Map<String, DataFlowGraph> dependedGraphs = graph.getDependedGraphs();
    Assert.assertEquals(1, dependedGraphs.size());
    String qualifiedPath = "java.lang.StringBuilder";
    Assert.assertEquals(Sets.newSet(qualifiedPath), dependedGraphs.keySet());
    DataFlowGraph newDfg = dependedGraphs.get(qualifiedPath);
    Assert.assertEquals("StringBuilder", newDfg.getName());
    Assert.assertEquals("java.lang", newDfg.getClassPackage());
    Assert.assertEquals(1, newDfg.getMethods().size());
    MethodDeclaration expectedRepresentedNode =
        new MethodDeclaration(EnumSet.of(Modifier.PUBLIC), new ClassOrInterfaceType(), "java.lang.StringBuilder.append(java.lang.String)");
    Assert.assertEquals("Only contained keys " + newDfg.getMethodMap().keySet(), resultMethod.get(), newDfg.getMethod(expectedRepresentedNode));

    DataFlowMethod expectedDfm = DataFlowMethod.builder().name("append").representedNode(expectedRepresentedNode).graph(newDfg)
        .inputParameters(ParameterList.builder().nodes(Arrays.asList(DataFlowNode.builder().name("arg0").type("java.lang.String").build())).build()).build();
    Assert.assertEquals(expectedDfm, resultMethod.get());
  }

}
