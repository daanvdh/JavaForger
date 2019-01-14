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

import java.util.Arrays;
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
  private static Map<String, String> parameterizedVariables = new HashMap<>();
  private static Map<String, String> emptyInit = new HashMap<>();
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
      var.setInit1(init1(var.getType()));
      var.setInit2(init2(var.getType()));
      var.setNoInit(getNoInitFor(var.getType()));
    } else if (var.getType().contains("<")) {
      initParameterized(var);
    } else {
      // TODO the stuff below should be replaced by a call to the Generator with a custom "builderUsage.javat" file defining the start and end of a builder.
      String init = var.getType() + ".builder().build()";
      var.setInit1(init);
      var.setInit2(init);
      var.setNoInit(getNoInitFor(var.getType()));
    }
    var.setDefaultInit(emptyInit.get(var.getTypeWithoutParameters()));
    var.setCollection(collections.contains(var.getTypeWithoutParameters()));
  }

  private void initParameterized(VariableDefinition var) {
    String mainType = var.getTypeWithoutParameters();
    StringBuilder sb1 = new StringBuilder();
    StringBuilder sb2 = new StringBuilder();
    if (this.parameterizedVariables.containsKey(mainType)) {
      sb1.append(this.parameterizedVariables.get(mainType));
      sb2.append(this.parameterizedVariables.get(mainType));
      List<VariableDefinition> subTypes = getSubTypes(var);

      String init1 = subTypes.stream().map(VariableDefinition::getInit1).collect(Collectors.joining(", "));
      String init2 = subTypes.stream().map(VariableDefinition::getInit2).collect(Collectors.joining(", "));

      sb1.append(init1 + ")");
      sb2.append(init2 + ")");
    } else {
      sb1.append(mainType + ".builder().build()");
      sb2.append(mainType + ".builder().build()");
    }
    var.setInit1(sb1.toString());
    var.setInit2(sb2.toString());
    var.setNoInit(getNoInitFor(mainType));
  }

  private String init1(String type) {
    return defaultValue1.get(type).getValue();
  }

  private String init2(String type) {
    return defaultValue2.get(type).getValue();
  }

  private List<VariableDefinition> getSubTypes(VariableDefinition var) {
    int indexOf = var.getType().indexOf("<");
    String subString = var.getType().substring(indexOf + 1, var.getType().length() - 1);
    String[] split = subString.split(",");
    List<VariableDefinition> subTypes = Arrays.stream(split).map(subType -> removeExtends(subType))
        .map(subType -> VariableDefinition.builder().withType(subType).build()).collect(Collectors.toList());
    subTypes.forEach(subVar -> init(subVar));
    return subTypes;
  }

  private String removeExtends(String subType) {
    String a = subType;
    String s = "extends";
    if (subType.contains(s)) {
      int firstIndex = subType.indexOf(s) + s.length() + 1;
      a = subType.substring(firstIndex);
    }
    return a;
  }

  private String getNoInitFor(String type) {
    return (this.testNoInit.containsKey(type)) ? this.testNoInit.get(type).getValue() : "null";
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

    // Special ones
    defaultValue1.put("LocalDateTime", new InitValue("LocalDateTime.of(2017, 3, 25, 0, 0)"));
    defaultValue2.put("LocalDateTime", new InitValue("LocalDateTime.of(2018, 4, 26, 1, 1)"));
    defaultValue1.put("BigDecimal", new InitValue("BigDecimal.valueOf(5)"));
    defaultValue2.put("BigDecimal", new InitValue("BigDecimal.valueOf(6)"));
    defaultValue1.put("Date", new InitValue("Date.from(ZonedDateTime.of(2017, 4, 25, 10, 0, 0, 0, TimeZone.getTimeZone(\"UTC\").toZoneId()).toInstant())"));
    defaultValue2.put("Date", new InitValue("Date.from(ZonedDateTime.of(2018, 5, 26, 11, 0, 0, 0, TimeZone.getTimeZone(\"UTC\").toZoneId()).toInstant())"));
    defaultValue1.put("Length", new InitValue("SI.METER"));
    defaultValue2.put("Length", new InitValue("SI.KILOMETER"));
    defaultValue1.put("Volume", new InitValue("SI.CUBIC_METRE"));
    defaultValue2.put("Volume", new InitValue("SI.CUBIC_METRE")); // no alternative
    defaultValue1.put("Mass", new InitValue("SI.KILOGRAM"));
    defaultValue2.put("Mass", new InitValue("SI.KILOGRAM"));
  }

  private void initializeJavaEmptyInit() {
    emptyInit.put("Optional", "Optional.empty()");
    emptyInit.put("List", "new ArrayList<>()");
    emptyInit.put("HashMap", "new HashMap<>()");
    emptyInit.put("Map", "new HashMap<>()");
    emptyInit.put("Set", "new HashSet<>()");
    emptyInit.put("HashSet", "new HashSet<>()");
    emptyInit.put("ArrayListValuedHashMap", "new ArrayListValuedHashMap<>()");
  }

  private void initializeJavaNoInit() {
    testNoInit.put("int", new InitValue("0"));
    testNoInit.put("boolean", new InitValue("false"));
    testNoInit.put("long", new InitValue("0L"));
    testNoInit.put("double", new InitValue("0.0"));
    testNoInit.put("float", new InitValue("0.0"));
    testNoInit.put("Optional", new InitValue("Optional.empty()"));
    testNoInit.put("List", new InitValue("Collections.emptyList()"));
    testNoInit.put("ArrayList", new InitValue("Collections.emptyList()"));
    testNoInit.put("HashMap", new InitValue("Collections.emptyMap()"));
    testNoInit.put("Map", new InitValue("Collections.emptyMap()"));
    testNoInit.put("Set", new InitValue("Collections.emptySet()"));
    testNoInit.put("HashSet", new InitValue("Collections.emptySet()"));
  }

  private void initializePrimitiveToObject() {
    primitiveToObject.put("int", "Integer");
    primitiveToObject.put("boolean", "Boolean");
    primitiveToObject.put("long", "Long");
    primitiveToObject.put("double", "Double");
    primitiveToObject.put("float", "Float");
  }

  private void initializeParameterizedJavaDefaults() {
    parameterizedVariables.put("Collection", "Collections.singletonList(");
    parameterizedVariables.put("List", "Collections.singletonList(");
    parameterizedVariables.put("ArrayList", "Collections.singletonList("); // This will not compile, but better than creating a builder for it.
    parameterizedVariables.put("Map", "Collections.singletonMap(");
    parameterizedVariables.put("HashMap", "Collections.singletonMap(");
    parameterizedVariables.put("Set", "Collections.singleton(");
    parameterizedVariables.put("HashSet", "Collections.singleton(");
    parameterizedVariables.put("ArrayListValuedHashMap", "new ArrayListValuedHashMap<>(");

    // Special ones

    parameterizedVariables.put("DecimalMeasure", "DecimalMeasure.valueOf(BigDecimal.ZERO, ");

  }

  private void initializeJavaCollections() {
    collections.add("List");
    collections.add("ArrayList");
    collections.add("Set");
    collections.add("HashSet");
    collections.add("Collection");
  }

}
