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
