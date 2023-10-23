/*
 * Copyright 2023 by Daan van den Heuvel.
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
package configuration.merger;

import configuration.ClassProvider;
import configuration.MergeLevel;
import merger.CodeSnippetMerger;
import merger.MergeType;

/**
 * Holds the configuration for {@link CodeSnippetMerger}, to determine how generated code should be merged into existing code.
 *
 * @author daan.vandenheuvel
 */
public class MergerConfiguration {

  private MergeType mergeType = MergeType.DEFAULT_JAVA;

  /** The {@link ClassProvider} to provide the class to merge the generated code with. */
  private ClassProvider mergeClassProvider;

  /** Determines if the generated code should be merged with the class given by the mergeClassProvider. */
  private boolean merge = true;

  /** Determines how fine grained the merging will be done. */
  private MergeLevel mergeLevel = MergeLevel.LINE;

  public MergerConfiguration() {
    // empty constructor which would otherwise be invisible due to the constructor receiving the builder.
  }

  protected MergerConfiguration(MergerConfiguration.Builder<?> builder) {
    this.mergeType = builder.mergeType == null ? this.mergeType : builder.mergeType;
    this.mergeClassProvider = builder.mergeClassProvider == null ? this.mergeClassProvider : builder.mergeClassProvider;
    this.merge = builder.merge == null ? this.merge : builder.merge;
    this.mergeLevel = builder.mergeLevel == null ? this.mergeLevel : builder.mergeLevel;
  }

  public MergeType getMergeType() {
    return this.mergeType;
  }

  public void setMergeType(MergeType mergeType) {
    this.mergeType = mergeType;
  }

  public boolean isMerge() {
    return merge;
  }

  public void setMerge(boolean merge) {
    this.merge = merge;
  }

  public ClassProvider getMergeClassProvider() {
    return mergeClassProvider;
  }

  public void setMergeClassProvider(ClassProvider mergeClassProvider) {
    this.mergeClassProvider = mergeClassProvider;
  }

  public void setMergeClass(String mergeClass) {
    this.mergeClassProvider = mergeClass == null ? null : new ClassProvider(mergeClass);
  }

  public MergeLevel getMergeLevel() {
    return this.mergeLevel;
  }

  public void setMergeLevel(MergeLevel mergeLevel) {
    this.mergeLevel = mergeLevel;
  }

  /**
   * Creates builder to build {@link MergerConfiguration}.
   *
   * @return created builder
   */
  public static Builder<?> builder() {
    return new Builder<>();
  }

  /**
   * Builder to build {@link MergerConfiguration}.
   */
  @SuppressWarnings("unchecked")
  public static class Builder<T extends MergerConfiguration.Builder<?>> {
    private MergeType mergeType;
    private ClassProvider mergeClassProvider;
    private Boolean merge;
    private MergeLevel mergeLevel;

    protected Builder() {
      // Builder should only be used via the parent class or extending builder
    }

    public T mergeType(MergeType mergeType) {
      this.mergeType = mergeType;
      return (T) this;
    }

    public T mergeClassProvider(ClassProvider mergeClassProvider) {
      this.mergeClassProvider = mergeClassProvider;
      return (T) this;
    }

    public T merge(Boolean merge) {
      this.merge = merge;
      return (T) this;
    }

    public T mergeLevel(MergeLevel mergeLevel) {
      this.mergeLevel = mergeLevel;
      return (T) this;
    }

    public <A extends MergerConfiguration> MergerConfiguration build() {
      return new MergerConfiguration(this);
    }
  }

}
