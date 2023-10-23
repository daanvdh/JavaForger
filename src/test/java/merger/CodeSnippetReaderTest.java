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

import org.junit.Assert;
import org.junit.Test;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;

import generator.CodeSnippet;

/**
 * Unit test for {@link CodeSnippetReader}
 *
 * @author Daan
 */
public class CodeSnippetReaderTest {

  private CodeSnippetReader sut = new CodeSnippetReader();

  @Test
  public void testRead_withClass() {
    String code = "class Claz { \n\n" + "protected String prettyString = \"very pretty\";\n" + "\n}\n";

    CompilationUnit result = sut.read(new CodeSnippet(code), "/path/to/Claz.java");

    Assert.assertEquals(1, result.getChildNodes().size());
    Assert.assertTrue(result.getChildNodes().get(0) instanceof ClassOrInterfaceDeclaration);
    ClassOrInterfaceDeclaration claz = (ClassOrInterfaceDeclaration) result.getChildNodes().get(0);
    Assert.assertEquals("Claz", claz.getNameAsString());
    Assert.assertEquals(2, claz.getChildNodes().size());
    Assert.assertTrue(claz.getChildNodes().get(1) instanceof FieldDeclaration);
  }

  @Test
  public void testRead_WithoutClass() {
    String code = "protected String prettyString = \"very pretty\";";

    CompilationUnit result = sut.read(new CodeSnippet(code), "/path/to/Claz.java");

    Assert.assertEquals(1, result.getChildNodes().size());
    Assert.assertTrue(result.getChildNodes().get(0) instanceof ClassOrInterfaceDeclaration);
    ClassOrInterfaceDeclaration claz = (ClassOrInterfaceDeclaration) result.getChildNodes().get(0);
    Assert.assertEquals("Claz", claz.getNameAsString());
    Assert.assertEquals(3, claz.getChildNodes().size());
    Assert.assertTrue(claz.getChildNodes().get(2) instanceof FieldDeclaration);
  }

  @Test
  public void testToCompleteClass_onlyImport() {
    String code = "import my.impord;";

    String claz = sut.toCompleteClass(new CodeSnippet(code), "The/Path\\To/MyClass.java");

    Assert.assertEquals(claz, code, claz);
  }

}
