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
package templateInput;

import generator.Generator;

/**
 * Enumeration of all possible custom input variable names that can be used inside a template. Any name, which is defined inside this enum, used inside a
 * template as variable will be filled with custom logic depending on other input by the {@link Generator}.
 *
 * @author Daan
 */
public enum TemplateInputDefaults {
  CLASS("class"),
  FIELDS("fields"),
  CONSTRUCTORS("constructors"),
  METHODS("methods"),
  PACKAGE("package"),
  MERGE_CLASS_NAME("mergeClassName");

  private final String name;

  private TemplateInputDefaults(String name) {
    this.name = name;
  }

  public String getName() {
    return this.name;
  }

}
