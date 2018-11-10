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

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;

import templateInput.MethodDefinition;

/**
 * Reads the methods from a given {@link CompilationUnit} created by {@link JavaParser}.
 *
 * @author Daan
 */
public class MethodReader extends Reader<MethodDefinition> {

  @Override
  public void visit(MethodDeclaration md, List<MethodDefinition> gatheredMethods) {
    super.visit(md, gatheredMethods);
    Set<String> accessModifiers = md.getModifiers().stream().map(Modifier::asString).collect(Collectors.toSet());
    Set<String> annotations = md.getAnnotations().stream().map(AnnotationExpr::getNameAsString).collect(Collectors.toSet());
    MethodDefinition method = MethodDefinition.builder().withName(md.getNameAsString()).withType(md.getTypeAsString()).withAccessModifiers(accessModifiers)
        .withAnnotations(annotations).withLineNumber(md.getBegin().map(p -> p.line).orElse(-1)).withColumn(md.getBegin().map(p -> p.column).orElse(-1)).build();
    gatheredMethods.add(method);
  }

}
