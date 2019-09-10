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
import java.util.LinkedHashMap;
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
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.github.javaparser.resolution.types.ResolvedType;

import configuration.StaticJavaForgerConfiguration;
import dataflow.DataFlowGraph;
import dataflow.DataFlowGraphFactory;
import dataflow.DataFlowMethod;
import dataflow.DataFlowNode;
import dataflow.GraphService;
import generator.JavaForgerException;
import templateInput.ClassContainer;
import templateInput.definition.ClassDefinition;
import templateInput.definition.FlowReceiverDefnition;
import templateInput.definition.MethodDefinition;
import templateInput.definition.TypeDefinition;
import templateInput.definition.VariableDefinition;

/**
 * Reader for all data within a class.
 *
 * @author Daan
 */
public class ClassContainerReader {

  private StaticJavaForgerConfiguration staticConfig = StaticJavaForgerConfiguration.getConfig();
  private DataFlowGraphFactory dfgFactory = new DataFlowGraphFactory();
  private GraphService graphService = new GraphService();

  public ClassContainer read(String inputClass) throws IOException {
    CompilationUnit cu = getCompilationUnit(inputClass);
    DataFlowGraph dfg = dfgFactory.createGraph(cu);
    ClassContainer claz = readCompilationUnit(cu, dfg);
    return claz;
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

  private ClassContainer readCompilationUnit(CompilationUnit cu, DataFlowGraph dfg) {
    ClassContainer claz = new ClassContainer();
    List<VariableDefinition> fields = new ArrayList<>();
    List<MethodDefinition> methods = new ArrayList<>();
    List<MethodDefinition> constructors = new ArrayList<>();

    for (TypeDeclaration<?> type : cu.getTypes()) {
      if (type instanceof ClassOrInterfaceDeclaration) {
        claz = parseClass(type);
      }

      List<Node> childNodes = type.getChildNodes();
      for (Node node : childNodes) {
        if (node instanceof FieldDeclaration) {
          fields.add(parseField(node));
        } else if (node instanceof MethodDeclaration) {
          MethodDefinition newMethod = parseMethod(node);
          methods.add(newMethod);

          // TODO start
          DataFlowMethod method = dfg.getMethod(node);
          List<DataFlowNode> changedFieldsNodes = method.getChangedFields();
          List<FlowReceiverDefnition> changedFields = new ArrayList<>();
          for (DataFlowNode dfn : changedFieldsNodes) {
            DataFlowNode firstValue = graphService.walkBackUntil(dfn, method);
            FlowReceiverDefnition receiver = new FlowReceiverDefnition();
            receiver.setReceivedValue(firstValue.toString());
            changedFields.add(receiver);
          }
          newMethod.setChangedFields(changedFields);
          // TODO end

        } else if (node instanceof ConstructorDeclaration) {
          constructors.add(parseConstructor(node));
        }
      }
    }

    Optional<String> typeImport = cu.getPackageDeclaration().map(pd -> pd.getNameAsString());
    if (typeImport.isPresent()) {
      claz.addTypeImport(typeImport.get() + "." + claz.getName());
      constructors.forEach(c -> c.addTypeImport(typeImport.get()));
    }

    claz.setFields(fields);
    claz.setMethods(methods);
    claz.setConstructors(constructors);
    return claz;
  }

  private ClassContainer parseClass(TypeDeclaration<?> type) {
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

  private MethodDefinition parseMethod(Node node) {
    MethodDeclaration md = (MethodDeclaration) node;
    MethodDefinition method = parseCallable(md);
    method.setType(md.getTypeAsString());
    resolveAndSetImport(md.getType(), method);
    return method;
  }

  private MethodDefinition parseConstructor(Node node) {
    ConstructorDeclaration md = (ConstructorDeclaration) node;
    MethodDefinition method = parseCallable(md);
    method.setType(md.getNameAsString());
    return method;
  }

  private MethodDefinition parseCallable(CallableDeclaration<?> md) {
    Set<String> accessModifiers = md.getModifiers().stream().map(Modifier::asString).collect(Collectors.toSet());
    Set<String> annotations = md.getAnnotations().stream().map(AnnotationExpr::getNameAsString).collect(Collectors.toSet());

    return MethodDefinition.builder().withName(md.getNameAsString()).withAccessModifiers(accessModifiers).withAnnotations(annotations)
        .withLineNumber(md.getBegin().map(p -> p.line).orElse(-1)).withColumn(md.getBegin().map(p -> p.column).orElse(-1)).withParameters(getParameters(md))
        .build();
  }

  private VariableDefinition parseField(Node node) {
    FieldDeclaration fd = (FieldDeclaration) node;
    Set<String> annotations = fd.getAnnotations().stream().map(annotation -> annotation.getName().toString()).collect(Collectors.toSet());
    Set<String> accessModifiers = fd.getModifiers().stream().map(modifier -> modifier.asString()).collect(Collectors.toSet());
    Optional<String> originalInit = depthFirstSearch(fd, Expression.class);
    VariableDefinition variable = VariableDefinition.builder().withName(fd.getVariable(0).getName().asString()).withType(fd.getElementType().asString())
        .withAnnotations(annotations).withLineNumber(fd.getBegin().map(p -> p.line).orElse(-1)).withColumn(fd.getBegin().map(p -> p.column).orElse(-1))
        .withAccessModifiers(accessModifiers).originalInit(originalInit.orElse(null)).build();

    resolveAndSetImport(fd.getElementType(), variable);
    return variable;
  }

  private Optional<String> depthFirstSearch(Node node, Class<Expression> claz) {
    if (claz.isAssignableFrom(node.getClass())) {
      return Optional.of(node.toString());
    }
    return node.getChildNodes().stream().map(n -> depthFirstSearch(n, claz)).filter(Optional::isPresent).map(Optional::get).map(Object::toString).findFirst();
  }

  private List<VariableDefinition> getParameters(CallableDeclaration<?> md) {
    LinkedHashMap<Parameter, VariableDefinition> params = new LinkedHashMap<>();
    md.getParameters().stream().forEach(p -> params.put(p, VariableDefinition.builder().withName(p.getNameAsString()).withType(p.getTypeAsString()).build()));
    params.entrySet().forEach(p -> resolveAndSetImport(p.getKey().getType(), p.getValue()));
    List<VariableDefinition> parameters = params.values().stream().collect(Collectors.toList());
    return parameters;
  }

  private void resolveAndSetImport(Type type, TypeDefinition variable) {
    List<String> imports = resolve(type);
    if (!imports.isEmpty()) {
      imports.stream().filter(s -> !s.contains("?")).forEach(s -> variable.addTypeImport(s));
    }
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
