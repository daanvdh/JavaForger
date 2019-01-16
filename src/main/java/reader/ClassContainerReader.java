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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.CallableDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.github.javaparser.resolution.types.ResolvedType;

import configuration.JavaForgerConfiguration;
import generator.JavaForgerException;
import initialization.VariableInitializer;
import templateInput.ClassContainer;
import templateInput.definition.ClassDefinition;
import templateInput.definition.MethodDefinition;
import templateInput.definition.VariableDefinition;

/**
 * Reader for all data within a class.
 *
 * @author Daan
 */
public class ClassContainerReader {

  private VariableInitializer initializer = new VariableInitializer();

  public ClassContainer read(String inputClass) throws IOException {
    return read(inputClass, JavaForgerConfiguration.builder().build());
  }

  public ClassContainer read(String inputClass, JavaForgerConfiguration config) throws IOException {
    setupSymbolSolver(config);
    CompilationUnit cu = getCompilationUnit(inputClass);
    return readCompilationUnit(cu, config);
  }

  private CompilationUnit getCompilationUnit(String inputClass) throws IOException {
    CompilationUnit cu = null;
    try (FileInputStream in = new FileInputStream(inputClass)) {
      cu = JavaParser.parse(in);
      in.close();
    } catch (FileNotFoundException e) {
      throw new JavaForgerException(e, "Could not parse " + inputClass);
    }
    return cu;
  }

  private ClassContainer readCompilationUnit(CompilationUnit cu, JavaForgerConfiguration config) {
    ClassContainer claz = new ClassContainer();
    List<VariableDefinition> fields = new ArrayList<>();
    List<MethodDefinition> methods = new ArrayList<>();
    List<MethodDefinition> constructors = new ArrayList<>();

    for (TypeDeclaration<?> type : cu.getTypes()) {
      if (type instanceof ClassOrInterfaceDeclaration) {
        claz = parseClass(config, type);
      }

      List<Node> childNodes = type.getChildNodes();
      for (Node node : childNodes) {
        if (node instanceof FieldDeclaration) {
          fields.add(parseField(config, node));
        } else if (node instanceof MethodDeclaration) {
          methods.add(parseMethod(config, node));
        } else if (node instanceof ConstructorDeclaration) {
          constructors.add(parseConstructor(config, node));
        }
      }
    }

    Optional<String> typeImport = cu.getPackageDeclaration().map(pd -> pd.getNameAsString());
    if (typeImport.isPresent()) {
      claz.addTypeImport(typeImport.get() + "." + claz.getName());
    }

    initializer.init(fields);
    claz.setFields(fields);
    claz.setMethods(methods);
    claz.setConstructors(constructors);
    return claz;
  }

  private ClassContainer parseClass(JavaForgerConfiguration config, TypeDeclaration<?> type) {
    ClassOrInterfaceDeclaration cd = (ClassOrInterfaceDeclaration) type;
    Set<String> annotations = cd.getAnnotations().stream().map(annotation -> annotation.getName().toString()).collect(Collectors.toSet());
    Set<String> accessModifiers = cd.getModifiers().stream().map(modifier -> modifier.asString()).collect(Collectors.toSet());
    List<String> interfaces = cd.getImplementedTypes().stream().map(i -> i.getNameAsString()).collect(Collectors.toList());
    String extend = cd.getExtendedTypes().stream().findFirst().map(e -> e.getNameAsString()).orElse(null);

    ClassDefinition def = ClassDefinition.builder().withName(cd.getNameAsString()).withType(cd.getNameAsString())
        .withLineNumber(cd.getBegin().map(p -> p.line).orElse(-1)).withColumn(cd.getBegin().map(p -> p.column).orElse(-1)).withAnnotations(annotations)
        .withAccessModifiers(accessModifiers).withExtend(extend).withInterfaces(interfaces).build();
    return new ClassContainer(def);
  }

  private MethodDefinition parseMethod(JavaForgerConfiguration config, Node node) {
    MethodDeclaration md = (MethodDeclaration) node;
    MethodDefinition method = parseCallable(md);
    method.setType(md.getTypeAsString());
    return method;
  }

  private MethodDefinition parseConstructor(JavaForgerConfiguration config, Node node) {
    ConstructorDeclaration md = (ConstructorDeclaration) node;
    MethodDefinition method = parseCallable(md);
    method.setType(md.getNameAsString());
    return method;
  }

  private MethodDefinition parseCallable(CallableDeclaration<?> md) {
    Set<String> accessModifiers = md.getModifiers().stream().map(Modifier::asString).collect(Collectors.toSet());
    Set<String> annotations = md.getAnnotations().stream().map(AnnotationExpr::getNameAsString).collect(Collectors.toSet());

    List<VariableDefinition> parameters = md.getParameters().stream()
        .map(par -> VariableDefinition.builder().withName(par.getNameAsString()).withType(par.getTypeAsString()).build()).collect(Collectors.toList());

    MethodDefinition method = MethodDefinition.builder().withName(md.getNameAsString()).withAccessModifiers(accessModifiers).withAnnotations(annotations)
        .withLineNumber(md.getBegin().map(p -> p.line).orElse(-1)).withColumn(md.getBegin().map(p -> p.column).orElse(-1)).withParameters(parameters).build();
    return method;
  }

  private VariableDefinition parseField(JavaForgerConfiguration config, Node node) {
    FieldDeclaration fd = (FieldDeclaration) node;
    Set<String> annotations = fd.getAnnotations().stream().map(annotation -> annotation.getName().toString()).collect(Collectors.toSet());
    Set<String> accessModifiers = fd.getModifiers().stream().map(modifier -> modifier.asString()).collect(Collectors.toSet());
    VariableDefinition variable = VariableDefinition.builder().withName(fd.getVariable(0).getName().asString()).withType(fd.getElementType().asString())
        .withAnnotations(annotations).withLineNumber(fd.getBegin().map(p -> p.line).orElse(-1)).withColumn(fd.getBegin().map(p -> p.column).orElse(-1))
        .withAccessModifiers(accessModifiers).build();

    resolveAndSetImport(fd, variable, config);
    return variable;
  }

  private void resolveAndSetImport(FieldDeclaration fd, VariableDefinition variable, JavaForgerConfiguration config) {
    if (config.getSymbolSolver() != null) {
      try {
        ResolvedType resolve = fd.getElementType().resolve();
        List<String> imports = getImports(resolve);
        if (!imports.isEmpty()) {
          variable.addTypeImports(imports);
        }
      } catch (@SuppressWarnings("unused") Exception e) {
        System.err.println("FieldReader: Could not resolve import for " + fd.getElementType().asString());
      }
    }
  }

  private List<String> getImports(ResolvedType resolve) {
    List<String> imports = new ArrayList<>();
    String imp;
    if (resolve.isReferenceType()) {
      ResolvedReferenceType refType = resolve.asReferenceType();
      ResolvedReferenceTypeDeclaration type = refType.getTypeDeclaration();
      imp = type.getQualifiedName();
      List<ResolvedType> innerResolvedTypes =
          type.getTypeParameters().stream().map(tp -> refType.typeParametersMap().getValue(tp)).collect(Collectors.toList());
      // This is a recursive call to resolve all imports of parameterized types
      List<String> collect = innerResolvedTypes.stream().flatMap(t -> getImports(t).stream()).collect(Collectors.toList());
      imports.addAll(collect);
    } else {
      imp = resolve.describe();
    }
    if (!imp.startsWith("java.lang.") && !resolve.isPrimitive()) {
      imports.add(imp);
    }
    return imports;
  }

  private void setupSymbolSolver(JavaForgerConfiguration config) {
    JavaParser.getStaticConfiguration().setSymbolResolver(config.getSymbolSolver());
  }

}
