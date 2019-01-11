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
import java.util.Set;
import java.util.stream.Collectors;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;

import templateInput.VariableDefinition;

/**
 * Reader for .java files, for getting the fields. If the {@link JavaSymbolSolver} is setup properly by calling
 * {@link JavaParser#getStaticConfiguration()}::setSymbolSolver before using this class, it will use the symbolSolver to find out the imports of the fields.
 *
 * @author Daan
 */
public class FieldReader {

  public List<VariableDefinition> getFields(String className) throws IOException {
    ArrayList<VariableDefinition> fields = new ArrayList<>();
    try (FileInputStream in = new FileInputStream(className)) {
      CompilationUnit cu = JavaParser.parse(in);
      in.close();
      getFields(fields, cu);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    return fields.stream().sorted().collect(Collectors.toList());
  }

  private void getFields(ArrayList<VariableDefinition> fields, CompilationUnit cu) {
    for (TypeDeclaration<?> type : cu.getTypes()) {
      List<Node> childNodes = type.getChildNodes();
      for (Node node : childNodes) {
        if (node instanceof FieldDeclaration) {
          FieldDeclaration fd = (FieldDeclaration) node;
          Set<String> annotations = fd.getAnnotations().stream().map(annotation -> annotation.getName().toString()).collect(Collectors.toSet());
          Set<String> accessModifiers = fd.getModifiers().stream().map(modifier -> modifier.asString()).collect(Collectors.toSet());
          VariableDefinition variable = VariableDefinition.builder().withName(fd.getVariable(0).getName().asString()).withType(fd.getElementType().asString())
              .withAnnotations(annotations).withLineNumber(fd.getBegin().map(p -> p.line).orElse(-1)).withColumn(fd.getBegin().map(p -> p.column).orElse(-1))
              .withAccessModifiers(accessModifiers).build();
          fields.add(variable);

          resolveAndSetImport(fd, variable);
        }
      }
    }
  }

  private void resolveAndSetImport(FieldDeclaration fd, VariableDefinition variable) {
    try {
      ResolvedType resolve = fd.getElementType().resolve();
      String imp = resolve.describe();
      if (!imp.startsWith("java.lang.") && !resolve.isPrimitive()) {
        variable.setTypeImport(imp);
      }
    } catch (@SuppressWarnings("unused") Exception e) {
      System.err.println("FieldReader: Could not resolve import for " + fd.getElementType().asString());
    }
  }

}
