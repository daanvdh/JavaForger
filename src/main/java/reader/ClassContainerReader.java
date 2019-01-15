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

import com.github.javaparser.JavaParser;

import configuration.JavaForgerConfiguration;
import initialization.VariableInitializer;
import templateInput.ClassContainer;
import templateInput.definition.ClassDefinition;
import templateInput.definition.VariableDefinition;

/**
 * Reader for all data within a class.
 *
 * @author Daan
 */
public class ClassContainerReader {

  private FieldReader fieldReader = new FieldReader();
  private ClassReader classReader = new ClassReader();
  private MethodReader methodReader = new MethodReader();
  private VariableInitializer initializer = new VariableInitializer();

  public ClassContainer read(String inputClass) throws IOException {
    return read(inputClass, JavaForgerConfiguration.builder().build());
  }

  public ClassContainer read(String inputClass, JavaForgerConfiguration config) throws IOException {
    setupSymbolSolver(config);
    ClassDefinition def = classReader.read(inputClass);
    ClassContainer claz = new ClassContainer(def);
    List<VariableDefinition> fields = fieldReader.getFields(inputClass, config);
    initializer.init(fields);
    claz.setFields(fields);
    claz.setMethods(methodReader.read(inputClass));
    return claz;
  }

  private void setupSymbolSolver(JavaForgerConfiguration config) {
    JavaParser.getStaticConfiguration().setSymbolResolver(config.getSymbolSolver());
  }

}
