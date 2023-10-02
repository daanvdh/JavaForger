/*
 * Copyright 2023 by Daan van den Heuvel.
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

import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.nodeTypes.NodeWithVariables;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.Type;

import templateInput.definition.TypeDefinition;

/**
 * Abstract class for shared logic around creating {@link TypeDefinition}s.
 *
 * @author daan.vandenheuvel
 */
public class TypeDefinitionFactory {

  private ImportResolver importResolver = new ImportResolver();

  protected void addType(Node n, TypeDefinition.Builder<?> fieldBuilder) {
    if (NodeWithVariables.class.isAssignableFrom(n.getClass())) {
      NodeWithVariables<?> cast = (NodeWithVariables<?>) n;
      LinkedHashSet<String> imports = importResolver.resolveImport(cast.getElementType());
      fieldBuilder.typeImports(imports);
      Type elementType = cast.getElementType();
      fieldBuilder.type(getType(elementType));
      List<String> generics = elementType.findAll(ClassOrInterfaceType.class).stream().filter(t -> !elementType.equals(t)).map(ClassOrInterfaceType::asString)
          .collect(Collectors.toList());
      fieldBuilder.genericsFromString(generics);
      fieldBuilder.pack(importResolver.resolvePackage(cast.getElementType()));
    }
  }

  protected String getType(Type elementType) {
    List<SimpleName> foundNames = elementType.findAll(SimpleName.class);
    String type = foundNames.isEmpty() ? null : foundNames.get(0).asString();
    if (type == null) {
      List<PrimitiveType> foundPrimitives = elementType.findAll(PrimitiveType.class);
      type = foundPrimitives.isEmpty() ? null : foundPrimitives.get(0).asString();
    }
    return type;
  }

}
