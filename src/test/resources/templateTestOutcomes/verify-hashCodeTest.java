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



/**
 * Input class for unit tests.
 *
 * @author Daan
 */
public class CLassWithEverythingTest {

@Test
  public void testHashCode_Same() {
    CLassWithEverything.Builder builder = createAndFillBuilder();
    CLassWithEverything a = builder.build();
    CLassWithEverything b = builder.build();
    Assert.assertEquals("Expected hash code to be the same", a.hashCode(), b.hashCode());
  }

@Test
  public void testHashCode_Different() {
    verifyHashCode_Different(CLassWithEverything.Builder::i, 2);
    verifyHashCode_Different(CLassWithEverything.Builder::c, CLassWithEverything.builder().build());
    verifyHashCode_Different(CLassWithEverything.Builder::s, "b");
  }

private <T> void verifyHashCode_Different(BiFunction<CLassWithEverything.Builder, T, CLassWithEverything.Builder> withMapper, T argument) {
    CLassWithEverything.Builder builder = createAndFillBuilder();
    CLassWithEverything a = builder.build();
    CLassWithEverything b = withMapper.apply(builder, argument).build();
    Assert.assertNotEquals("Expected hash code to be different", a.hashCode(), b.hashCode());
  }

private CLassWithEverything.Builder createAndFillBuilder() {
    return CLassWithEverything.builder()
      .i(1)
      .c(CLassWithEverything.builder().build())
      .s("a")
      ;
  }

}
