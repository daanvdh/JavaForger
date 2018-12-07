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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.javaparser.ast.Modifier;

import freemarker.core.ParseException;
import freemarker.template.Configuration;
import freemarker.template.MalformedTemplateNameException;
import freemarker.template.Template;
import freemarker.template.TemplateNotFoundException;
import parameters.ParameterAdjuster;
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

  /** The class that the generated code should be merged with. */
  private String mergeClass;

  /** If a field contains a modifier in this set it may be selected. This should not be used, use adjusters instead */
  @Deprecated
  private Set<Modifier> allowedModifiers = new HashSet<>();

  /**
   * If a field contains a modifier which is in this set, that field will not be selected. This overrules the allowed modifiers. This should not be used, use
   * adjusters instead
   */
  @Deprecated
  private Set<Modifier> notAllowedModifiers = new HashSet<>();

  /** With this you can define a sequence of templates to be executed. */
  private List<JavaForgerConfiguration> childConfigs;

  /** With these consumers you can make changes to the input parameters for the template after parsing is done in the {@link Generator} */
  private List<ParameterAdjuster> adjusters;

  private Configuration freeMarkerConfiguration;

  public JavaForgerConfiguration() {
    this.freeMarkerConfiguration = FreeMarkerConfiguration.getDefaultConfig();
  }

  private JavaForgerConfiguration(Builder builder) {
    this();
    this.template = builder.template;
    this.inputParameters = new TemplateInputParameters(builder.inputParameters);
    this.mergeClass = builder.mergeClass;
    this.allowedModifiers = builder.allowedModifiers;
    this.notAllowedModifiers = builder.notAllowedModifiers;
    this.childConfigs = new ArrayList<>(builder.childConfigs);
    this.adjusters = new ArrayList<>(builder.adjusters);
    this.freeMarkerConfiguration = (builder.freeMarkerConfiguration == null) ? this.freeMarkerConfiguration : builder.freeMarkerConfiguration;
  }

  public String getMergeClass() {
    return mergeClass;
  }

  public void setMergeClass(String mergeClass) {
    this.mergeClass = mergeClass;
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

  @Deprecated
  public boolean modifiersAreAllowed(EnumSet<Modifier> modifiers) {
    Boolean allowed = modifiers.stream().map(m -> this.allowedModifiers.contains(m)).reduce(Boolean::logicalOr).get();
    Boolean notAllowed = modifiers.stream().map(m -> this.notAllowedModifiers.contains(m)).reduce(Boolean::logicalOr).get();
    return allowed && !notAllowed;
  }

  public void addInputParameter(String name, Object value) {
    this.inputParameters.put(name, value);
  }

  public ParameterAdjuster getAdjuster() {
    return parameters -> adjusters.stream().forEach(adj -> adj.accept(parameters));
  }

  public Configuration getFreeMarkerConfiguration() {
    return freeMarkerConfiguration;
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
    private String mergeClass;
    @Deprecated
    private Set<Modifier> notAllowedModifiers = new HashSet<>();
    @Deprecated
    private Set<Modifier> allowedModifiers =
        new HashSet<>(Arrays.asList(Modifier.PUBLIC, Modifier.PROTECTED, Modifier.PRIVATE, Modifier.ABSTRACT, Modifier.STATIC, Modifier.FINAL,
            Modifier.TRANSIENT, Modifier.VOLATILE, Modifier.SYNCHRONIZED, Modifier.NATIVE, Modifier.STRICTFP, Modifier.TRANSITIVE, Modifier.DEFAULT));
    private List<JavaForgerConfiguration> childConfigs = new ArrayList<>();
    private List<ParameterAdjuster> adjusters = new ArrayList<>();
    private Configuration freeMarkerConfiguration = null;

    private Builder() {
    }

    private Builder(JavaForgerConfiguration config) {
      this.template = config.template;
      this.inputParameters = new TemplateInputParameters(config.inputParameters);
      this.mergeClass = config.mergeClass;
      this.allowedModifiers = config.allowedModifiers;
      this.notAllowedModifiers = config.notAllowedModifiers;
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
      this.mergeClass = mergeClass;
      return this;
    }

    @Deprecated
    public Builder withModifiers(Modifier... allowedModifiers) {
      this.allowedModifiers.clear();
      this.allowedModifiers.addAll(Arrays.asList(allowedModifiers));
      return this;
    }

    @Deprecated
    public Builder withoutModifiers(Modifier... allowedModifiers) {
      this.notAllowedModifiers.clear();
      this.notAllowedModifiers.addAll(Arrays.asList(allowedModifiers));
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

    public Builder withParameterAdjusters(ParameterAdjuster... adjusters) {
      this.adjusters.clear();
      this.adjusters.addAll(Arrays.asList(adjusters));
      return this;
    }

  }

}
