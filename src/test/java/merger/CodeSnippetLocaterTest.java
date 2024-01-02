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

import configuration.JavaForgerConfiguration;
import reader.Parser;

/**
 * Unit test for {@link CodeSnippetLocater}
 *
 * @author Daan
 */
public class CodeSnippetLocaterTest {

  private Parser parser = new Parser();

  private CodeSnippetLocater locater = new CodeSnippetLocater();

  @Test
  public void testLocate() {
    String code = "import my.impord;\n\npublic class ClassToMerge {\n\n}";

    LinkedHashMap<CodeSnippetLocation, CodeSnippetLocation> expected = new LinkedHashMap<>();
    expected.put(CodeSnippetLocation.of(1, 0, 2, 0), CodeSnippetLocation.of(2, 0, 2, 0));

    executeAndVerify(code, code, expected);
  }

  @Test
  public void testLocate_innerClass() {
    String code1 = "public class ClassToMerge {\n\npublic class InnerClass1 {\n\n}\n}";
    String code2 = "public class ClassToMerge {\n\nclass InnerClass2 {\n\n}\n}";

    LinkedHashMap<CodeSnippetLocation, CodeSnippetLocation> expected = new LinkedHashMap<>();
    expected.put(CodeSnippetLocation.of(3, 0, 6, 0), CodeSnippetLocation.of(6, 0, 6, 0));

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

    LinkedHashMap<CodeSnippetLocation, CodeSnippetLocation> expected = new LinkedHashMap<>();
    expected.put(CodeSnippetLocation.of(4, 0, 5, 0), CodeSnippetLocation.of(3, 0, 4, 0));
    expected.put(CodeSnippetLocation.of(3, 0, 4, 0), CodeSnippetLocation.of(5, 0, 5, 0));

    executeAndVerify(code1, code2, expected);
  }

  private void executeAndVerify(String existing, String insert, LinkedHashMap<CodeSnippetLocation, CodeSnippetLocation> expected) {
    CompilationUnit cu1 = parser.parse(existing);
    CompilationUnit cu2 = parser.parse(insert);

    InsertionMap locations = locater.locate(cu1, cu2, JavaForgerConfiguration.builder().build());

    Assert.assertEquals("Expected the same size", expected.size(), locations.size());

    Iterator<InsertionEntry> res = locations.iterator();
    Iterator<Entry<CodeSnippetLocation, CodeSnippetLocation>> exp = expected.entrySet().iterator();
    int i = 0;
    while (res.hasNext()) {
      Assert.assertEquals("Expected the locations on index " + i++ + " to be equal", exp.next(), res.next());
    }
  }

}
