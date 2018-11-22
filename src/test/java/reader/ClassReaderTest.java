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
package reader;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import templateInput.ClassDefinition;

/**
 * Unit test for {@link ClassReader}.
 *
 * @author Daan
 */
public class ClassReaderTest {
  private static final String INPUT_CLASS = "C:/gitrepo/JavaForger/src/test/java/inputClassesForTests/ExtendedProduct.java";

  private ClassReader cr = new ClassReader();

  @Test
  public void testRead() throws IOException {
    ClassDefinition c = cr.read(INPUT_CLASS);

    Set<String> mod = new HashSet<>();
    mod.add("public");
    ClassDefinition expected = ClassDefinition.builder().withName("ExtendedProduct").withType("ExtendedProduct").withLineNumber(25).withColumn(1)
        .withAccessModifiers(mod).withExtend("Product").withInterfaces(Collections.singletonList("TestInterface")).build();

    assertEquals(expected, c);
  }

}
