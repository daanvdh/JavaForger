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

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

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

    LinkedHashMap<CodeSnipitLocation, CodeSnipitLocation> expected = new LinkedHashMap<>();
    expected.put(CodeSnipitLocation.of(1, 2), CodeSnipitLocation.of(2, 2));

    executeAndVerify(code, code, expected);
  }

  @Test
  public void testLocate_innerClass() {
    String code1 = "public class ClassToMerge {\n\npublic class InnerClass1 {\n\n}\n}";
    String code2 = "public class ClassToMerge {\n\nclass InnerClass2 {\n\n}\n}";

    LinkedHashMap<CodeSnipitLocation, CodeSnipitLocation> expected = new LinkedHashMap<>();
    expected.put(CodeSnipitLocation.of(3, 6), CodeSnipitLocation.of(6, 6));

    executeAndVerify(code1, code2, expected);
  }

  @Test
  public void testLocate_ordering() {
    String code1 = "public class ClassToMerge {\n\n" //
        + "public int a; \n" //
        + "private int b; \n" //
        + "\n}";
    String code2 = "public class ClassToMerge {\n\n" //
        + "private int c; \n" //
        + "public int a = 5; \n" //
        + "\n}";

    LinkedHashMap<CodeSnipitLocation, CodeSnipitLocation> expected = new LinkedHashMap<>();
    expected.put(CodeSnipitLocation.of(4, 5), CodeSnipitLocation.of(3, 4));
    expected.put(CodeSnipitLocation.of(3, 4), CodeSnipitLocation.of(5, 5));

    executeAndVerify(code1, code2, expected);
  }

  private void executeAndVerify(String existing, String insert, LinkedHashMap<CodeSnipitLocation, CodeSnipitLocation> expected) {
    CompilationUnit cu1 = parser.parse(existing);
    CompilationUnit cu2 = parser.parse(insert);

    LinkedHashMap<CodeSnipitLocation, CodeSnipitLocation> locations = locater.locate(cu1, cu2);

    Assert.assertEquals("Expected the same size", expected.size(), locations.size());

    Iterator<Entry<CodeSnipitLocation, CodeSnipitLocation>> res = locations.entrySet().iterator();
    Iterator<Entry<CodeSnipitLocation, CodeSnipitLocation>> exp = expected.entrySet().iterator();
    int i = 0;
    while (res.hasNext()) {
      Assert.assertEquals("Expected the locations on index " + i++ + " to be equal", exp.next(), res.next());
    }
  }

}
