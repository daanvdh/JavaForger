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

/**
 * TODO javadoc
 *
 * @author daan.vandenheuvel
 */
public class GitMergerConfiguration extends MergerConfiguration {

  private String inputGitRepository;
  private String templateGitRepository;

  public GitMergerConfiguration() {
    // empty constructor which would otherwise be invisible due to the constructor receiving the builder.
  }

  protected GitMergerConfiguration(GitMergerConfiguration.Builder<?> builder) {
    super(builder);
    this.inputGitRepository = builder.inputGitRepository == null ? this.inputGitRepository : builder.inputGitRepository;
    this.templateGitRepository = builder.templateGitRepository == null ? this.templateGitRepository : builder.templateGitRepository;
  }

  public String getTemplateGitRepository() {
    return templateGitRepository;
  }

  public void setTemplateGitRepository(String templateGitRepository) {
    this.templateGitRepository = templateGitRepository;
  }

  public String getInputGitRepository() {
    return inputGitRepository;
  }

  public void setInputGitRepository(String inputGitRepository) {
    this.inputGitRepository = inputGitRepository;
  }

  /**
   * Creates builder to build {@link GitMergerConfiguration}.
   *
   * @return created builder
   */
  public static Builder<?> builder() {
    return new Builder<>();
  }

  /**
   * Builder to build {@link GitMergerConfiguration}.
   */
  @SuppressWarnings("unchecked")
  public static class Builder<T extends GitMergerConfiguration.Builder<?>> extends MergerConfiguration.Builder<T> {
    private String inputGitRepository;
    private String templateGitRepository;

    protected Builder() {
      // Builder should only be used via the parent class or extending builder
    }

    public T inputGitRepository(String inputGitRepository) {
      this.inputGitRepository = inputGitRepository;
      return (T) this;
    }

    public T templateGitRepository(String templateGitRepository) {
      this.templateGitRepository = templateGitRepository;
      return (T) this;
    }

    @Override
    public GitMergerConfiguration build() {
      return new GitMergerConfiguration(this);
    }
  }

}
