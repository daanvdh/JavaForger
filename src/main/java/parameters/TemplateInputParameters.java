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
package parameters;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import templateInput.ClassDefinition;
import templateInput.MethodDefinition;
import templateInput.VariableDefinition;

/**
 * Class containing the input parameters for a template.
 *
 * @author Daan
 */
@SuppressWarnings("unchecked")
public class TemplateInputParameters extends HashMap<String, Object> {
  private static final long serialVersionUID = 7129701603512062051L;

  public TemplateInputParameters(Map<String, Object> collect) {
    super(collect);
  }

  public TemplateInputParameters() {
    // empty constructor
  }

  public List<VariableDefinition> getFields() {
    return (List<VariableDefinition>) get(TemplateInputDefaults.CLASS_FIELDS.getName());
  }

  public ClassDefinition getClassDefinition() {
    return (ClassDefinition) get(TemplateInputDefaults.CLASS.getName());
  }

  public List<MethodDefinition> getMethods() {
    return (List<MethodDefinition>) get(TemplateInputDefaults.METHODS.getName());
  }

  public TemplateInputParameters copy() {
    return new TemplateInputParameters(this);
  }

}
