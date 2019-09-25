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
package initialization;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import templateInput.ClassContainer;
import templateInput.definition.InitializedTypeDefinition;
import templateInput.definition.MethodDefinition;
import templateInput.definition.VariableDefinition;

/**
 * Class for initializing {@link VariableDefinition} for creating java code or unit tests from templates.
 *
 * @author Daan
 */
public class InitializationService {

  private InitDefaultValues defaults = new InitDefaultValues();
  private InitConverter converter = new InitConverter();

  public void init(ClassContainer claz) {
    converter.reset();
    initVariables(claz.getFields());
    initMethods(claz.getMethods());
    initMethods(claz.getConstructors());
  }

  public void init(InitializedTypeDefinition var) {
    converter.reset();
    initialize(var);
  }

  private void initialize(InitializedTypeDefinition var) {
    if (defaults.containsDefaultValue(var.getType().toString())) {
      setDefaultInit1(var);
      setDefaultInit2(var);
      setNoInit(var);
    } else if (var.getType().toString().contains("<")) {
      initParameterized(var);
    } else {
      // TODO the stuff below should be replaced by a call to the Generator with a custom "builderUsage.javat" file defining the start and end of a builder.
      String init = var.getType() + ".builder().build()";
      var.setInit1(init);
      var.setInit2(init);
      var.setNoInit(defaults.getNoInitFor(var.getType().toString()));
    }
    var.setDefaultInit(defaults.containsEmptyInit(var.getTypeWithoutParameters()) ? defaults.getEmptyInit(var.getTypeWithoutParameters()).getValue() : null);
    var.setCollection(defaults.isCollection(var.getTypeWithoutParameters()));
  }

  private void initMethods(List<? extends MethodDefinition> methods) {
    methods.stream().forEach(this::initialize);
    methods.forEach(m -> initVariables(m.getParameters()));
  }

  private void initVariables(List<? extends VariableDefinition> list) {
    list.stream().forEach(this::initialize);
  }

  private void setNoInit(InitializedTypeDefinition var) {
    if (defaults.containsTestNoInit(var.getType().toString())) {
      InitValue value = defaults.getTestNoInit(var.getType().toString());
      var.setNoInit(value.getValue());
      var.addInitImports(value.getImports());
    } else {
      var.setNoInit("null");
    }
  }

  private void setDefaultInit1(InitializedTypeDefinition var) {
    if (defaults.containsDefaultValue(var.getType().toString())) {
      InitValue value = defaults.getDefaultValue1(var.getType().toString());
      var.setInit1(converter.convert(value.getValue()));
      var.addInitImports(value.getImports());
    }
  }

  private void setDefaultInit2(InitializedTypeDefinition var) {
    if (defaults.containsDefaultValue(var.getType().toString())) {
      InitValue value = defaults.getDefaultValue2(var.getType().toString());
      var.setInit2(converter.convert(value.getValue()));
      var.addInitImports(value.getImports());
    }
  }

  private void initParameterized(InitializedTypeDefinition var) {
    String mainType = var.getTypeWithoutParameters();
    StringBuilder sb1 = new StringBuilder();
    StringBuilder sb2 = new StringBuilder();
    if (defaults.isParameterizedVariable(mainType)) {
      InitValue value = defaults.getParameterizedVariable(mainType);
      sb1.append(value.getValue());
      sb2.append(value.getValue());
      var.addInitImports(value.getImports());
      List<VariableDefinition> subTypes = getSubTypes(var);

      String init1 = subTypes.stream().map(VariableDefinition::getInit1).collect(Collectors.joining(", "));
      String init2 = subTypes.stream().map(VariableDefinition::getInit2).collect(Collectors.joining(", "));

      sb1.append(init1 + ")");
      sb2.append(init2 + ")");

      subTypes.stream().forEach(v -> var.addInitImports(v.getInitImports()));

    } else {
      sb1.append(mainType + ".builder().build()");
      sb2.append(mainType + ".builder().build()");
    }
    var.setInit1(sb1.toString());
    var.setInit2(sb2.toString());
    var.setNoInit(defaults.getNoInitFor(mainType));
  }

  private List<VariableDefinition> getSubTypes(InitializedTypeDefinition var) {
    int indexOf = var.getType().toString().indexOf("<");
    String subString = var.getType().toString().substring(indexOf + 1, var.getType().toString().length() - 1);
    List<String> subVariableTypes = splitSubTypes(subString);
    List<VariableDefinition> subTypes =
        subVariableTypes.stream().map(subType -> VariableDefinition.builder().type(subType).build()).collect(Collectors.toList());
    // This is a recursive call, to the caller
    subTypes.forEach(subVar -> initialize(subVar));
    return subTypes;
  }

  /**
   * This method receives the inner type of a parmeterized type (e.g. 'InnerType1, ? extends InnerType2' which originates from 'ParameterizedType<InnerType1, ?
   * extends InnerType2>'). All comma-seperated types are then split into subStrings and returned. This method does not split any inner parameterized types,
   * this should be done by recursively calling this method on inner types.
   *
   * @param type The comma-separated inner type of a parameterized type.
   * @return
   */
  private List<String> splitSubTypes(String type) {
    List<String> subVariableTypes = new ArrayList<>();
    int withinBrackets = 0;
    StringBuilder currentVar = new StringBuilder();

    for (char c : type.toCharArray()) {
      if (withinBrackets > 0) {
        currentVar.append(c);
        if (c == '>') {
          withinBrackets--;
          if (withinBrackets <= 0) {
            subVariableTypes.add(currentVar.toString());
            currentVar = new StringBuilder();
          }
        }
      } else if (c == '<') {
        currentVar.append(c);
        withinBrackets++;
      } else if (Character.isLetter(c) || Character.isDigit(c)) {
        currentVar.append(c);
      } else if (c == '?') {
        // This has to be added so that the if statement checking 'extends' can safely remove it.
        subVariableTypes.add("?");
      } else if (currentVar.length() > 0) {
        String current = currentVar.toString();
        if (current.equals("extends")) {
          // We do not want to store extends
          // If this variable is the keyword extends, then the previous variable does not define a type
          subVariableTypes.remove(subVariableTypes.size() - 1);
          currentVar = new StringBuilder();
        } else {
          subVariableTypes.add(current);
          currentVar = new StringBuilder();
        }
      }
    }

    if (currentVar.length() > 0) {
      subVariableTypes.add(currentVar.toString());
    }
    return subVariableTypes;
  }

}
