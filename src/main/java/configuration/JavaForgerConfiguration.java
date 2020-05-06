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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import generator.Generator;
import templateInput.TemplateInputParameters;

/**
 * DTO containing the configuration for the execution of a template.
 *
 * @author Daan
 */
public class JavaForgerConfiguration {

  /** The template to be prosessed. */
  private String template;

  /** The input parameters to be used for the template. This is aditional to the parameters that will be added from the input class. */
  private TemplateInputParameters inputParameters;

  /** The {@link ClassProvider} to provide the input class for the template. */
  private ClassProvider inputClassProvider = new ClassProvider();

  /** The {@link ClassProvider} to provide the class to merge the generated code with. */
  private ClassProvider mergeClassProvider;

  /** Determines if the generated code should be merged with the class given by the mergeClassProvider. */
  private boolean merge = true;

  /**
   * The {@link JavaForgerConfiguration} that will be executed after this configuration is executed. The {@link JavaForgerConfiguration#inputClassProvider} can
   * be used to determine the input class. Typically childConfigurations will need the merge class of the parent as input, this can be done by using
   * {@link ClassProvider#fromParentMergeClass} as {@link JavaForgerConfiguration#inputClassProvider}.
   */
  private final List<JavaForgerConfiguration> childConfigs = new ArrayList<>();

  /** With these consumers you can make changes to the input parameters for the template after parsing is done in the {@link Generator} */
  private final List<ClassContainerAdjuster> adjusters = new ArrayList<>();

  /** If true the merge class provided by the {@link ClassProvider} will be created if it does not exists. */
  private boolean createFileIfNotExists;

  /**
   * If the merge class provided by the {@link ClassProvider} does not exist and createFileIfNotExists is true, the new file will be filled with the processed
   * template from the configIfFileDoesNotExist {@link JavaForgerConfiguration}. If this is null, the new class will be filled with the processed template only.
   */
  private JavaForgerConfiguration configIfFileDoesNotExist;

  /**
   * Determines if existing constructors, methods or fields should be overridden with generated equivalents. Classes are never overridden independent from this
   * setting, because we recursively check what's inside.
   */
  private boolean override = false;

  public JavaForgerConfiguration() {
    // Make Constructor visible
  }

  private JavaForgerConfiguration(Builder builder) {
    this();
    this.template = builder.template;
    this.inputParameters = new TemplateInputParameters(builder.inputParameters);
    this.mergeClassProvider = builder.mergeClassProvider;
    this.inputClassProvider = (builder.inputClassProvider == null) ? this.inputClassProvider : builder.inputClassProvider;
    this.childConfigs.addAll(builder.childConfigs);
    this.adjusters.addAll(builder.adjusters);
    this.createFileIfNotExists = builder.createFileIfNotExists;
    this.configIfFileDoesNotExist = builder.configIfFileDoesNotExist;
    this.override = (builder.override == null) ? this.override : builder.override;
  }

  public boolean isMerge() {
    return merge;
  }

  public ClassProvider getInputClassProvider() {
    return inputClassProvider;
  }

  public void setInputClassProvider(ClassProvider inputClassProvider) {
    this.inputClassProvider = inputClassProvider;
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

  /**
   * @return {@link JavaForgerConfiguration#childConfigs}
   */
  public List<JavaForgerConfiguration> getChildConfigs() {
    return childConfigs;
  }

  public void setChildConfigs(List<JavaForgerConfiguration> configs) {
    this.childConfigs.clear();
    this.childConfigs.addAll(configs);
  }

  public void addChildConfig(JavaForgerConfiguration config) {
    this.childConfigs.add(config);
  }

  public void addChildConfigs(JavaForgerConfiguration... children) {
    this.childConfigs.addAll(Arrays.asList(children));
  }

  public String getTemplate() {
    return template;
  }

  public String getTemplateName() {
    return template;
  }

  public void setTemplate(String template) {
    this.template = template;
  }

  public TemplateInputParameters getInputParameters() {
    return inputParameters.copy();
  }

  public void setInputParameters(TemplateInputParameters inputParameters) {
    this.inputParameters = inputParameters;
  }

  public void addInputParameter(String name, Object value) {
    this.inputParameters.put(name, value);
  }

  public ClassContainerAdjuster getAdjuster() {
    return parameters -> adjusters.stream().forEach(adj -> adj.accept(parameters));
  }

  public void addParameterAdjusters(ClassContainerAdjuster... adjusters) {
    this.adjusters.addAll(Arrays.asList(adjusters));
  }

  public void setParameterAdjusters(ClassContainerAdjuster... adjusters) {
    this.adjusters.clear();
    this.adjusters.addAll(Arrays.asList(adjusters));
  }

  public boolean isCreateFileIfNotExists() {
    return createFileIfNotExists;
  }

  public void setCreateFileIfNotExists(boolean createFileIfNotExists) {
    this.createFileIfNotExists = createFileIfNotExists;
  }

  public JavaForgerConfiguration getConfigIfFileDoesNotExist() {
    return configIfFileDoesNotExist;
  }

  public void setConfigIfFileDoesNotExist(JavaForgerConfiguration configIfFileDoesNotExist) {
    setCreateFileIfNotExists(true);
    this.configIfFileDoesNotExist = configIfFileDoesNotExist;
  }

  /**
   * @see JavaForgerConfiguration#override
   * @param override
   */
  public void setOverride(boolean override) {
    this.override = override;
  }

  /**
   * @see JavaForgerConfiguration#override
   */
  public boolean isOverride() {
    return override;
  }

  /**
   * Execute the given consumer on this {@link JavaForgerConfiguration} and all child configurations.
   *
   * @param consumer The consumer to be executed.
   */
  public void setRecursive(Consumer<JavaForgerConfiguration> consumer) {
    consumer.accept(this);
    this.childConfigs.stream().forEach(config -> config.setRecursive(consumer::accept));
  }

  /**
   * Insert a setter to be executed on this {@link JavaForgerConfiguration} and all child configurations.
   *
   * @param setter The setter to be executed.
   * @param value The value to put as parameter in the setter
   */
  public <T> void setRecursive(BiConsumer<JavaForgerConfiguration, T> setter, T value) {
    setter.accept(this, value);
    this.childConfigs.stream().forEach(config -> config.setRecursive(setter, value));
  }

  @Override
  public String toString() {
    return "config: " + template;
  }

  /**
   * Creates builder to build {@link JavaForgerConfiguration}.
   *
   * @return created builder
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Creates builder that is filled with the input {@link JavaForgerConfiguration} to build {@link JavaForgerConfiguration}.
   *
   * @param config The config to copy
   * @return created builder
   */
  public static Builder builder(JavaForgerConfiguration config) {
    return new Builder(config);
  }

  /**
   * Builder to build {@link JavaForgerConfiguration}.
   */
  public static final class Builder {
    private String template;
    private TemplateInputParameters inputParameters = new TemplateInputParameters();
    private ClassProvider mergeClassProvider;
    private ClassProvider inputClassProvider;
    private List<JavaForgerConfiguration> childConfigs = new ArrayList<>();
    private List<ClassContainerAdjuster> adjusters = new ArrayList<>();
    private boolean createFileIfNotExists;
    private JavaForgerConfiguration configIfFileDoesNotExist;
    private Boolean override;

    private Builder() {
      // Make constructor visible
    }

    private Builder(JavaForgerConfiguration config) {
      this.template = config.template;
      this.inputParameters = new TemplateInputParameters(config.inputParameters);
      this.mergeClassProvider = config.mergeClassProvider;
      this.childConfigs = config.childConfigs.stream().map(JavaForgerConfiguration::builder).map(Builder::build).collect(Collectors.toList());
      this.adjusters = new ArrayList<>(config.adjusters);
    }

    public Builder template(String template) {
      this.template = template;
      return this;
    }

    public Builder inputParameters(TemplateInputParameters inputParameters) {
      this.inputParameters = inputParameters;
      return this;
    }

    public Builder mergeClass(String mergeClass) {
      this.mergeClassProvider = new ClassProvider(mergeClass);
      return this;
    }

    /**
     * @param configs {@link JavaForgerConfiguration#childConfigs}
     */
    public Builder childConfig(JavaForgerConfiguration... configs) {
      this.childConfigs.clear();
      this.childConfigs.addAll(Arrays.asList(configs));
      return this;
    }

    public Builder parameterAdjusters(ClassContainerAdjuster... adjusters) {
      this.adjusters.clear();
      this.adjusters.addAll(Arrays.asList(adjusters));
      return this;
    }

    public Builder mergeClassProvider(ClassProvider mergeClassProvider) {
      this.mergeClassProvider = mergeClassProvider;
      return this;
    }

    public Builder createFileIfNotExists(boolean createFileIfNotExists) {
      this.createFileIfNotExists = createFileIfNotExists;
      return this;
    }

    /**
     * @param classProvider {@link JavaForgerConfiguration#inputClassProvider}
     */
    public Builder inputClassProvider(ClassProvider classProvider) {
      this.inputClassProvider = classProvider;
      return this;
    }

    public Builder configIfFileDoesNotExist(JavaForgerConfiguration configIfFileDoesNotExist) {
      this.createFileIfNotExists = true;
      this.configIfFileDoesNotExist = configIfFileDoesNotExist;
      return this;
    }

    public Builder override(boolean override) {
      this.override = override;
      return this;
    }

    public JavaForgerConfiguration build() {
      return new JavaForgerConfiguration(this);
    }

  }

}
