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
package templateInput.definition;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import templateInput.StringConverter;

/**
 * Unit test for {@link TypeDefinition}.
 *
 * @author Daan
 */
@RunWith(MockitoJUnitRunner.class)
public class TypeDefinitionTest {
  private static final StringConverter NAME = StringConverter.builder().build();
  private static final StringConverter TYPE = StringConverter.builder().build();
  private static final List<StringConverter> GENERICS = Collections.singletonList(StringConverter.builder().build());
  private static final LinkedHashSet<String> TYPE_IMPORTS = new LinkedHashSet<>();
  private static final int LINE_NUMBER = 1;
  private static final int COLUMN = 3;
  private static final Set<String> ANNOTATIONS = Collections.singleton("a");
  private static final Set<String> ACCESS_MODIFIERS = Collections.singleton("c");
  private static final String PACK = "e";

  @Test
  public void testTypeDefinition_minimum() {
    TypeDefinition typeDefinition = MethodDefinition.builder().build();

    Assert.assertNull("Unexpected name", typeDefinition.getName());
    Assert.assertEquals("Unexpected type", new StringConverter(null), typeDefinition.getType());
    Assert.assertTrue("Unexpected generics", typeDefinition.getGenerics().isEmpty());
    Assert.assertNull("Unexpected typeImports", typeDefinition.getTypeImports());
    Assert.assertEquals("Unexpected lineNumber", 0, typeDefinition.getLineNumber());
    Assert.assertEquals("Unexpected column", 0, typeDefinition.getColumn());
    Assert.assertTrue("Unexpected annotations", typeDefinition.getAnnotations().isEmpty());
    Assert.assertTrue("Unexpected accessModifiers", typeDefinition.getAccessModifiers().isEmpty());
    Assert.assertNull("Unexpected pack", typeDefinition.getPack());
  }

  @Test
  public void testTypeDefinition_maximum() {
    TypeDefinition typeDefinition = createAndFillBuilder().build();

    Assert.assertEquals("Unexpected name", NAME, typeDefinition.getName());
    Assert.assertEquals("Unexpected type", TYPE, typeDefinition.getType());
    Assert.assertEquals("Unexpected generics", GENERICS, typeDefinition.getGenerics());
    Assert.assertEquals("Unexpected typeImports", TYPE_IMPORTS, typeDefinition.getTypeImports());
    Assert.assertEquals("Unexpected lineNumber", LINE_NUMBER, typeDefinition.getLineNumber());
    Assert.assertEquals("Unexpected column", COLUMN, typeDefinition.getColumn());
    Assert.assertEquals("Unexpected annotations", ANNOTATIONS, typeDefinition.getAnnotations());
    Assert.assertEquals("Unexpected accessModifiers", ACCESS_MODIFIERS, typeDefinition.getAccessModifiers());
    Assert.assertEquals("Unexpected pack", PACK, typeDefinition.getPack());
  }

  private TypeDefinition.Builder<?> createAndFillBuilder() {
    return MethodDefinition.builder().name(NAME).type(TYPE).generics(GENERICS).typeImports(TYPE_IMPORTS).lineNumber(LINE_NUMBER).column(COLUMN)
        .annotations(ANNOTATIONS).accessModifiers(ACCESS_MODIFIERS).pack(PACK);
  }

}
