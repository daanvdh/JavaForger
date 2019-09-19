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
 * Unit test for {@link FieldFactory}.
 *
 * @author Daan
 */
public class FieldFactoryTest {

  private ClassContainerReader sut = new ClassContainerReader();

  @Before
  public void setup() {
    SymbolSolverSetup.setup();
  }

  @Test
  public void testRead_Fields() throws IOException {
    String input = "src/test/java/inputClassesForTests/Product.java";
    List<? extends VariableDefinition> variables = sut.read(input).getFields();

    VariableDefinition v1 = VariableDefinition.builder().withName("url").withType("String").withLineNumber(32).withColumn(3)
        .withAccessModifiers(Collections.singleton("private")).build();
    VariableDefinition v2 = VariableDefinition.builder().withName("name").withType("String").withLineNumber(33).withColumn(3)
        .withAccessModifiers(Collections.singleton("private")).originalInit("\"interesting\"").build();
    VariableDefinition v3 = VariableDefinition.builder().withName("prod").withType("Product").withLineNumber(34).withColumn(3)
        .withAccessModifiers(Collections.singleton("public")).withTypeImport("inputClassesForTests.Product").build();

    Assert.assertThat(variables, Matchers.contains(v1, v2, v3));
  }

}
