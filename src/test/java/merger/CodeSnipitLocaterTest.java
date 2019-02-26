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

import java.util.LinkedHashMap;

import org.junit.Assert;
import org.junit.Test;

import com.github.javaparser.ast.CompilationUnit;

import reader.Parser;

/**
 * Unit test for {@link CodeSnipitLocater}
 *
 * @author Daan
 */
public class CodeSnipitLocaterTest {

  private Parser parser = new Parser();

  private CodeSnipitLocater locater = new CodeSnipitLocater();

  @Test
  public void testLocate() {
    String code = "import my.impord;\n\npublic class ClassToMerge {\n\n}";
    CompilationUnit cu = parser.parse(code);

    LinkedHashMap<CodeSnipitLocation, CodeSnipitLocation> expected = new LinkedHashMap<>();
    expected.put(CodeSnipitLocation.of(1, 2), CodeSnipitLocation.of(2, 2));

    LinkedHashMap<CodeSnipitLocation, CodeSnipitLocation> locations = locater.locate(cu, cu);

    Assert.assertEquals(expected, locations);
  }

  @Test
  public void testLocate_innerClass() {
    String code1 = "public class ClassToMerge {\n\npublic class InnerClass1 {\n\n}\n}";
    String code2 = "public class ClassToMerge {\n\nclass InnerClass2 {\n\n}\n}";
    CompilationUnit cu1 = parser.parse(code1);
    CompilationUnit cu2 = parser.parse(code2);

    LinkedHashMap<CodeSnipitLocation, CodeSnipitLocation> expected = new LinkedHashMap<>();
    expected.put(CodeSnipitLocation.of(3, 6), CodeSnipitLocation.of(6, 6));

    LinkedHashMap<CodeSnipitLocation, CodeSnipitLocation> locations = locater.locate(cu1, cu2);

    Assert.assertEquals(expected, locations);
  }

}
