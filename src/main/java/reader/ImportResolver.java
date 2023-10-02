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

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.github.javaparser.resolution.types.ResolvedType;

import configuration.StaticJavaForgerConfiguration;
import templateInput.definition.TypeDefinition;

/**
 * Class for resolving the imports for given {@link JavaParser} Types.
 *
 * @author Daan
 */
public class ImportResolver {
  private static final Logger LOG = LoggerFactory.getLogger(ImportResolver.class);

  private StaticJavaForgerConfiguration staticConfig = StaticJavaForgerConfiguration.getConfig();

  /**
   * @param type
   * @param variable
   * @deprecated Use resolveImports instead.
   */
  @Deprecated
  public void resolveAndSetImport(Type type, TypeDefinition variable) {
    resolveImport(type).forEach(variable::addTypeImport);
  }

  /**
   * Resolve the imports for the given type.
   *
   * @param type The {@link JavaParser} {@link Type}.
   * @return A {@link List} of {@link String} representing the imports.
   */
  public LinkedHashSet<String> resolveImport(Type type) {
    List<String> imports = resolve(type);
    return !imports.isEmpty() ? imports.stream().filter(s -> !s.contains("?")).collect(Collectors.toCollection(LinkedHashSet::new)) : new LinkedHashSet<>();
  }

  /**
   * @param type The {@link Type} to get the package from.
   * @return The resolved package.
   */
  public String resolvePackage(Type type) {
    ResolvedType resolve = type.resolve();
    String typeImport = getTypeImport(resolve);
    int lastIndex = typeImport.lastIndexOf(".") < 0 ? typeImport.length() : typeImport.lastIndexOf(".");
    return typeImport.substring(0, lastIndex);
  }

  private List<String> resolve(Type type) {
    List<String> imports = new ArrayList<>();
    if (staticConfig.getSymbolSolver() != null) {
      try {
        ResolvedType resolve = type.resolve();
        imports.addAll(getImportsFromResolvedType(resolve));
      } catch (Exception e) {
        LOG.error("Could not resolve import for {}, check if symbol solver is correctly setup using StaticJavaForgerConfiguration::setProjectPaths. "
            + "Received exception with message: {}", type.asString(), e.getMessage());
      }
    }
    return imports;
  }

  private List<String> getImportsFromResolvedType(ResolvedType resolve) {
    List<String> imports = new ArrayList<>();
    String imp = getTypeImport(resolve);
    if (!imp.startsWith("java.lang.") && !resolve.isPrimitive()) {
      imports.add(imp);
    }
    if (resolve.isReferenceType()) {
      // This is a recursive call to resolve all imports of parameterized types
      ResolvedReferenceType refType = resolve.asReferenceType();
      ResolvedReferenceTypeDeclaration type = refType.getTypeDeclaration();
      List<ResolvedType> innerResolvedTypes =
          type.getTypeParameters().stream().map(tp -> refType.typeParametersMap().getValue(tp)).collect(Collectors.toList());
      List<String> collect = innerResolvedTypes.stream().flatMap(t -> getImportsFromResolvedType(t).stream()).collect(Collectors.toList());
      imports.addAll(collect);
    }
    return imports;
  }

  protected String getTypeImport(ResolvedType resolve) {
    String imp;
    if (resolve.isReferenceType()) {
      ResolvedReferenceType refType = resolve.asReferenceType();
      ResolvedReferenceTypeDeclaration type = refType.getTypeDeclaration();
      imp = type.getQualifiedName();
    } else {
      imp = resolve.describe();
    }
    return imp;
  }
}
