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

import java.util.function.Function;

import generator.Generator;
import generator.JavaForgerException;

/**
 * A functional interface to provide a path to a class given an input string. The origin of the input string can be defined by the {@link ProvideFrom} enum. The
 * {@link Generator} uses this to determine merge classes and input classes.
 *
 * @author Daan
 */
public class ClassProvider {

  /**
   * Determines what the {@link Generator} should provide as input to the {@link ClassProvider} to determine the path to the merge class.
   */
  public enum ProvideFrom {
    /** Indicates the provider does not require any input because the class is already defined. */
    SELF,
    /** The merge class of the parent configuration is required as input. */
    PARENT_CONFIG_MERGE_CLASS,
    /** The input class for the {@link Generator} is required as input. */
    INPUT_CLASS
  }

  private final ProvideFrom provideFrom;
  private final Function<String, String> provide;

  /**
   * Provider to return the same class as the input class.
   */
  public ClassProvider() {
    this.provideFrom = ProvideFrom.INPUT_CLASS;
    this.provide = s -> {
      validate(s);
      return s;
    };
  }

  /**
   * Provider to return the class defined by the input path.
   *
   * @param path The path to the class to merge with.
   */
  public ClassProvider(String path) {
    this.provideFrom = ProvideFrom.SELF;
    this.provide = s -> path;
  }

  /**
   * Provider to create a custom path to a class after receiving a path from the given {@link ProvideFrom}.
   *
   * @param provideFrom The {@link ProvideFrom} defining where the input path should be taken from.
   * @param provide The provider for determining the output path after receiving the input path.
   */
  public ClassProvider(ProvideFrom provideFrom, Function<String, String> provide) {
    this.provideFrom = provideFrom;
    this.provide = provide;
  }

  /**
   * Provider to create a custom path to a class after receiving the path to the input class.
   *
   * @param provider The provider for determining the output path after receiving the input path.
   * @return new {@link ClassProvider}
   */
  public static ClassProvider fromInputClass(Function<String, String> provider) {
    return new ClassProvider(ProvideFrom.INPUT_CLASS, provider);
  }

  /**
   * @return The same class as the class to which the parent {@link JavaForgerConfiguration} was merged.
   */
  public static ClassProvider fromParentMergeClass() {
    return fromParentMergeClass(s -> s);
  }

  /**
   * Provider to create a custom path to a class after receiving the path to the merge class from the parent {@link JavaForgerConfiguration}.
   *
   * @param provider The provider for determining the output path after receiving the input path.
   * @return new {@link ClassProvider}
   */
  public static ClassProvider fromParentMergeClass(Function<String, String> provider) {
    return new ClassProvider(ProvideFrom.PARENT_CONFIG_MERGE_CLASS, provider);
  }

  /**
   * Calculates the path of the maven unit test by replacing 'main' with 'test' and replacing '.java' with 'Test.java'.
   *
   * @return The path to the maven unit test.
   */
  public static ClassProvider forMavenUnitTestFromInput() {
    return new ClassProvider(ProvideFrom.INPUT_CLASS, s -> PathConverter.toMavenUnitTestPath(s));
  }

  /**
   * Calculates the path of the maven unit test by replacing 'main' with 'test' and replacing '.java' with 'Test.java'.
   *
   * @return The path to the maven unit test.
   */
  public static ClassProvider forMavenUnitTestFromParent() {
    return new ClassProvider(ProvideFrom.PARENT_CONFIG_MERGE_CLASS, s -> PathConverter.toMavenUnitTestPath(s));
  }

  /**
   * Provider to provide the class to which the parent {@link JavaForgerConfiguration} was merged.
   *
   * @return the parent config merge class
   */
  public static ClassProvider sameAsParentMergeClass() {
    return new ClassProvider(ProvideFrom.PARENT_CONFIG_MERGE_CLASS, s -> s);
  }

  /**
   * Calculates the path to the class given the input path.
   *
   * @param path The input path.
   * @return Path to the class defined by this provider.
   */
  public String provide(String path) {
    return provide.apply(path);
  }

  /**
   * Selects one of the 2 input values depending on the {@link ProvideFrom} that was set. Then uses that input to calculate the new path.
   *
   * @param parentInputClass The input path.
   * @param parentMergeClass Path to the merge class to which the parent {@link JavaForgerConfiguration} was merged.
   * @return Path to the class defined by this provider.
   */
  public String provide(String parentInputClass, String parentMergeClass) {
    String path;
    switch (provideFrom) {
    case INPUT_CLASS:
      path = parentInputClass;
      break;
    case PARENT_CONFIG_MERGE_CLASS:
      path = parentMergeClass;
      break;
    case SELF:
    default:
      path = "";
    }
    return provide(path);
  }

  /**
   * Defines what the provider requires as input to provide the output class.
   *
   * @return {@link ProvideFrom}
   */
  public ProvideFrom provideFrom() {
    return this.provideFrom;
  }

  private void validate(String claz) {
    if (claz == null) {
      throw new JavaForgerException("input class path may not be null");
    }
    if (claz.isEmpty()) {
      throw new JavaForgerException("input class path may not be empty");
    }
  }

}
