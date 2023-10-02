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
package configuration;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import initialization.InitDefaultValues;
import templateInput.ClassContainer;
import templateInput.TemplateInputParameters;
import templateInput.definition.MethodDefinition;
import templateInput.definition.VariableDefinition;

/**
 * Default adjusters for adding or removing input to/from the {@link TemplateInputParameters} after parsing is done.
 *
 * @author Daan
 */
public class DefaultAdjusters {

  /**
   * @return Returns an {@link ClassContainerAdjuster} to remove all depracated fields
   */
  public static ClassContainerAdjuster removeDepracatedFields() {
    return p -> removeVariableIf(p, var -> var.getAnnotations().contains("Deprecated"));
  }

  /**
   * @return Returns an {@link ClassContainerAdjuster} to remove all static fields
   */
  public static ClassContainerAdjuster removeStaticFields() {
    return p -> removeVariableIf(p, var -> var.getAccessModifiers().contains("static"));
  }

  /**
   * @return Returns an {@link ClassContainerAdjuster} to replace all primitive types with their object version (e.g. int to Integer).
   */
  public static ClassContainerAdjuster replaceFieldPrimitivesWithObjects() {
    return p -> changeVariable(p, var -> var.setType(InitDefaultValues.getObjectForPrimitive(var.getTypeWithoutParameters().toString())));
  }

  /**
   * @return Returns an {@link ClassContainerAdjuster} to remove all void methods
   */
  public static ClassContainerAdjuster removeVoidMethods() {
    return p -> removeMethodIf(p, met -> met.getType().toString().equals("void"));
  }

  /**
   * Changes the fields inside the {@link ClassContainer} using the given parameterChanger.
   *
   * @param parameters The {@link ClassContainer} in which the fields are changed.
   * @param parameterChanger a consumer changing an individual {@link VariableDefinition} inside the parameters.
   */
  public static void changeVariable(ClassContainer parameters, Consumer<VariableDefinition> parameterChanger) {
    List<? extends VariableDefinition> fields = parameters.getFields();
    for (int i = 0; i < fields.size(); i++) {
      parameterChanger.accept(fields.get(i));
    }
  }

  /**
   * Removes all fields inside the {@link ClassContainer} for which the input function returns true.
   *
   * @param parameters The {@link ClassContainer} in which the fields are removed.
   * @param removeIfTrue Function to determine if a variable should be removed.
   */
  public static void removeVariableIf(ClassContainer parameters, Function<VariableDefinition, Boolean> removeIfTrue) {
    List<? extends VariableDefinition> fields = parameters.getFields();
    for (int i = 0; i < fields.size();) {
      if (removeIfTrue.apply(fields.get(i))) {
        fields.remove(i);
      } else {
        i++;
      }
    }
  }

  /**
   * Removes all methods inside the {@link ClassContainer} for which the input function returns true.
   *
   * @param parameters The {@link ClassContainer} in which the methods are removed.
   * @param removeIfTrue Function to determine if a variable should be removed.
   */
  public static void removeMethodIf(ClassContainer parameters, Function<MethodDefinition, Boolean> removeIfTrue) {
    List<? extends MethodDefinition> methods = parameters.getMethods();
    for (int i = 0; i < methods.size();) {
      if (removeIfTrue.apply(methods.get(i))) {
        methods.remove(i);
      } else {
        i++;
      }
    }
  }

}
