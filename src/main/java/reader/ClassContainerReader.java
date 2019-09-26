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
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;

import dataflow.DataFlowException;
import dataflow.DataFlowGraph;
import dataflow.DataFlowGraphFactory;
import generator.JavaForgerException;
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

  private DataFlowGraphFactory dfgFactory = new DataFlowGraphFactory();
  private MethodFactory methodFactory = new MethodFactory();
  private VariableFactory fieldFactory = new VariableFactory();

  public ClassContainer read(String inputClass) throws IOException {
    CompilationUnit cu = getCompilationUnit(inputClass);
    DataFlowGraph dfg = null;
    dfg = dfgFactory.create(cu);
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
          fields.addAll(fieldFactory.create(node));
        } else if (node instanceof MethodDeclaration) {
          MethodDefinition newMethod = methodFactory.createMethod(node, dfg);
          methods.add(newMethod);
        } else if (node instanceof ConstructorDeclaration) {
          MethodDefinition constructor = methodFactory.createConstructor(node);
          constructors.add(constructor);
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

    ClassDefinition def = ClassDefinition.builder().name(cd.getNameAsString()).type(cd.getNameAsString())
        .lineNumber(cd.getBegin().map(p -> p.line).orElse(-1)).column(cd.getBegin().map(p -> p.column).orElse(-1)).annotations(annotations)
        .accessModifiers(accessModifiers).extend(extend).interfaces(interfaces).build();
    return new ClassContainer(def);
  }

}
