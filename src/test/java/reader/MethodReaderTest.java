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

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import templateInput.MethodDefinition;
import templateInput.MethodDefinition.Builder;

/**
 * Unit test for {@link MethodReader}
 *
 * @author Daan
 */
public class MethodReaderTest {
  private static final String INPUT_CLASS = "src/test/java/inputClassesForTests/Product.java";

  private MethodReader mr = new MethodReader();

  @Test
  public void testRead() throws IOException {
    List<MethodDefinition> methods = mr.read(INPUT_CLASS);

    Builder build = MethodDefinition.builder().withAccessModifiers(Collections.singleton("public")).withType("String");
    MethodDefinition m1 = build.withName("getUrl").withLineNumber(40).withColumn(3).build();
    MethodDefinition m2 = build.withName("getName").withLineNumber(44).withColumn(3).build();
    MethodDefinition m3 = build.withName("toString").withLineNumber(48).withColumn(3).withAnnotations(Collections.singleton("Override")).build();
    MethodDefinition m4 =
        build.withName("hashCode").withLineNumber(53).withColumn(3).withAnnotations(Collections.singleton("Override")).withType("int").build();
    MethodDefinition m5 =
        build.withName("equals").withLineNumber(58).withColumn(3).withAnnotations(Collections.singleton("Override")).withType("boolean").build();

    Assert.assertEquals(Arrays.asList(m1, m2, m3, m4, m5), methods);
  }

}
