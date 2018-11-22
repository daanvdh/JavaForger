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

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;

import templateInput.ClassDefinition;

/**
 * Reader to fill {@link ClassDefinition} from a java input file using {@link JavaParser}.
 *
 * @author Daan
 */
public class ClassReader {

  public ClassDefinition read(String inputClass) throws IOException {
    InnerClassReader reader = new InnerClassReader();
    List<ClassDefinition> classes = reader.read(inputClass);

    // TODO create better error than index out of bound when class not found.

    return classes.get(0);
  }

  private class InnerClassReader extends Reader<ClassDefinition> {

    @Override
    public void visit(ClassOrInterfaceDeclaration cd, List<ClassDefinition> classes) {
      super.visit(cd, classes);
      Set<String> annotations = cd.getAnnotations().stream().map(annotation -> annotation.getName().toString()).collect(Collectors.toSet());
      Set<String> accessModifiers = cd.getModifiers().stream().map(modifier -> modifier.asString()).collect(Collectors.toSet());
      List<String> interfaces = cd.getImplementedTypes().stream().map(i -> i.getNameAsString()).collect(Collectors.toList());
      String extend = cd.getExtendedTypes().stream().findFirst().map(e -> e.getNameAsString()).orElse(null);

      ClassDefinition def = ClassDefinition.builder().withName(cd.getNameAsString()).withType(cd.getNameAsString())
          .withLineNumber(cd.getBegin().map(p -> p.line).orElse(-1)).withColumn(cd.getBegin().map(p -> p.column).orElse(-1)).withAnnotations(annotations)
          .withAccessModifiers(accessModifiers).withExtend(extend).withInterfaces(interfaces).build();
      classes.add(def);
    }
  }

}
