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

import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.FieldDeclaration;

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

    Assert.assertNull("Unexpected javaParserNode", dataFlowNode.getJavaParserNode());
    Assert.assertTrue("Unexpected in", dataFlowNode.getIn().isEmpty());
    Assert.assertTrue("Unexpected out", dataFlowNode.getOut().isEmpty());
    Assert.assertNull("Unexpected name", dataFlowNode.getName());
  }

  @Test
  public void testDataFlowNode_maximum() {
    DataFlowNode dataFlowNode = createAndFillBuilder().build();

    Assert.assertEquals("Unexpected javaParserNode", JAVA_PARSER_NODE, dataFlowNode.getJavaParserNode());
    Assert.assertEquals("Unexpected in", IN, dataFlowNode.getIn());
    Assert.assertEquals("Unexpected out", OUT, dataFlowNode.getOut());
    Assert.assertEquals("Unexpected name", NAME, dataFlowNode.getName());
  }

  private DataFlowNode.Builder createAndFillBuilder() {
    return DataFlowNode.builder().javaParserNode(JAVA_PARSER_NODE).in(IN).out(OUT).name(NAME);
  }

}
