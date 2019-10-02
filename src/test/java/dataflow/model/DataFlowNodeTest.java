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
package dataflow.model;

import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;

import org.junit.Assert;
import org.junit.Test;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;

/**
 * Unit test for {@link DataFlowNode}.
 *
 * @author Daan
 */
public class DataFlowNodeTest {
  private static final Node JAVA_PARSER_NODE = new FieldDeclaration();
  private static final List<DataFlowEdge> IN = Collections.singletonList(DataFlowEdge.builder().build());
  private static final List<DataFlowEdge> OUT = Collections.singletonList(DataFlowEdge.builder().build());
  private static final String NAME = "a";

  @Test
  public void testDataFlowNode_minimum() {
    DataFlowNode dataFlowNode = DataFlowNode.builder().build();

    Assert.assertNull("Unexpected javaParserNode", dataFlowNode.getRepresentedNode());
    Assert.assertTrue("Unexpected in", dataFlowNode.getIn().isEmpty());
    Assert.assertTrue("Unexpected out", dataFlowNode.getOut().isEmpty());
    Assert.assertNull("Unexpected name", dataFlowNode.getName());
  }

  @Test
  public void testDataFlowNode_maximum() {
    DataFlowNode dataFlowNode = createAndFillBuilder().build();

    Assert.assertEquals("Unexpected javaParserNode", JAVA_PARSER_NODE, dataFlowNode.getRepresentedNode());
    Assert.assertEquals("Unexpected in", IN, dataFlowNode.getIn());
    Assert.assertEquals("Unexpected out", OUT, dataFlowNode.getOut());
    Assert.assertEquals("Unexpected name", NAME, dataFlowNode.getName());
  }

  @Test
  public void testEquals_Same() {
    DataFlowNode.Builder builder = createAndFillBuilder();
    DataFlowNode a = builder.build();
    DataFlowNode b = builder.build();
    Assert.assertTrue("Expected a and b to be equal", a.equals(b));
  }

  @Test
  public void testEquals_Different() {
    verifyEqualsDifferent(DataFlowNode.Builder::name, "b");
    verifyEqualsDifferent(DataFlowNode.Builder::representedNode, new MethodDeclaration());
    verifyEqualsDifferent(DataFlowNode.Builder::in, Collections.singletonList(DataFlowEdge.builder().build()));
    verifyEqualsDifferent(DataFlowNode.Builder::out, Collections.singletonList(DataFlowEdge.builder().build()));
  }

  @Test
  public void testHashCode_Same() {
    DataFlowNode.Builder builder = createAndFillBuilder();
    DataFlowNode a = builder.build();
    DataFlowNode b = builder.build();
    Assert.assertEquals("Expected hash code to be the same", a.hashCode(), b.hashCode());
  }

  @Test
  public void testHashCode_Different() {
    verifyHashCode_Different(DataFlowNode.Builder::name, "b");
    verifyHashCode_Different(DataFlowNode.Builder::representedNode, new MethodDeclaration());
    verifyHashCode_Different(DataFlowNode.Builder::in, Collections.singletonList(DataFlowEdge.builder().build()));
    verifyHashCode_Different(DataFlowNode.Builder::out, Collections.singletonList(DataFlowEdge.builder().build()));
  }

  private DataFlowNode.Builder createAndFillBuilder() {
    return DataFlowNode.builder().representedNode(JAVA_PARSER_NODE).in(IN).out(OUT).name(NAME);
  }

  private <T> void verifyEqualsDifferent(BiFunction<DataFlowNode.Builder, T, DataFlowNode.Builder> withMapper, T argument) {
    DataFlowNode.Builder builder = createAndFillBuilder();
    DataFlowNode a = builder.build();
    DataFlowNode b = withMapper.apply(builder, argument).build();
    Assert.assertFalse("Expected a and b not to be equal", a.equals(b));
  }

  private <T> void verifyHashCode_Different(BiFunction<DataFlowNode.Builder, T, DataFlowNode.Builder> withMapper, T argument) {
    DataFlowNode.Builder builder = createAndFillBuilder();
    DataFlowNode a = builder.build();
    DataFlowNode b = withMapper.apply(builder, argument).build();
    Assert.assertNotEquals("Expected hash code to be different", a.hashCode(), b.hashCode());
  }

}
