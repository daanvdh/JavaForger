/*
 * Copyright 2018 by Daan van den Heuvel.
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
 */package templateInput.definition;

import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Unit test for {@link MethodDefinition}.
 *
 * @author Daan
 */
@RunWith(MockitoJUnitRunner.class)
public class MethodDefinitionTest {
  private static final List<VariableDefinition> PARAMETERS = Collections.singletonList(VariableDefinition.builder().build());
  private static final List<FlowReceiverDefinition> CHANGED_FIELDS = Collections.singletonList(FlowReceiverDefinition.builder().build());
  private static final List<MethodDefinition> INPUT_METHODS = Collections.singletonList(MethodDefinition.builder().build());
  private static final List<MethodDefinition> OUTPUT_METHODS = Collections.singletonList(MethodDefinition.builder().build());
  private static final String METHOD_SIGNATURE = "a";
  private static final String RETURN_SIGNATURE = "c";

  @Test
  public void testMethodDefinition_minimum() {
    MethodDefinition methodDefinition = MethodDefinition.builder().build();

    Assert.assertTrue("Unexpected parameters", methodDefinition.getParameters().isEmpty());
    Assert.assertTrue("Unexpected changedFields", methodDefinition.getChangedFields().isEmpty());
    Assert.assertTrue("Unexpected inputMethods", methodDefinition.getInputMethods().isEmpty());
    Assert.assertTrue("Unexpected outputMethods", methodDefinition.getOutputMethods().isEmpty());
    Assert.assertNull("Unexpected methodSignature", methodDefinition.getMethodSignature());
    Assert.assertNull("Unexpected returnSignature", methodDefinition.getReturnSignature());
  }

  @Test
  public void testMethodDefinition_maximum() {
    MethodDefinition methodDefinition = createAndFillBuilder().build();

    Assert.assertEquals("Unexpected parameters", PARAMETERS, methodDefinition.getParameters());
    Assert.assertEquals("Unexpected changedFields", CHANGED_FIELDS, methodDefinition.getChangedFields());
    Assert.assertEquals("Unexpected inputMethods", INPUT_METHODS, methodDefinition.getInputMethods());
    Assert.assertEquals("Unexpected outputMethods", OUTPUT_METHODS, methodDefinition.getOutputMethods());
    Assert.assertEquals("Unexpected methodSignature", METHOD_SIGNATURE, methodDefinition.getMethodSignature());
    Assert.assertEquals("Unexpected returnSignature", RETURN_SIGNATURE, methodDefinition.getReturnSignature());
  }

  private MethodDefinition.Builder createAndFillBuilder() {
    return MethodDefinition.builder().parameters(PARAMETERS).changedFields(CHANGED_FIELDS).inputMethods(INPUT_METHODS).outputMethods(OUTPUT_METHODS)
        .methodSignature(METHOD_SIGNATURE).returnSignature(RETURN_SIGNATURE);
  }

}
