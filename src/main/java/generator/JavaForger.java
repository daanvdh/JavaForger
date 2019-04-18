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
package generator;

import java.io.IOException;

import configuration.JavaForgerConfiguration;
import freemarker.template.TemplateException;

/**
 * Class with static methods for executing code generation and insertion.
 *
 * @author Daan
 */
public class JavaForger {

  private static final Generator generator = new Generator();

  /**
   * Executes the given {@link JavaForgerConfiguration} that contains the template. The parameters used to fill in the template will also come from the
   * configuration.
   *
   * @param config The Configuration containing the template and settings for how to process the input class to generate code.
   * @return The {@link CodeSnipit} containing the generated code.
   */
  public static CodeSnipit execute(JavaForgerConfiguration config) {
    Exception caught = null;
    try {
      return generator.execute(config);
    } catch (IOException e) {
      caught = e;
    } catch (TemplateException e) {
      caught = e;
    }
    throw new JavaForgerException(caught);
  }

  /**
   * Executes the given {@link JavaForgerConfiguration} that contains the template. The input class is used to fill in the given template with fields, methods
   * etc. derived from it.
   *
   * @param config The Configuration containing the template and settings for how to process the input class to generate code.
   * @param inputClass The full path to the class to be used as input for the template.
   * @return The {@link CodeSnipit} containing the generated code.
   */
  public static CodeSnipit execute(JavaForgerConfiguration config, String inputClass) {
    Exception caught = null;
    try {
      return generator.execute(config, inputClass);
    } catch (IOException e) {
      caught = e;
    } catch (TemplateException e) {
      caught = e;
    }
    throw new JavaForgerException(caught);
  }

  /**
   * Executes the given {@link JavaForgerConfiguration} that contains the template. The input class is used to fill in the given template with fields, methods
   * etc. derived from it. The generated code is merged into the output class.
   *
   * @param config The Configuration containing the template and settings for how to process the input class to generate code.
   * @param inputClass The full path to the class to be used as input for the template.
   * @param outputClass The class to be used to merge the generated code with.
   * @return The {@link CodeSnipit} containing the generated code.
   */
  public static CodeSnipit execute(JavaForgerConfiguration config, String inputClass, String outputClass) {
    Exception caught = null;
    try {
      JavaForgerConfiguration copy = JavaForgerConfiguration.builder(config).withMergeClass(outputClass).build();
      return generator.execute(copy, inputClass);
    } catch (IOException e) {
      caught = e;
    } catch (TemplateException e) {
      caught = e;
    }
    throw new JavaForgerException(caught);
  }

}
