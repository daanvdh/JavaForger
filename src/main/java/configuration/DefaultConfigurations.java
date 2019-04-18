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

import configuration.JavaForgerConfiguration.Builder;

/**
 * Class containing default {@link JavaForgerConfiguration}s for a set of templates.
 *
 * @author Daan
 */
public class DefaultConfigurations {

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
    JavaForgerConfiguration config = defaultConfig("innerBuilder.javat");
    config.addParameterAdjusters(DefaultAdjusters.replaceFieldPrimitivesWithObjects());
    return config;
  }

  public static JavaForgerConfiguration forBuilderAndTest() {
    JavaForgerConfiguration config = defaultConfiguration("innerBuilder.javat", "innerBuilderTest.javat");
    config.addParameterAdjusters(DefaultAdjusters.replaceFieldPrimitivesWithObjects());
    return config;
  }

  public static JavaForgerConfiguration forToString() {
    return defaultConfig("toString/complete.javat");
  }

  protected static JavaForgerConfiguration defaultConfig(String template) {
    return defaultBuilder(template).build();
  }

  protected static JavaForgerConfiguration defaultConfiguration(String template, String testTemplate) {
    return defaultBuilder(template).withChildConfig(defaultBuilder(testTemplate).withMergeClassProvider(ClassProvider.forMavenUnitTestFromInput()).build())
        .build();
  }

  protected static Builder defaultBuilder(String template) {
    return JavaForgerConfiguration.builder().withTemplate(template).withMergeClassProvider(new ClassProvider())
        .withParameterAdjusters(DefaultAdjusters.removeDepracatedFields(), DefaultAdjusters.removeStaticFields());
  }

}
