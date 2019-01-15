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
import java.util.Collections;
import java.util.List;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import common.SymbolSolverSetup;
import templateInput.definition.VariableDefinition;

/**
 * Unit test for {@link FieldReader}.
 *
 * @author Daan
 */
public class FieldReaderTest {
  private static final String INPUT_CLASS = "src/test/java/inputClassesForTests/Product.java";

  @Before
  public void setup() {
    SymbolSolverSetup.setup();
  }

  @Test
  public void testGetFields() throws IOException {
    FieldReader reader = new FieldReader();
    List<VariableDefinition> variables = reader.getFields(INPUT_CLASS, SymbolSolverSetup.getDefaultConfig());

    VariableDefinition v1 = VariableDefinition.builder().withName("url").withType("String").withLineNumber(32).withColumn(3)
        .withAccessModifiers(Collections.singleton("private")).build();
    VariableDefinition v2 = VariableDefinition.builder().withName("name").withType("String").withLineNumber(33).withColumn(3)
        .withAccessModifiers(Collections.singleton("private")).build();
    VariableDefinition v3 = VariableDefinition.builder().withName("prod").withType("Product").withLineNumber(34).withColumn(3)
        .withAccessModifiers(Collections.singleton("public")).withTypeImport("inputClassesForTests.Product").build();

    Assert.assertThat(variables, Matchers.contains(v1, v2, v3));
  }

}
