/*
 * Copyright 2019 by Daan van den Heuvel.
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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Class containing all defaults for different java types in different contexts.
 *
 * @author Daan
 */
public class InitDefaultValues {

  private static Map<String, InitValue> testNoInit = new HashMap<>();
  /** The first value that can be used to initialize the type given by the key of this hashMap. */
  private static Map<String, InitValue> defaultValue1 = new HashMap<>();
  // TODO The defaultValue2 needs to be removed at some point since we now have the InitConverter. But we have to come up with a solution for Date because the
  // random numbers need to be modulo 12 (months), 24 (hours), 60 (minutes)
  /** The second value that can be used to initialize the type given by the key of this hashMap. This value is different from defaultValue1. */
  private static Map<String, InitValue> defaultValue2 = new HashMap<>();
  private static Map<String, InitValue> parameterizedVariables = new HashMap<>();
  private static Map<String, InitValue> emptyInit = new HashMap<>();
  private static Set<String> collections = new HashSet<>();

  private static Map<String, String> primitiveToObject;

  public InitDefaultValues() {
    initializeJavaDefaults();
    initializeJavaNoInit();
    initializeJavaEmptyInit();
    initializeJavaCollections();
    initializeParameterizedJavaDefaults();
    initializePrimitiveToObject();
  }

  public static String getObjectForPrimitive(String type) {
    return isPrimitive(type) ? primitiveToObject.get(type) : type;
  }

  public static boolean isPrimitive(String type) {
    initializePrimitiveToObject();
    return primitiveToObject.containsKey(type);
  }

  public boolean containsDefaultValue(String type) {
    return defaultValue1.containsKey(type);
  }

  public InitValue getDefaultValue1(String type) {
    return defaultValue1.get(type);
  }

  public InitValue getDefaultValue2(String type) {
    return defaultValue2.get(type);
  }

  public boolean containsEmptyInit(String type) {
    return emptyInit.containsKey(type);
  }

  public InitValue getEmptyInit(String type) {
    return emptyInit.get(type);
  }

  public boolean isCollection(String type) {
    return collections.contains(type);
  }

  public boolean containsTestNoInit(String type) {
    return testNoInit.containsKey(type);
  }

  public InitValue getTestNoInit(String type) {
    return testNoInit.get(type);
  }

  public String getNoInitFor(String type) {
    return this.testNoInit.containsKey(type) ? this.testNoInit.get(type).getValue() : "null";
  }

  public boolean isParameterizedVariable(String type) {
    return parameterizedVariables.containsKey(type);
  }

  public InitValue getParameterizedVariable(String type) {
    return parameterizedVariables.get(type);
  }

  private void initializeJavaDefaults() {
    defaultValue1.put("int", new InitValue("%d"));
    defaultValue2.put("int", new InitValue("%d"));
    defaultValue1.put("Integer", new InitValue("%d"));
    defaultValue2.put("Integer", new InitValue("%d"));
    defaultValue1.put("boolean", new InitValue("true"));
    defaultValue2.put("boolean", new InitValue("false"));
    defaultValue1.put("Boolean", new InitValue("false"));
    defaultValue2.put("Boolean", new InitValue("true"));
    defaultValue1.put("long", new InitValue("%dL"));
    defaultValue2.put("long", new InitValue("%dL"));
    defaultValue1.put("Long", new InitValue("%dL"));
    defaultValue2.put("Long", new InitValue("%dL"));
    defaultValue1.put("double", new InitValue("%d.%d"));
    defaultValue2.put("double", new InitValue("%d.%d"));
    defaultValue1.put("Double", new InitValue("%d.%d"));
    defaultValue2.put("Double", new InitValue("%d.%d"));
    defaultValue1.put("float", new InitValue("%d.%d"));
    defaultValue2.put("float", new InitValue("%d.%d"));
    defaultValue1.put("Float", new InitValue("%d.%d"));
    defaultValue2.put("Float", new InitValue("%d.%d"));
    defaultValue1.put("String", new InitValue("\"%s\""));
    defaultValue2.put("String", new InitValue("\"%s\""));
    defaultValue1.put("Object", new InitValue("new Object()"));
    defaultValue2.put("Object", new InitValue("new Object()"));

    // Special ones
    defaultValue1.put("LocalDateTime", new InitValue("LocalDateTime.of(2017, 3, 25, 0, 0)"));
    defaultValue2.put("LocalDateTime", new InitValue("LocalDateTime.of(2018, 4, 26, 1, 1)"));
    defaultValue1.put("BigDecimal", new InitValue("BigDecimal.valueOf(%d)", "java.math.BigDecimal"));
    defaultValue2.put("BigDecimal", new InitValue("BigDecimal.valueOf(%d)", "java.math.BigDecimal"));
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
    defaultValue1.put("Duration", new InitValue("Duration.ofDays(%d);", " java.time.Duration"));
    defaultValue2.put("Duration", new InitValue("Duration.ofDays(%d);", " java.time.Duration"));

  }

  private void initializeJavaEmptyInit() {
    emptyInit.put("Collection", new InitValue("new ArrayList<>()"));
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
    testNoInit.put("Collection", new InitValue("Collections.emptyList()", "java.util.Collections"));
    testNoInit.put("List", new InitValue("Collections.emptyList()", "java.util.Collections"));
    testNoInit.put("ArrayList", new InitValue("Collections.emptyList()", "java.util.Collections"));
    testNoInit.put("HashMap", new InitValue("Collections.emptyMap()", "java.util.Collections"));
    testNoInit.put("Map", new InitValue("Collections.emptyMap()", "java.util.Collections"));
    testNoInit.put("Set", new InitValue("Collections.emptySet()", "java.util.Collections"));
    testNoInit.put("HashSet", new InitValue("Collections.emptySet()", "java.util.Collections"));
  }

  private static void initializePrimitiveToObject() {
    if (primitiveToObject == null) {
      primitiveToObject = new HashMap<>();
      primitiveToObject.put("int", "Integer");
      primitiveToObject.put("boolean", "Boolean");
      primitiveToObject.put("long", "Long");
      primitiveToObject.put("double", "Double");
      primitiveToObject.put("float", "Float");
    }
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

    parameterizedVariables.put("DecimalMeasure", new InitValue("DecimalMeasure.valueOf(BigDecimal.TEN, ", "java.math.BigDecimal"));

  }

  private void initializeJavaCollections() {
    collections.add("List");
    collections.add("ArrayList");
    collections.add("Set");
    collections.add("HashSet");
    collections.add("Collection");
  }

}
