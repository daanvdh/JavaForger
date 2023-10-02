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

/**
 * Class containing default {@link JavaForgerConfiguration}s for a set of templates.
 *
 * @author Daan
 */
public class DefaultConfigurations {

  public static JavaForgerConfiguration forGetterSetter() {
    return defaultConfig("getterSetter.javat");
  }

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

  public static JavaForgerConfiguration forExtendableBuilderAndTest() {
    JavaForgerConfiguration config = defaultConfiguration("extendableInnerBuilder.javat", "extendableInnerBuilderTest.javat");
    config.addParameterAdjusters(DefaultAdjusters.replaceFieldPrimitivesWithObjects());
    return config;
  }

  /**
   * @return {@link JavaForgerConfiguration} for creating a toString method in the input class.
   */
  public static JavaForgerConfiguration forToString() {
    return defaultConfig("toString.javat");
  }

  /**
   * This {@link JavaForgerConfiguration} can only be used as {@link JavaForgerConfiguration#getChildConfigs()}.
   *
   * @return {@link JavaForgerConfiguration} for creating a toString method in the merge class from the parent.
   */
  public static JavaForgerConfiguration forChildToString() {
    JavaForgerConfiguration conf = forToString();
    conf.setInputClassProvider(ClassProvider.fromParentMergeClass());
    return conf;
  }

  /**
   * Create a unit test for a class that state. This template is work in progress mainly depending on the JavaDataFlow project.
   *
   * @return A {@link JavaForgerConfiguration} containing the settings for the state-full class test template.
   */
  public static JavaForgerConfiguration forStateFullClassTest() {
    return defaultBuilder("test/generic/stateFullClassTest.javat").mergeClassProvider(ClassProvider.forMavenUnitTestFromInput()).build();
  }

  /**
   * Create a unit test for a class that has no state. This template is work in progress mainly depending on the JavaDataFlow project.
   *
   * @return A {@link JavaForgerConfiguration} containing the settings for the state-less class test template.
   */
  public static JavaForgerConfiguration forStatelessClassTest() {
    return defaultBuilder("test/generic/statelessClassTest.javat").mergeClassProvider(ClassProvider.forMavenUnitTestFromInput())
        .configIfFileDoesNotExist(emptyTestFile()).build();
  }

  protected static JavaForgerConfiguration defaultConfig(String template) {
    return defaultBuilder(template).build();
  }

  protected static JavaForgerConfiguration defaultConfiguration(String template, String testTemplate) {
    return defaultBuilder(template).childConfig(defaultTestConfiguration(testTemplate)).build();
  }

  private static JavaForgerConfiguration defaultTestConfiguration(String testTemplate) {
    return defaultBuilder(testTemplate).mergeClassProvider(ClassProvider.forMavenUnitTestFromInput()).configIfFileDoesNotExist(emptyTestFile()).build();
  }

  private static JavaForgerConfiguration emptyTestFile() {
    return JavaForgerConfiguration.builder().template("test/common/emptyTestClass.javat").createFileIfNotExists(true)
        .mergeClassProvider(ClassProvider.fromParentMergeClass()).build();
  }

  protected static JavaForgerConfiguration.Builder defaultBuilder(String template) {
    return JavaForgerConfiguration.builder().template(template).mergeClassProvider(new ClassProvider())
        .parameterAdjusters(DefaultAdjusters.removeDepracatedFields(), DefaultAdjusters.removeStaticFields());
  }

}
