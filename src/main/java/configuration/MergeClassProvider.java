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
import merger.CodeSnipitMerger;

/**
 * This class provides a class name to merge with for the {@link CodeSnipitMerger}.
 *
 * @author Daan
 */
public class MergeClassProvider {

  /**
   * Determines what the {@link Generator} should provide as input to the {@link MergeClassProvider} to determine the path to the merge class.
   */
  public enum ProvideFrom {
    /** Indicates the provider does not require any input because the merge class is already defined. */
    SELF,
    /** The merge class of the parent configuration is required as input. */
    PARENT_CONFIG_MERGE_CLASS,
    /** The input class for the {@link Generator} is required as input. */
    INPUT_CLASS
  }

  private final ProvideFrom provideFrom;
  private final Function<String, String> provide;

  /**
   * Provider to merge with the same class as the input class.
   */
  public MergeClassProvider() {
    this.provideFrom = ProvideFrom.INPUT_CLASS;
    this.provide = s -> {
      validate(s);
      return s;
    };
  }

  /**
   * Provider to merge with the class defined by the input path.
   *
   * @param path The path to the class to merge with.
   */
  public MergeClassProvider(String path) {
    this.provideFrom = ProvideFrom.SELF;
    this.provide = s -> path;
  }

  /**
   * Provider to create a custom merge class after receiving a path from the given {@link ProvideFrom}.
   *
   * @param provideFrom The {@link ProvideFrom} defining where the input path should be taken from.
   * @param provide The provider for determining the merge path after receiving the input path.
   */
  public MergeClassProvider(ProvideFrom provideFrom, Function<String, String> provide) {
    this.provideFrom = provideFrom;
    this.provide = provide;
  }

  /**
   * Calculates the path of the maven unit test by replacing 'main' with 'test' and replacing '.java' with 'Test.java'.
   *
   * @return The path to the maven unit test.
   */
  public static MergeClassProvider forMavenUnitTestFromInput() {
    return new MergeClassProvider(ProvideFrom.INPUT_CLASS, s -> PathConverter.toMavenUnitTestPath(s));
  }

  /**
   * Calculates the path of the maven unit test by replacing 'main' with 'test' and replacing '.java' with 'Test.java'.
   *
   * @return The path to the maven unit test.
   */
  public static MergeClassProvider forMavenUnitTestFromParent() {
    return new MergeClassProvider(ProvideFrom.PARENT_CONFIG_MERGE_CLASS, s -> PathConverter.toMavenUnitTestPath(s));
  }

  /**
   * Calculates the path to the class to merge with given the input path.
   *
   * @param path The input path.
   * @return Path to the class to merge with.
   */
  public String provide(String path) {
    return provide.apply(path);
  }

  /**
   * Defines what the provider requires as input to provide the class to merge with.
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
