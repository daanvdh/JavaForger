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

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import parameters.ClassContainerAdjuster;
import parameters.TemplateInputParameters;
import templateInput.ClassContainer;
import templateInput.MethodDefinition;
import templateInput.VariableDefinition;

/**
 * Default adjusters for adding or removing input to/from the {@link TemplateInputParameters} after parsing is done.
 *
 * @author Daan
 */
public class DefaultAdjusters {

  public static ClassContainerAdjuster removeDepracatedFields() {
    return p -> removeVariableIf(p, var -> var.getAnnotations().contains("Deprecated"));
  }

  public static ClassContainerAdjuster removeStaticFields() {
    return p -> removeVariableIf(p, var -> var.getAccessModifiers().contains("static"));
  }

  public static ClassContainerAdjuster replaceFieldPrimitivesWithObjects() {
    VariableInitializer init = new VariableInitializer();
    return p -> changeVariable(p, var -> var.setType(init.getObjectForPrimitive(var.getType())));
  }

  public static ClassContainerAdjuster removeVoidMethods() {
    return p -> removeMethodIf(p, met -> met.getType().equals("void"));
  }

  public static void changeVariable(ClassContainer parameters, Consumer<VariableDefinition> f) {
    List<? extends VariableDefinition> fields = parameters.getFields();
    for (int i = 0; i < fields.size(); i++) {
      f.accept(fields.get(i));
    }
  }

  public static void removeVariableIf(ClassContainer parameters, Function<VariableDefinition, Boolean> f) {
    List<? extends VariableDefinition> fields = parameters.getFields();
    for (int i = 0; i < fields.size();) {
      if (f.apply(fields.get(i))) {
        fields.remove(i);
      } else {
        i++;
      }
    }
  }

  public static void removeMethodIf(ClassContainer parameters, Function<MethodDefinition, Boolean> f) {
    List<? extends MethodDefinition> methods = parameters.getMethods();
    for (int i = 0; i < methods.size();) {
      if (f.apply(methods.get(i))) {
        methods.remove(i);
      } else {
        i++;
      }
    }
  }

}
