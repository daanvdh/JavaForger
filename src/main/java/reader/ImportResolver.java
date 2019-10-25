/*
 * Copyright (c) 2019 by Eyefreight BV (www.eyefreight.com). All rights reserved.
 *
 * This software is provided by the copyright holder and contributors "as is" and any express or implied warranties, including, but
 * not limited to, the implied warranties of merchantability and fitness for a particular purpose are disclaimed. In no event shall
 * Eyefreight BV or contributors be liable for any direct, indirect, incidental, special, exemplary, or consequential damages
 * (including, but not limited to, procurement of substitute goods or services; * loss of use, data, or profits; or business
 * interruption) however caused and on any theory of liability, whether in contract, strict liability, or tort (including
 * negligence or otherwise) arising in any way out of the use of this software, even if advised of the possibility of such damage.
 */
package reader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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
  public List<String> resolveImport(Type type) {
    List<String> imports = resolve(type);
    return !imports.isEmpty() ? imports.stream().filter(s -> !s.contains("?")).collect(Collectors.toList()) : Collections.emptyList();
  }

  private List<String> resolve(Type type) {
    List<String> imports = new ArrayList<>();
    if (staticConfig.getSymbolSolver() != null) {
      try {
        ResolvedType resolve = type.resolve();
        imports.addAll(getImportsFromResolvedType(resolve));
      } catch (@SuppressWarnings("unused") Exception e) {
        System.err.println("FieldReader: Could not resolve import for " + type.asString());
      }
    }
    return imports;
  }

  private List<String> getImportsFromResolvedType(ResolvedType resolve) {
    List<String> imports = new ArrayList<>();
    String imp;
    if (resolve.isReferenceType()) {
      ResolvedReferenceType refType = resolve.asReferenceType();
      ResolvedReferenceTypeDeclaration type = refType.getTypeDeclaration();
      imp = type.getQualifiedName();
      List<ResolvedType> innerResolvedTypes =
          type.getTypeParameters().stream().map(tp -> refType.typeParametersMap().getValue(tp)).collect(Collectors.toList());
      // This is a recursive call to resolve all imports of parameterized types
      List<String> collect = innerResolvedTypes.stream().flatMap(t -> getImportsFromResolvedType(t).stream()).collect(Collectors.toList());
      imports.addAll(collect);
    } else {
      imp = resolve.describe();
    }
    if (!imp.startsWith("java.lang.") && !resolve.isPrimitive()) {
      imports.add(imp);
    }
    return imports;
  }
}
