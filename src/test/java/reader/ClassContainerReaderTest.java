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
import java.util.List;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import common.SymbolSolverSetup;
import templateInput.ClassContainer;
import templateInput.definition.AnnotationDefinition;
import templateInput.definition.ClassDefinition;
import templateInput.definition.MethodDefinition;
import templateInput.definition.MethodDefinition.Builder;
import templateInput.definition.VariableDefinition;

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

    ClassDefinition expected = ClassDefinition.builder().withName("ExtendedProduct").withType("ExtendedProduct").withLineNumber(25).withColumn(1)
        .withAccessModifiers(Collections.singleton("public")).withExtend("Product").withInterfaces(Collections.singletonList("TestInterface")).build();

    assertEquals(expected, result);
  }

  @Test
  public void testRead_Methods() throws IOException {
    String input = "src/test/java/inputClassesForTests/Product.java";
    List<? extends MethodDefinition> methods = sut.read(input).getMethods();

    Builder build = MethodDefinition.builder().withAccessModifiers(Collections.singleton("public")).withType("String");
    MethodDefinition m1 = build.withName("getUrl").withLineNumber(46).withColumn(3).build();
    MethodDefinition m2 = build.withName("getName").withLineNumber(50).withColumn(3).build();
    MethodDefinition m3 =
        build.withName("toString").withLineNumber(54).withColumn(3).withAnnotations(Collections.singleton(new AnnotationDefinition("Override"))).build();
    MethodDefinition m4 = build.withName("hashCode").withLineNumber(59).withColumn(3)
        .withAnnotations(Collections.singleton(new AnnotationDefinition("Override"))).withType("int").build();
    MethodDefinition m5 = build.withName("equals").withLineNumber(64).withColumn(3).withAnnotations(Collections.singleton(new AnnotationDefinition("Override")))
        .withType("boolean").withParameters(VariableDefinition.builder().withType("Object").withName("obj").build()).build();

    Assert.assertThat(methods, Matchers.contains(m1, m2, m3, m4, m5));
  }

  @Test
  public void testRead_Constructors() throws IOException {
    String input = "src/test/java/inputClassesForTests/Product.java";
    List<? extends MethodDefinition> constructors = sut.read(input).getConstructors();

    Builder build = MethodDefinition.builder().withAccessModifiers(Collections.singleton("public")).withType("Product").withName("Product").withColumn(3)
        .withTypeImports("inputClassesForTests");
    MethodDefinition m1 = build.withLineNumber(36).withParameters(VariableDefinition.builder().withType("String").withName("name2").build(),
        VariableDefinition.builder().withType("String").withName("url").build()).build();
    MethodDefinition m2 = build.withLineNumber(42).withParameters().build();

    Assert.assertThat(constructors, Matchers.contains(m1, m2));
  }

  @Test
  public void testRead_Fields() throws IOException {
    String input = "src/test/java/inputClassesForTests/Product.java";
    List<? extends VariableDefinition> variables = sut.read(input).getFields();

    VariableDefinition v1 = VariableDefinition.builder().withName("url").withType("String").withLineNumber(32).withColumn(3)
        .withAccessModifiers(Collections.singleton("private")).build();
    VariableDefinition v2 = VariableDefinition.builder().withName("name").withType("String").withLineNumber(33).withColumn(3)
        .withAccessModifiers(Collections.singleton("private")).build();
    VariableDefinition v3 = VariableDefinition.builder().withName("prod").withType("Product").withLineNumber(34).withColumn(3)
        .withAccessModifiers(Collections.singleton("public")).withTypeImport("inputClassesForTests.Product").build();

    Assert.assertThat(variables, Matchers.contains(v1, v2, v3));
  }

}
