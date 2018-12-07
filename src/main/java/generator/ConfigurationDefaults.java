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

import com.github.javaparser.ast.Modifier;

import generator.JavaForgerConfiguration.Builder;

/**
 * Class containing default {@link JavaForgerConfiguration}s for a set of templates.
 *
 * @author Daan
 */
public class ConfigurationDefaults {

  public static JavaForgerConfiguration forEquals() {
    return defaultConfig("equals.javat");
  }

  public static JavaForgerConfiguration forEqualsAndTest() {
    return defaultConfiguration("equals.javat", "equalsTest.javat");
  }

  public static JavaForgerConfiguration forHashCode() {
    return defaultConfig("hashCode.javat");
  }

  public static JavaForgerConfiguration forHashCodeAndTest() {
    return defaultConfiguration("hashCode.javat", "hashCodeTest.javat");
  }

  public static JavaForgerConfiguration forBuilder() {
    return defaultConfig("innerBuilder.javat");
  }

  public static JavaForgerConfiguration forBuilderAndTest() {
    return defaultConfiguration("innerBuilder.javat", "innerBuilderTest.javat");
  }

  public static JavaForgerConfiguration forToString() {
    return defaultConfig("toString.javat");
  }

  private static JavaForgerConfiguration defaultConfig(String template) {
    return defaultBuilder(template).build();
  }

  private static JavaForgerConfiguration defaultConfiguration(String template, String testTemplate) {
    return defaultBuilder(template).withChildConfig(defaultBuilder(testTemplate).withMergeClassProvider(MergeClassProvider.forMavenUnitTest()).build()).build();
  }

  private static Builder defaultBuilder(String template) {
    return JavaForgerConfiguration.builder().withTemplate(template).withMergeClassProvider(new MergeClassProvider()).withoutModifiers(Modifier.STATIC);
  }

}
