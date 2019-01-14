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

import templateInput.definition.TypeDefinition;

/**
 * Class for frequently used string conversions of the name of {@link TypeDefinition}, like uppercasing the first char, or creating a getter.
 *
 * @author Daan
 */
public class StringConverter {

  private String string;

  public StringConverter(String string) {
    this.string = string;
  }

  public String getLowerFirst() {
    char[] c = string.toCharArray();
    c[0] = Character.toLowerCase(c[0]);
    return new String(c);
  }

  public String getUpperFirst() {
    char[] c = string.toCharArray();
    c[0] = Character.toUpperCase(c[0]);
    return new String(c);
  }

  public String getSnakeCase() {
    String regex = "([A-Z])";
    String replacement = "_$1";
    return string.replaceAll(regex, replacement).toUpperCase();
  }

}
