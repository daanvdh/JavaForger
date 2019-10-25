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
import org.junit.Assert;
import org.junit.Test;

/**
 * Input class for unit tests.
 *
 * @author Daan
 */
public class ClassWithEverythingTest {
  @Mock
  private StringBuilder sb; 
  @InjectMocks
  private Claz sut = new Claz(); 
  @Test
  public void testSetS() {

    String a = "a"; 


    StringBuilder returnAppend = StringBuilder.builder().build();
    Mockito.when(sb.append()).thenReturn(returnAppend);

    StringBuilder return_setS = sut.setS(
      a        );
    
    Assert.assertEquals("Unexpected setS", returnAppend, return_setS);
    
  }

}
