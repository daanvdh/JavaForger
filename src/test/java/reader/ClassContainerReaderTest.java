/*
 * Copyright 2019 by Daan van den Heuvel.
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

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;

import common.SymbolSolverSetup;
import templateInput.ClassContainer;
import templateInput.definition.ClassDefinition;

/**
 * Unit test for the {@link ClassContainerReader}.
 *
 * @author Daan
 */
public class ClassContainerReaderTest {

  private ClassContainerReader sut = new ClassContainerReader();

  @Before
  public void setup() {
    SymbolSolverSetup.setup();
  }

  @Test
  public void testRead_Class() throws IOException {
    String input = "src/test/java/inputClassesForTests/ExtendedProduct.java";
    ClassContainer cc = sut.read(input);
    ClassDefinition result = ClassDefinition.builder(cc).build();

    ClassDefinition expected = ClassDefinition.builder().name("ExtendedProduct").type("ExtendedProduct").lineNumber(25).column(1)
        .accessModifiers(Collections.singleton("public")).extend("Product").interfaces(Collections.singletonList("TestInterface")).build();

    assertEquals(expected, result);
  }

}
