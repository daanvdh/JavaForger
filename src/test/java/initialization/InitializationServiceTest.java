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
 * Unit test for {@link InitializationService}.
 *
 * @author Daan
 */
public class InitializationServiceTest {

  private InitializationService sut = new InitializationService();

  @Test
  public void testInitialize_parameterized() {
    VariableDefinition var = VariableDefinition.builder().type("HashMap<Date, BigDecimal>").build();

    sut.init(var);

    Assert.assertEquals(
        "Collections.singletonMap(Date.from(ZonedDateTime.of(1, 4, 25, 10, 0, 0, 0, TimeZone.getTimeZone(\"UTC\").toZoneId()).toInstant()), BigDecimal.valueOf(3))",
        var.getInit1());
    Assert.assertEquals(
        "Collections.singletonMap(Date.from(ZonedDateTime.of(2, 5, 26, 11, 0, 0, 0, TimeZone.getTimeZone(\"UTC\").toZoneId()).toInstant()), BigDecimal.valueOf(4))",
        var.getInit2());

    Assert.assertTrue(var.getTypeImports().isEmpty());
    Assert.assertThat(var.getInitImports(),
        Matchers.containsInAnyOrder("java.math.BigDecimal", "java.util.Collections", "java.time.ZonedDateTime", "java.util.TimeZone"));
  }

  @Test
  public void testInitialize_parameterizedInParameterized() {
    VariableDefinition var = VariableDefinition.builder().type("Map<HashMap<int, String>, HashMap<Object, double>>").build();

    sut.init(var);

    Assert.assertEquals("Collections.singletonMap(Collections.singletonMap(1, \"a\"), Collections.singletonMap(new Object(), 3.4))", var.getInit1());
    Assert.assertEquals("Collections.singletonMap(Collections.singletonMap(2, \"b\"), Collections.singletonMap(new Object(), 5.6))", var.getInit2());
    Assert.assertTrue(var.getTypeImports().isEmpty());
    Assert.assertThat(var.getInitImports(), Matchers.containsInAnyOrder("java.util.Collections"));
  }

  @Test
  public void testInitialize_parameterizedExtends() {
    VariableDefinition var = VariableDefinition.builder().type("List<? extends BigDecimal>").build();

    sut.init(var);

    Assert.assertEquals("Collections.singletonList(BigDecimal.valueOf(1))", var.getInit1());
    Assert.assertEquals("Collections.singletonList(BigDecimal.valueOf(2))", var.getInit2());
    Assert.assertTrue(var.getTypeImports().isEmpty());
    Assert.assertThat(var.getInitImports(), Matchers.containsInAnyOrder("java.util.Collections", "java.math.BigDecimal"));
  }

}
