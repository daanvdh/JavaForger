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
package inputClassesForTests;import java.util.function.BiFunction;
import org.junit.Assert;
import org.junit.Test;
import inputClassesForTests.CLassWithEverything;



/**
 * Input class for unit tests.
 *
 * @author Daan
 */
public class CLassWithEverythingTest {

@Test
  public void testEquals_Same() {
    CLassWithEverything.Builder builder = createAndFillBuilder();
    CLassWithEverything a = builder.build();
    CLassWithEverything b = builder.build();
    Assert.assertTrue("Expected a and b to be equal", a.equals(b));
  }

@Test
  public void testEquals_Different() {
    verifyEqualsDifferent(CLassWithEverything.Builder::i, 1);
    verifyEqualsDifferent(CLassWithEverything.Builder::c, CLassWithEverything.builder().build());
    verifyEqualsDifferent(CLassWithEverything.Builder::s, "a");
  }

private <T> void verifyEqualsDifferent(BiFunction<CLassWithEverything.Builder, T, CLassWithEverything.Builder> withMapper, T argument) {
    CLassWithEverything.Builder builder = createAndFillBuilder();
    CLassWithEverything a = builder.build();
    CLassWithEverything b = withMapper.apply(builder, argument).build();
    Assert.assertFalse("Expected a and b not to be equal", a.equals(b));
  }

private CLassWithEverything.Builder createAndFillBuilder() {
    return CLassWithEverything.builder()
      .i(1)
      .c(CLassWithEverything.builder().build())
      .s("a")
;
  }

}
