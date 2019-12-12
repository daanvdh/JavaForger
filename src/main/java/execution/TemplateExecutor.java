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
package execution;

import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

import configuration.DefaultConfigurations;
import configuration.JavaForgerConfiguration;
import configuration.StaticJavaForgerConfiguration;
import generator.JavaForger;

/**
 * Helper class to generate parts of the code. Should never be called from any other class.
 *
 * @author Daan
 */
public final class TemplateExecutor {
  private static final String PATH_PREFIX = "C:/gitrepo";

  public static void main(String[] args) {
    setupSymbolSolver();
    String inputClass = "/JavaForger/src/main/java/dataflow/model/ParameterList.java";
    JavaForgerConfiguration config = DefaultConfigurations.forToString();

    // config.setMerge(false);
    // config.setRecursive(JavaForgerConfiguration::setMerge, false);
    // config.setRecursive(JavaForgerConfiguration::setOverride, true);
    // config.setOverride(true);

    JavaForger.execute(config, PATH_PREFIX + inputClass).print();
  }

  private static void setupSymbolSolver() {
    StaticJavaForgerConfiguration staticConfig = StaticJavaForgerConfiguration.getConfig();
    TypeSolver[] reflTypeSolver = {new ReflectionTypeSolver(), new JavaParserTypeSolver(PATH_PREFIX + "/JavaForger/src/main/java/")};
    TypeSolver typeSolver = new CombinedTypeSolver(reflTypeSolver);
    JavaSymbolSolver symbolSolver = new JavaSymbolSolver(typeSolver);
    staticConfig.setSymbolSolver(symbolSolver);
  }

}
