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
package initialization;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

import templateInput.definition.VariableDefinition;

/**
 * Unit test for {@link VariableInitializer}.
 *
 * @author Daan
 */
public class VariableInitializerTest {

  private VariableInitializer sut = new VariableInitializer();

  @Test
  public void testInit_parameterized() {
    VariableDefinition var = VariableDefinition.builder().withType("HashMap<Date, BigDecimal>").build();

    sut.init(var);

    Assert.assertEquals(
        "Collections.singletonMap(Date.from(ZonedDateTime.of(2017, 4, 25, 10, 0, 0, 0, TimeZone.getTimeZone(\"UTC\").toZoneId()).toInstant()), BigDecimal.valueOf(5))",
        var.getInit1());
    Assert.assertEquals(
        "Collections.singletonMap(Date.from(ZonedDateTime.of(2018, 5, 26, 11, 0, 0, 0, TimeZone.getTimeZone(\"UTC\").toZoneId()).toInstant()), BigDecimal.valueOf(6))",
        var.getInit2());

    Assert.assertTrue(var.getTypeImports().isEmpty());
    Assert.assertThat(var.getInitImports(),
        Matchers.containsInAnyOrder("java.math.BigDecimal", "java.util.Collections", "java.time.ZonedDateTime", "java.util.TimeZone"));
  }

  @Test
  public void testInit_parameterizedInParameterized() {
    VariableDefinition var = VariableDefinition.builder().withType("Map<HashMap<int, String>, HashMap<Object, double>>").build();

    sut.init(var);

    Assert.assertEquals("Collections.singletonMap(Collections.singletonMap(1, \"a\"), Collections.singletonMap(new Object(), 1.0))", var.getInit1());
    Assert.assertEquals("Collections.singletonMap(Collections.singletonMap(2, \"b\"), Collections.singletonMap(new Object(), 2.0))", var.getInit2());
    Assert.assertTrue(var.getTypeImports().isEmpty());
    Assert.assertThat(var.getInitImports(), Matchers.containsInAnyOrder("java.util.Collections"));
  }

  @Test
  public void testInit_parameterizedExtends() {
    VariableDefinition var = VariableDefinition.builder().withType("List<? extends BigDecimal>").build();

    sut.init(var);

    Assert.assertEquals("Collections.singletonList(BigDecimal.valueOf(5))", var.getInit1());
    Assert.assertEquals("Collections.singletonList(BigDecimal.valueOf(6))", var.getInit2());
    Assert.assertTrue(var.getTypeImports().isEmpty());
    Assert.assertThat(var.getInitImports(), Matchers.containsInAnyOrder("java.util.Collections", "java.math.BigDecimal"));
  }

}
