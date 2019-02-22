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
package merger;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import configuration.JavaForgerConfiguration;
import generator.CodeSnipit;

/**
 * Unit test for {@link CodeSnipitMerger}
 *
 * @author Daan
 */
public class CodeSnipitMergerTest {

  public CodeSnipitMerger merger = new CodeSnipitMerger() {
    @Override
    protected void executeMerge(JavaForgerConfiguration config, CodeSnipit codeSnipit, String mergeClassPath) throws IOException {
      // empty abstract method
    }
  };

  @Test
  public void testToCompleteClass() {
    String code = "import my.impord;";
    String expected = code + "\n\npublic class MyClass {\n\n}";

    String claz = merger.toCompleteClass(new CodeSnipit(code), "The/Path\\To/MyClass.java");

    Assert.assertEquals(claz, expected, claz);

  }

}
