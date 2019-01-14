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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import freemarker.cache.FileTemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.core.ParseException;
import freemarker.template.Configuration;
import freemarker.template.MalformedTemplateNameException;
import freemarker.template.Template;
import freemarker.template.TemplateNotFoundException;
import generator.Generator;
import parameters.ClassContainerAdjuster;
import parameters.TemplateInputParameters;

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

  /** The {@link MergeClassProvider} to provide the class to merge the generated code with. */
  private MergeClassProvider mergeClassProvider;

  private boolean merge = true;

  /** With this you can define a sequence of templates to be executed. */
  private final List<JavaForgerConfiguration> childConfigs = new ArrayList<>();

  /** With these consumers you can make changes to the input parameters for the template after parsing is done in the {@link Generator} */
  private final List<ClassContainerAdjuster> adjusters = new ArrayList<>();

  private Configuration freeMarkerConfiguration;

  public JavaForgerConfiguration() {
    this.freeMarkerConfiguration = FreeMarkerConfiguration.getDefaultConfig();
  }

  private JavaForgerConfiguration(Builder builder) {
    this();
    this.template = builder.template;
    this.inputParameters = new TemplateInputParameters(builder.inputParameters);
    this.mergeClassProvider = builder.mergeClassProvider;
    this.childConfigs.addAll(builder.childConfigs);
    this.adjusters.addAll(builder.adjusters);
    this.freeMarkerConfiguration = (builder.freeMarkerConfiguration == null) ? this.freeMarkerConfiguration : builder.freeMarkerConfiguration;
  }

  public boolean isMerge() {
    return merge;
  }

  public void setMerge(boolean merge) {
    this.merge = merge;
  }

  public MergeClassProvider getMergeClassProvider() {
    return mergeClassProvider;
  }

  public void setMergeClassProvider(MergeClassProvider mergeClassProvider) {
    this.mergeClassProvider = mergeClassProvider;
  }

  public void setMergeClass(String mergeClass) {
    this.mergeClassProvider = mergeClass == null ? null : new MergeClassProvider(mergeClass);
  }

  public List<JavaForgerConfiguration> getChildConfigs() {
    return childConfigs;
  }

  public void setChildConfigs(List<JavaForgerConfiguration> configs) {
    this.childConfigs.clear();
    this.childConfigs.addAll(configs);
  }

  public Template getTemplate() throws TemplateNotFoundException, MalformedTemplateNameException, ParseException, IOException {
    return freeMarkerConfiguration.getTemplate(template);
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

  public Configuration getFreeMarkerConfiguration() {
    return freeMarkerConfiguration;
  }

  public void setFreeMarkerConfiguration(Configuration freeMarkerConfig) {
    this.freeMarkerConfiguration = freeMarkerConfig;
  }

  public void addTemplateLocation(String templateLocation) throws IOException {
    FileTemplateLoader loader = new FileTemplateLoader(new File(templateLocation));
    TemplateLoader original = this.getFreeMarkerConfiguration().getTemplateLoader();
    MultiTemplateLoader mtl = new MultiTemplateLoader(new TemplateLoader[] {original, loader});
    this.freeMarkerConfiguration.setTemplateLoader(mtl);
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
    private MergeClassProvider mergeClassProvider;
    private List<JavaForgerConfiguration> childConfigs = new ArrayList<>();
    private List<ClassContainerAdjuster> adjusters = new ArrayList<>();
    private Configuration freeMarkerConfiguration = null;

    private Builder() {
    }

    private Builder(JavaForgerConfiguration config) {
      this.template = config.template;
      this.inputParameters = new TemplateInputParameters(config.inputParameters);
      this.mergeClassProvider = config.mergeClassProvider;
      this.childConfigs = config.childConfigs.stream().map(JavaForgerConfiguration::builder).map(Builder::build).collect(Collectors.toList());
      this.adjusters = new ArrayList<>(config.adjusters);
      this.freeMarkerConfiguration = config.freeMarkerConfiguration;
    }

    public Builder withTemplate(String template) {
      this.template = template;
      return this;
    }

    public Builder withInputParameters(TemplateInputParameters inputParameters) {
      this.inputParameters = inputParameters;
      return this;
    }

    public Builder withMergeClass(String mergeClass) {
      this.mergeClassProvider = new MergeClassProvider(mergeClass);
      return this;
    }

    public Builder withChildConfig(JavaForgerConfiguration... configs) {
      this.childConfigs.clear();
      this.childConfigs.addAll(Arrays.asList(configs));
      return this;
    }

    public Builder withFreeMarkerConfiguration(Configuration config) {
      this.freeMarkerConfiguration = config;
      return this;
    }

    public JavaForgerConfiguration build() {
      return new JavaForgerConfiguration(this);
    }

    public Builder withParameterAdjusters(ClassContainerAdjuster... adjusters) {
      this.adjusters.clear();
      this.adjusters.addAll(Arrays.asList(adjusters));
      return this;
    }

    public Builder withMergeClassProvider(MergeClassProvider mergeClassProvider) {
      this.mergeClassProvider = mergeClassProvider;
      return this;
    }

  }

}
