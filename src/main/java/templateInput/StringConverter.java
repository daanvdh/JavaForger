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

import org.apache.commons.lang3.builder.EqualsBuilder;

import templateInput.definition.TypeDefinition;

/**
 * Class for template input, contains frequently used string conversions of the name of {@link TypeDefinition}, like uppercasing the first char, or snake casing
 * the whole string.
 *
 * @author Daan
 */
public class StringConverter {

  private String string;

  public StringConverter(String string) {
    this.string = string;
  }

  protected StringConverter(StringConverter.Builder<?> builder) {
    this.string = builder.string == null ? this.string : builder.string;
  }

  public String getLower() {
    return string.toLowerCase();
  }

  public String getUpper() {
    return string.toUpperCase();
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
    return string.length() <= 1 ? this.getUpperFirst()
        : string.substring(0, 1).toUpperCase() + string.substring(1).replaceAll(regex, replacement).toUpperCase();
  }

  public String getLowerSpace() {
    String regex = "([A-Z])";
    String replacement = " $1";
    return getLowerFirst().replaceAll(regex, replacement).toLowerCase();
  }

  public String getLowerDash() {
    String regex = "([A-Z])";
    String replacement = "-$1";
    return getLowerFirst().replaceAll(regex, replacement).toLowerCase();
  }

  public boolean containsString(CharSequence s) {
    return this.string.contains(s);
  }

  @Override
  public String toString() {
    return string;
  }

  public String getString() {
    return string;
  }

  @Override
  public boolean equals(Object obj) {
    boolean equals = false;
    if (this == obj) {
      equals = true;
    } else if (obj != null && getClass() == obj.getClass()) {
      StringConverter other = (StringConverter) obj;
      equals = new EqualsBuilder().append(string, other.string).isEquals();
    }
    return equals;
  }

  /**
   * Creates builder to build {@link StringConverter}.
   * 
   * @return created builder
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Builder to build {@link StringConverter}.
   */
  @SuppressWarnings("unchecked")
  public static class Builder<T extends StringConverter.Builder<?>> {
    private String string;

    protected Builder() {
      // Builder should only be used via the parent class or extending builder
    }

    public T string(String string) {
      this.string = string;
      return (T) this;
    }

    public <A extends StringConverter> StringConverter build() {
      return new StringConverter(this);
    }
  }

}
