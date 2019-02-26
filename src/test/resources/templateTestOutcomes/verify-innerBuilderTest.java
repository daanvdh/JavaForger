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
package inputClassesForTests;import org.junit.Assert;
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

@Test 
  public void testClassWithEverything_minimum() {
    ClassWithEverything classWithEverything= ClassWithEverything.builder().build();

    Assert.assertTrue("Unexpected prod", classWithEverything.getProd().isEmpty());
    Assert.assertEquals("Unexpected i", 0, classWithEverything.getI());
    Assert.assertNull("Unexpected c", classWithEverything.getC());
    Assert.assertNull("Unexpected s", classWithEverything.getS());
  }

@Test 
  public void testClassWithEverything_maximum() {
    ClassWithEverything classWithEverything= createAndFillBuilder().build(); 

    Assert.assertEquals("Unexpected prod", PROD, classWithEverything.getProd());
    Assert.assertEquals("Unexpected i", I, classWithEverything.getI());
    Assert.assertEquals("Unexpected c", C, classWithEverything.getC());
    Assert.assertEquals("Unexpected s", S, classWithEverything.getS());
}

private ClassWithEverything.Builder createAndFillBuilder() {
    return ClassWithEverything.builder()
    .prod(PROD)
    .i(I)
    .c(C)
    .s(S)
    ;
  }

private static final Set<Product> PROD = Collections.singleton(Product.builder().build());

private static final int I = 1;

private static final ClassWithEverything C = ClassWithEverything.builder().build();

private static final String S = "a";

}
