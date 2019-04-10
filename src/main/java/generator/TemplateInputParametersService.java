/*
 * Copyright 2019 by Daan van den Heuvel.
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

import configuration.ClassProvider;
import configuration.JavaForgerConfiguration;
import configuration.PathConverter;
import configuration.StaticJavaForgerConfiguration;
import initialization.InitializationService;
import reader.ClassContainerReader;
import templateInput.ClassContainer;
import templateInput.TemplateInputDefaults;
import templateInput.TemplateInputParameters;

/**
 * Service for constructing the {@link TemplateInputParameters} required as input for a template.
 *
 * @author Daan
 */
public class TemplateInputParametersService {

  private ClassContainerReader reader = StaticJavaForgerConfiguration.getReader();
  private InitializationService initializer = StaticJavaForgerConfiguration.getInitializer();

  /**
   * Gets the {@link TemplateInputParameters} from the {@link JavaForgerConfiguration} and inserts all missing input parameters given by
   * {@link TemplateInputDefaults}.
   *
   * @param config The {@link JavaForgerConfiguration}
   * @param inputClass The class that will be parsed to construct the input parameters.
   * @param mergeClassPath The class to which the template from the config will be merged to.
   * @return {@link TemplateInputParameters}
   * @throws IOException
   */
  public TemplateInputParameters getInputParameters(JavaForgerConfiguration config, String inputClass, String mergeClassPath) throws IOException {
    TemplateInputParameters inputParameters = config.getInputParameters();

    if (inputClass != null && !inputClass.isEmpty()) {
      if (!inputParameters.containsKey(TemplateInputDefaults.FIELDS.getName()) || !inputParameters.containsKey(TemplateInputDefaults.CLASS.getName())
          || !inputParameters.containsKey(TemplateInputDefaults.METHODS.getName())
          || !inputParameters.containsKey(TemplateInputDefaults.CONSTRUCTORS.getName())) {

        String newInputClass = getInputClass(config, inputClass, mergeClassPath);

        ClassContainer claz = reader.read(newInputClass);
        initializer.init(claz);
        config.getAdjuster().accept(claz);
        if (!inputParameters.containsKey(TemplateInputDefaults.FIELDS.getName())) {
          inputParameters.put(TemplateInputDefaults.FIELDS.getName(), claz.getFields());
        }
        if (!inputParameters.containsKey(TemplateInputDefaults.CLASS.getName())) {
          inputParameters.put(TemplateInputDefaults.CLASS.getName(), claz);
        }
        if (!inputParameters.containsKey(TemplateInputDefaults.METHODS.getName())) {
          inputParameters.put(TemplateInputDefaults.METHODS.getName(), claz.getMethods());
        }
        if (!inputParameters.containsKey(TemplateInputDefaults.CONSTRUCTORS.getName())) {
          inputParameters.put(TemplateInputDefaults.CONSTRUCTORS.getName(), claz.getConstructors());
        }
      }
    }
    if (mergeClassPath != null) {
      if (!inputParameters.containsKey(TemplateInputDefaults.PACKAGE.getName())) {
        String pack = PathConverter.toPackage(mergeClassPath);
        inputParameters.put(TemplateInputDefaults.PACKAGE.getName(), pack);
      }
      if (!inputParameters.containsKey(TemplateInputDefaults.MERGE_CLASS_NAME.getName())) {
        String a = mergeClassPath.replace("\\", "/");
        String name = a.substring(a.lastIndexOf("/") + 1, a.lastIndexOf("."));
        inputParameters.put(TemplateInputDefaults.MERGE_CLASS_NAME.getName(), name);
      }
    }

    return inputParameters;
  }

  private String getInputClass(JavaForgerConfiguration config, String inputClass, String mergeClassPath) {
    String input = null;
    ClassProvider provider = config.getInputClassProvider();
    switch (provider.provideFrom()) {
    case PARENT_CONFIG_MERGE_CLASS:
      input = provider.provide(mergeClassPath);
      break;
    case INPUT_CLASS:
    case SELF:
    default:
      input = provider.provide(inputClass);
      break;
    }
    return input;
  }

}
