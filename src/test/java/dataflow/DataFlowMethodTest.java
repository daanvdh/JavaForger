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
import java.util.Map;
import java.util.function.BiFunction;

import org.junit.Assert;
import org.junit.Test;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;

/**
 * Unit test for {@link DataFlowMethod}.
 *
 * @author Daan
 */
public class DataFlowMethodTest {
  private static final List<DataFlowNode> INPUT_PARAMETERS = Collections.singletonList(DataFlowNode.builder().name("c").build());
  private static final List<DataFlowNode> INPUT_FIELDS = Collections.singletonList(DataFlowNode.builder().name("b").build());
  private static final List<DataFlowNode> CHANGED_FIELDS = Collections.singletonList(DataFlowNode.builder().name("a").build());
  private static final Map<DataFlowNode, DataFlowMethod> INPUT_METHODS =
      Collections.singletonMap(DataFlowNode.builder().name("g").build(), DataFlowMethod.builder().name("f").build());
  private static final List<DataFlowMethod> OUTPUT_METHODS = Collections.singletonList(DataFlowMethod.builder().name("e").build());
  private static final String NAME = "a";
  private static final Node REPRESENTED_NODE = new FieldDeclaration();
  private static final DataFlowNode RETURN_NODE = DataFlowNode.builder().name("d").build();

  @Test
  public void testDataFlowMethod_minimum() {
    DataFlowMethod dataFlowMethod = DataFlowMethod.builder().build();

    Assert.assertTrue("Unexpected inputParameters", dataFlowMethod.getInputParameters().isEmpty());
    Assert.assertTrue("Unexpected inputFields", dataFlowMethod.getInputFields().isEmpty());
    Assert.assertTrue("Unexpected changedFields", dataFlowMethod.getChangedFields().isEmpty());
    Assert.assertTrue("Unexpected inputMethods", dataFlowMethod.getInputMethods().isEmpty());
    Assert.assertTrue("Unexpected outputMethods", dataFlowMethod.getOutputMethods().isEmpty());
  }

  @Test
  public void testDataFlowMethod_maximum() {
    DataFlowMethod dataFlowMethod = createAndFillBuilder().build();

    Assert.assertEquals("Unexpected inputParameters", INPUT_PARAMETERS, dataFlowMethod.getInputParameters());
    Assert.assertEquals("Unexpected inputFields", INPUT_FIELDS, dataFlowMethod.getInputFields());
    Assert.assertEquals("Unexpected changedFields", CHANGED_FIELDS, dataFlowMethod.getChangedFields());
    Assert.assertThat(dataFlowMethod.getInputMethods(), org.hamcrest.Matchers.containsInAnyOrder(INPUT_METHODS.values().iterator().next()));
    Assert.assertEquals("Unexpected outputMethods", OUTPUT_METHODS, dataFlowMethod.getOutputMethods());
  }

  @Test
  public void testHashCode_Same() {
    DataFlowMethod.Builder builder = createAndFillBuilder();
    DataFlowMethod a = builder.build();
    DataFlowMethod b = builder.build();
    Assert.assertEquals("Expected hash code to be the same", a.hashCode(), b.hashCode());
  }

  @Test
  public void testHashCode_Different() {
    verifyHashCode_Different(DataFlowMethod.Builder::name, "b");
    verifyHashCode_Different(DataFlowMethod.Builder::representedNode, new MethodDeclaration());
    verifyHashCode_Different(DataFlowMethod.Builder::returnNode, DataFlowNode.builder().build());
    verifyHashCode_Different(DataFlowMethod.Builder::inputParameters, Collections.singletonList(DataFlowNode.builder().build()));
    verifyHashCode_Different(DataFlowMethod.Builder::inputFields, Collections.singletonList(DataFlowNode.builder().build()));
    verifyHashCode_Different(DataFlowMethod.Builder::changedFields, Collections.singletonList(DataFlowNode.builder().build()));
    verifyHashCode_Different(DataFlowMethod.Builder::inputMethods, Collections.singletonMap(DataFlowNode.builder().build(), DataFlowMethod.builder().build()));
    verifyHashCode_Different(DataFlowMethod.Builder::outputMethods, Collections.singletonList(DataFlowMethod.builder().build()));
  }

  @Test
  public void testEquals_Same() {
    DataFlowMethod.Builder builder = createAndFillBuilder();
    DataFlowMethod a = builder.build();
    DataFlowMethod b = builder.build();
    Assert.assertTrue("Expected a and b to be equal", a.equals(b));
  }

  @Test
  public void testEquals_Different() {
    verifyEqualsDifferent(DataFlowMethod.Builder::name, "b");
    verifyEqualsDifferent(DataFlowMethod.Builder::representedNode, new MethodDeclaration());
    verifyEqualsDifferent(DataFlowMethod.Builder::returnNode, DataFlowNode.builder().build());
    verifyEqualsDifferent(DataFlowMethod.Builder::inputParameters, Collections.singletonList(DataFlowNode.builder().build()));
    verifyEqualsDifferent(DataFlowMethod.Builder::inputFields, Collections.singletonList(DataFlowNode.builder().build()));
    verifyEqualsDifferent(DataFlowMethod.Builder::changedFields, Collections.singletonList(DataFlowNode.builder().build()));
    verifyEqualsDifferent(DataFlowMethod.Builder::inputMethods, Collections.singletonMap(DataFlowNode.builder().build(), DataFlowMethod.builder().build()));
    verifyEqualsDifferent(DataFlowMethod.Builder::outputMethods, Collections.singletonList(DataFlowMethod.builder().build()));
  }

  private DataFlowMethod.Builder createAndFillBuilder() {
    return DataFlowMethod.builder().name(NAME).representedNode(REPRESENTED_NODE).returnNode(RETURN_NODE).inputParameters(INPUT_PARAMETERS)
        .inputFields(INPUT_FIELDS).changedFields(CHANGED_FIELDS).inputMethods(INPUT_METHODS).outputMethods(OUTPUT_METHODS);
  }

  private <T> void verifyHashCode_Different(BiFunction<DataFlowMethod.Builder, T, DataFlowMethod.Builder> withMapper, T argument) {
    DataFlowMethod.Builder builder = createAndFillBuilder();
    DataFlowMethod a = builder.build();
    DataFlowMethod b = withMapper.apply(builder, argument).build();
    Assert.assertNotEquals("Expected hash code to be different", a.hashCode(), b.hashCode());
  }

  private <T> void verifyEqualsDifferent(BiFunction<DataFlowMethod.Builder, T, DataFlowMethod.Builder> withMapper, T argument) {
    DataFlowMethod.Builder builder = createAndFillBuilder();
    DataFlowMethod a = builder.build();
    DataFlowMethod b = withMapper.apply(builder, argument).build();
    Assert.assertFalse("Expected a and b not to be equal", a.equals(b));
  }

}
