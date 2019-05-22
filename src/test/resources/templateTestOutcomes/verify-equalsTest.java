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
package inputClassesForTests;
import java.util.function.BiFunction;
import org.junit.Assert;
import org.junit.Test;
import inputClassesForTests.Product;
import java.util.Set;
import java.util.Collections;
import inputClassesForTests.ClassWithEverything;

/**
 * Input class for unit tests.
 *
 * @author Daan
 */
public class ClassWithEverythingTest {
  private static final Set<Product> PROD = Collections.singleton(Product.builder().build());
  private static final int I = 1;
  private static final ClassWithEverything C = ClassWithEverything.builder().build();
  private static final String S = "a";
  @Test
  public void testEquals_Same() {
    ClassWithEverything.Builder builder = createAndFillBuilder();
    ClassWithEverything a = builder.build();
    ClassWithEverything b = builder.build();
    Assert.assertTrue("Expected a and b to be equal", a.equals(b));
  }
  @Test
  public void testEquals_Different() {
    verifyEqualsDifferent(ClassWithEverything.Builder::prod, Collections.singleton(Product.builder().build()));
    verifyEqualsDifferent(ClassWithEverything.Builder::i, 2);
    verifyEqualsDifferent(ClassWithEverything.Builder::c, ClassWithEverything.builder().build());
    verifyEqualsDifferent(ClassWithEverything.Builder::s, "b");
  }
  private <T> void verifyEqualsDifferent(BiFunction<ClassWithEverything.Builder, T, ClassWithEverything.Builder> withMapper, T argument) {
    ClassWithEverything.Builder builder = createAndFillBuilder();
    ClassWithEverything a = builder.build();
    ClassWithEverything b = withMapper.apply(builder, argument).build();
    Assert.assertFalse("Expected a and b not to be equal", a.equals(b));
  }
  private ClassWithEverything.Builder createAndFillBuilder() {
    return ClassWithEverything.builder()
    .prod(PROD)
    .i(I)
    .c(C)
    .s(S)
    ;
  }

}
