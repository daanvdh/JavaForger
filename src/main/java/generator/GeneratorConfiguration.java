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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.javaparser.ast.Modifier;

/**
 * DTO containing the configuration for the execution of a template.
 *
 * @author Daan
 */
public class GeneratorConfiguration {

  /** The template to be prosessed. */
  private String template;

  /** The input parameters to be used for the template. This is aditional to the parameters that will be added from the input class. */
  private Map<String, Object> inputParameters;

  /** The class that the generated code should be merged with. */
  private String mergeClass;

  /** If a field contains a modifier in this set it may be selected. */
  private Set<Modifier> allowedModifiers = new HashSet<>();

  /** If a field contains a modifier which is in this set, that field will not be selected. This overrules the allowed modifiers. */
  private Set<Modifier> notAllowedModifiers = new HashSet<>();

  /** With this you can define a sequence of templates to be executed. */
  private List<GeneratorConfiguration> childConfigs;

  private GeneratorConfiguration(Builder builder) {
    this.template = builder.template;
    this.inputParameters = builder.inputParameters;
    this.mergeClass = builder.mergeClass;
    this.allowedModifiers = builder.allowedModifiers;
    this.notAllowedModifiers = builder.notAllowedModifiers;
    this.childConfigs = builder.configs;
  }

  public String getMergeClass() {
    return mergeClass;
  }

  public void setMergeClass(String mergeClass) {
    this.mergeClass = mergeClass;
  }

  public List<GeneratorConfiguration> getChildConfigs() {
    return childConfigs;
  }

  public void setChildConfigs(List<GeneratorConfiguration> configs) {
    this.childConfigs.clear();
    this.childConfigs.addAll(configs);
  }

  public String getTemplate() {
    return template;
  }

  public void setTemplate(String template) {
    this.template = template;
  }

  public Map<String, Object> getInputParameters() {
    return Collections.unmodifiableMap(inputParameters);
  }

  public void setInputParameters(Map<String, Object> inputParameters) {
    this.inputParameters = inputParameters;
  }

  public boolean modifiersAreAllowed(EnumSet<Modifier> modifiers) {
    Boolean allowed = modifiers.stream().map(m -> this.allowedModifiers.contains(m)).reduce(Boolean::logicalOr).get();
    Boolean notAllowed = modifiers.stream().map(m -> this.notAllowedModifiers.contains(m)).reduce(Boolean::logicalOr).get();
    return allowed && !notAllowed;
  }

  /**
   * Creates builder to build {@link GeneratorConfiguration}.
   *
   * @return created builder
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Builder to build {@link GeneratorConfiguration}.
   */
  public static final class Builder {
    private String template;
    private Map<String, Object> inputParameters = new HashMap<>();
    private String mergeClass;
    private Set<Modifier> notAllowedModifiers = new HashSet<>();
    private Set<Modifier> allowedModifiers =
        new HashSet<>(Arrays.asList(Modifier.PUBLIC, Modifier.PROTECTED, Modifier.PRIVATE, Modifier.ABSTRACT, Modifier.STATIC, Modifier.FINAL,
            Modifier.TRANSIENT, Modifier.VOLATILE, Modifier.SYNCHRONIZED, Modifier.NATIVE, Modifier.STRICTFP, Modifier.TRANSITIVE, Modifier.DEFAULT));
    private List<GeneratorConfiguration> configs = new ArrayList<>();

    private Builder() {
    }

    public Builder withTemplate(String template) {
      this.template = template;
      return this;
    }

    public Builder withInputParameters(Map<String, Object> inputParameters) {
      this.inputParameters = inputParameters;
      return this;
    }

    public Builder withMergeClass(String mergeClass) {
      this.mergeClass = mergeClass;
      return this;
    }

    public Builder withModifiers(Modifier... allowedModifiers) {
      this.allowedModifiers.clear();
      this.allowedModifiers.addAll(Arrays.asList(allowedModifiers));
      return this;
    }

    public Builder withoutModifiers(Modifier... allowedModifiers) {
      this.notAllowedModifiers.clear();
      this.notAllowedModifiers.addAll(Arrays.asList(allowedModifiers));
      return this;
    }

    public Builder withChildConfig(GeneratorConfiguration... configs) {
      this.configs.clear();
      this.configs.addAll(Arrays.asList(configs));
      return this;
    }

    public GeneratorConfiguration build() {
      return new GeneratorConfiguration(this);
    }

  }

  public void addInputParameters(String name, Object value) {
    this.inputParameters.put(name, value);

  }

}
