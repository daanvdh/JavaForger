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
 */
package reader;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import generator.JavaForgerConfiguration;
import templateInput.VariableDefinition;

/**
 * Unit test for {@link FieldReader}.
 *
 * @author Daan
 */
public class FieldReaderTest {
  private static final String INPUT_CLASS = "src/test/java/inputClassesForTests/Product.java";

  @Test
  public void testGetFields() throws IOException {
    FieldReader reader = new FieldReader();
    List<VariableDefinition> variables = reader.getFields(JavaForgerConfiguration.builder().build(), "src/test/java/inputClassesForTests/Product.java");

    VariableDefinition v1 = VariableDefinition.builder().withName("url").withType("String").withLineNumber(32).withColumn(3)
        .withAccessModifiers(Collections.singleton("private")).build();
    VariableDefinition v2 = VariableDefinition.builder().withName("name").withType("String").withLineNumber(33).withColumn(3)
        .withAccessModifiers(Collections.singleton("private")).build();
    Assert.assertEquals(Arrays.asList(v1, v2), variables);
  }

  @Test
  public void testRead() throws IOException {
    FieldReader reader = new FieldReader();
    List<VariableDefinition> variables = reader.read(INPUT_CLASS);

    VariableDefinition v1 = VariableDefinition.builder().withName("url").withType("String").withLineNumber(32).withColumn(3)
        .withAccessModifiers(Collections.singleton("private")).build();
    VariableDefinition v2 = VariableDefinition.builder().withName("name").withType("String").withLineNumber(33).withColumn(3)
        .withAccessModifiers(Collections.singleton("private")).build();

    Assert.assertEquals(Arrays.asList(v1, v2), variables);
  }

}
