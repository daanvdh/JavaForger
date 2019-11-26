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
 * Unit test for {@link VariableDefintionFactory}.
 *
 * @author Daan
 */
public class VariableDefintionFactoryTest {

  private ClassContainerReader sut = new ClassContainerReader();

  @Before
  public void setup() {
    SymbolSolverSetup.setup();
  }

  @Test
  public void testRead_Fields() throws IOException {
    String input = "src/test/java/inputClassesForTests/Product.java";
    List<? extends VariableDefinition> variables = sut.read(input).getFields();

    VariableDefinition v1 =
        VariableDefinition.builder().name("url").type("String").lineNumber(32).column(3).accessModifiers(Collections.singleton("private")).build();
    VariableDefinition v2 = VariableDefinition.builder().name("name").type("String").lineNumber(33).column(3).accessModifiers(Collections.singleton("private"))
        .originalInit("\"interesting\"").build();
    VariableDefinition v3 = VariableDefinition.builder().name("prod").type("Product").lineNumber(34).column(3).accessModifiers(Collections.singleton("public"))
        .typeImport("inputClassesForTests.Product").build();

    Assert.assertThat(variables, Matchers.contains(v1, v2, v3));
  }

}
