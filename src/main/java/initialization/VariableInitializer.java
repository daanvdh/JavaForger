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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import templateInput.definition.VariableDefinition;

/**
 * Class for initializing {@link VariableDefinition} for creating java code or unit tests from templates.
 *
 * @author Daan
 */
public class VariableInitializer {

  private static Map<String, InitValue> testNoInit = new HashMap<>();
  /** The first value that can be used to initialize the type given by the key of this hashMap. */
  private static Map<String, InitValue> defaultValue1 = new HashMap<>();
  /** The second value that can be used to initialize the type given by the key of this hashMap. This value is different from defaultValue1. */
  private static Map<String, InitValue> defaultValue2 = new HashMap<>();
  private static Map<String, InitValue> parameterizedVariables = new HashMap<>();
  private static Map<String, InitValue> emptyInit = new HashMap<>();
  private static Set<String> collections = new HashSet<>();

  private static Map<String, String> primitiveToObject = new HashMap<>();

  public VariableInitializer() {
    initializeJavaDefaults();
    initializeJavaNoInit();
    initializeJavaEmptyInit();
    initializeJavaCollections();
    initializeParameterizedJavaDefaults();
    initializePrimitiveToObject();
  }

  public void init(List<VariableDefinition> fields) {
    fields.stream().forEach(f -> init(f));
  }

  public String getObjectForPrimitive(String type) {
    return primitiveToObject.containsKey(type) ? primitiveToObject.get(type) : type;
  }

  public void init(VariableDefinition var) {
    if (this.defaultValue1.containsKey(var.getType())) {
      setDefaultInit1(var);
      setDefaultInit2(var);
      setNoInit(var);
    } else if (var.getType().contains("<")) {
      initParameterized(var);
    } else {
      // TODO the stuff below should be replaced by a call to the Generator with a custom "builderUsage.javat" file defining the start and end of a builder.
      String init = var.getType() + ".builder().build()";
      var.setInit1(init);
      var.setInit2(init);
      var.setNoInit(getNoInitFor(var.getType()));
    }
    var.setDefaultInit(emptyInit.containsKey(var.getTypeWithoutParameters()) ? emptyInit.get(var.getTypeWithoutParameters()).getValue() : null);
    var.setCollection(collections.contains(var.getTypeWithoutParameters()));
  }

  private void setNoInit(VariableDefinition var) {
    if (this.testNoInit.containsKey(var.getType())) {
      InitValue value = this.testNoInit.get(var.getType());
      var.setNoInit(value.getValue());
      var.addInitImports(value.getImports());
    } else {
      var.setNoInit("null");
    }
  }

  private void setDefaultInit1(VariableDefinition var) {
    if (defaultValue1.containsKey(var.getType())) {
      InitValue value = defaultValue1.get(var.getType());
      var.setInit1(value.getValue());
      var.addInitImports(value.getImports());
    }
  }

  private void setDefaultInit2(VariableDefinition var) {
    if (defaultValue2.containsKey(var.getType())) {
      InitValue value = defaultValue2.get(var.getType());
      var.setInit2(value.getValue());
      var.addInitImports(value.getImports());
    }
  }

  private void initParameterized(VariableDefinition var) {
    String mainType = var.getTypeWithoutParameters();
    StringBuilder sb1 = new StringBuilder();
    StringBuilder sb2 = new StringBuilder();
    if (this.parameterizedVariables.containsKey(mainType)) {
      InitValue value = this.parameterizedVariables.get(mainType);
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
    var.setNoInit(getNoInitFor(mainType));
  }

  private List<VariableDefinition> getSubTypes(VariableDefinition var) {
    int indexOf = var.getType().indexOf("<");
    String subString = var.getType().substring(indexOf + 1, var.getType().length() - 1);
    List<String> subVariableTypes = splitSubTypes(subString);
    List<VariableDefinition> subTypes =
        subVariableTypes.stream().map(subType -> VariableDefinition.builder().withType(subType).build()).collect(Collectors.toList());
    // This is a recursive call, to the caller
    subTypes.forEach(subVar -> init(subVar));
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

  private String getNoInitFor(String type) {
    return this.testNoInit.containsKey(type) ? this.testNoInit.get(type).getValue() : "null";
  }

  private void initializeJavaDefaults() {
    defaultValue1.put("int", new InitValue("1"));
    defaultValue2.put("int", new InitValue("2"));
    defaultValue1.put("Integer", new InitValue("3"));
    defaultValue2.put("Integer", new InitValue("4"));
    defaultValue1.put("boolean", new InitValue("true"));
    defaultValue2.put("boolean", new InitValue("false"));
    defaultValue1.put("Boolean", new InitValue("false"));
    defaultValue2.put("Boolean", new InitValue("true"));
    defaultValue1.put("long", new InitValue("1L"));
    defaultValue2.put("long", new InitValue("2L"));
    defaultValue1.put("Long", new InitValue("3L"));
    defaultValue2.put("Long", new InitValue("4L"));
    defaultValue1.put("double", new InitValue("1.0"));
    defaultValue2.put("double", new InitValue("2.0"));
    defaultValue1.put("Double", new InitValue("3.0"));
    defaultValue2.put("Double", new InitValue("4.0"));
    defaultValue1.put("float", new InitValue("1.0"));
    defaultValue2.put("float", new InitValue("2.0"));
    defaultValue1.put("Float", new InitValue("3.0"));
    defaultValue2.put("Float", new InitValue("4.0"));
    defaultValue1.put("String", new InitValue("\"a\""));
    defaultValue2.put("String", new InitValue("\"b\""));
    defaultValue1.put("Object", new InitValue("new Object()"));
    defaultValue2.put("Object", new InitValue("new Object()"));

    // Special ones
    defaultValue1.put("LocalDateTime", new InitValue("LocalDateTime.of(2017, 3, 25, 0, 0)"));
    defaultValue2.put("LocalDateTime", new InitValue("LocalDateTime.of(2018, 4, 26, 1, 1)"));
    defaultValue1.put("BigDecimal", new InitValue("BigDecimal.valueOf(5)", "java.math.BigDecimal"));
    defaultValue2.put("BigDecimal", new InitValue("BigDecimal.valueOf(6)", "java.math.BigDecimal"));
    defaultValue1.put("ZonedDateTime",
        new InitValue("ZonedDateTime.of(2017, 4, 25, 10, 0, 0, 0, TimeZone.getTimeZone(\"UTC\").toZoneId())", "java.time.ZonedDateTime", "java.util.TimeZone"));
    defaultValue2.put("ZonedDateTime",
        new InitValue("ZonedDateTime.of(2018, 5, 26, 11, 0, 0, 0, TimeZone.getTimeZone(\"UTC\").toZoneId())", "java.time.ZonedDateTime", "java.util.TimeZone"));
    defaultValue1.put("Date", new InitValue("Date.from(ZonedDateTime.of(2017, 4, 25, 10, 0, 0, 0, TimeZone.getTimeZone(\"UTC\").toZoneId()).toInstant())",
        "java.time.ZonedDateTime", "java.util.TimeZone"));
    defaultValue2.put("Date", new InitValue("Date.from(ZonedDateTime.of(2018, 5, 26, 11, 0, 0, 0, TimeZone.getTimeZone(\"UTC\").toZoneId()).toInstant())",
        "java.time.ZonedDateTime", "java.util.TimeZone"));
    defaultValue1.put("Length", new InitValue("SI.METER", "javax.measure.unit.SI"));
    defaultValue2.put("Length", new InitValue("SI.KILOMETER", "javax.measure.unit.SI"));
    defaultValue1.put("Volume", new InitValue("SI.CUBIC_METRE", "javax.measure.unit.SI"));
    defaultValue2.put("Volume", new InitValue("SI.CUBIC_METRE", "javax.measure.unit.SI")); // no alternative
    defaultValue1.put("Mass", new InitValue("SI.KILOGRAM", "javax.measure.unit.SI"));
    defaultValue2.put("Mass", new InitValue("SI.KILOGRAM", "javax.measure.unit.SI"));
  }

  private void initializeJavaEmptyInit() {
    emptyInit.put("Optional", new InitValue("Optional.empty()"));
    emptyInit.put("List", new InitValue("new ArrayList<>()"));
    emptyInit.put("HashMap", new InitValue("new HashMap<>()"));
    emptyInit.put("Map", new InitValue("new HashMap<>()"));
    emptyInit.put("Set", new InitValue("new HashSet<>()"));
    emptyInit.put("HashSet", new InitValue("new HashSet<>()"));
    emptyInit.put("ArrayListValuedHashMap", new InitValue("new ArrayListValuedHashMap<>()"));
  }

  private void initializeJavaNoInit() {
    testNoInit.put("int", new InitValue("0"));
    testNoInit.put("boolean", new InitValue("false"));
    testNoInit.put("long", new InitValue("0L"));
    testNoInit.put("double", new InitValue("0.0"));
    testNoInit.put("float", new InitValue("0.0"));
    testNoInit.put("Optional", new InitValue("Optional.empty()"));
    testNoInit.put("List", new InitValue("Collections.emptyList()", "java.util.Collections"));
    testNoInit.put("ArrayList", new InitValue("Collections.emptyList()", "java.util.Collections"));
    testNoInit.put("HashMap", new InitValue("Collections.emptyMap()", "java.util.Collections"));
    testNoInit.put("Map", new InitValue("Collections.emptyMap()", "java.util.Collections"));
    testNoInit.put("Set", new InitValue("Collections.emptySet()", "java.util.Collections"));
    testNoInit.put("HashSet", new InitValue("Collections.emptySet()", "java.util.Collections"));
  }

  private void initializePrimitiveToObject() {
    primitiveToObject.put("int", "Integer");
    primitiveToObject.put("boolean", "Boolean");
    primitiveToObject.put("long", "Long");
    primitiveToObject.put("double", "Double");
    primitiveToObject.put("float", "Float");
  }

  private void initializeParameterizedJavaDefaults() {
    parameterizedVariables.put("Collection", new InitValue("Collections.singletonList(", "java.util.Collections"));
    parameterizedVariables.put("List", new InitValue("Collections.singletonList(", "java.util.Collections"));
    // This will not compile, but better than creating a builder for it.
    parameterizedVariables.put("ArrayList", new InitValue("Collections.singletonList(", "java.util.Collections"));
    parameterizedVariables.put("Map", new InitValue("Collections.singletonMap(", "java.util.Collections"));
    parameterizedVariables.put("HashMap", new InitValue("Collections.singletonMap(", "java.util.Collections"));
    parameterizedVariables.put("Set", new InitValue("Collections.singleton(", "java.util.Collections"));
    parameterizedVariables.put("HashSet", new InitValue("Collections.singleton(", "java.util.Collections"));
    parameterizedVariables.put("ArrayListValuedHashMap", new InitValue("new ArrayListValuedHashMap<>("));

    // Special ones

    parameterizedVariables.put("DecimalMeasure", new InitValue("DecimalMeasure.valueOf(BigDecimal.ZERO, ", "java.math.BigDecimal"));

  }

  private void initializeJavaCollections() {
    collections.add("List");
    collections.add("ArrayList");
    collections.add("Set");
    collections.add("HashSet");
    collections.add("Collection");
  }

}
