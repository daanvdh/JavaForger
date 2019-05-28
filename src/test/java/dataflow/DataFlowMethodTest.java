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

/**
 * Unit test for {@link DataFlowMethod}.
 *
 * @author Daan
 */
public class DataFlowMethodTest {
  private static final List<DataFlowNode> INPUT_PARAMETERS = Collections.singletonList(DataFlowNode.builder().build());
  private static final List<DataFlowNode> INPUT_FIELDS = Collections.singletonList(DataFlowNode.builder().build());
  private static final List<DataFlowNode> CHANGED_FIELDS = Collections.singletonList(DataFlowNode.builder().build());
  private static final List<DataFlowMethod> INPUT_METHODS = Collections.singletonList(DataFlowMethod.builder().build());
  private static final List<DataFlowMethod> OUTPUT_METHODS = Collections.singletonList(DataFlowMethod.builder().build());
  private static final String NAME = "a";

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
    Assert.assertEquals("Unexpected inputMethods", INPUT_METHODS, dataFlowMethod.getInputMethods());
    Assert.assertEquals("Unexpected outputMethods", OUTPUT_METHODS, dataFlowMethod.getOutputMethods());
  }

  private DataFlowMethod.Builder createAndFillBuilder() {
    return DataFlowMethod.builder().inputParameters(INPUT_PARAMETERS).inputFields(INPUT_FIELDS).changedFields(CHANGED_FIELDS).inputMethods(INPUT_METHODS)
        .outputMethods(OUTPUT_METHODS);
  }

}
