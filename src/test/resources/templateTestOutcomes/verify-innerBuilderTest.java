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



/**
 * Input class for unit tests.
 *
 * @author Daan
 */
public class CLassWithEverythingTest {

@Test 
public void testCLassWithEverything_minimum() {
CLassWithEverything cLassWithEverything= CLassWithEverything.builder().build();

Assert.assertEquals("Unexpected i", 0, cLassWithEverything.getI());
Assert.assertNull("Unexpected c", cLassWithEverything.getC());
Assert.assertNull("Unexpected s", cLassWithEverything.getS());
}

@Test 
public void testCLassWithEverything_maximum() {
CLassWithEverything cLassWithEverything= CLassWithEverything.builder()
.i(I)
.c(C)
.s(S)
.build();

Assert.assertEquals("Unexpected i", I, cLassWithEverything.getI());
Assert.assertEquals("Unexpected c", C, cLassWithEverything.getC());
Assert.assertEquals("Unexpected s", S, cLassWithEverything.getS());
}

private static final Integer I = 1;

private static final CLassWithEverything C = CLassWithEverything.builder().build();

private static final String S = "a";

}
