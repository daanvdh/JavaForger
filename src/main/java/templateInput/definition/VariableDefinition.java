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
package templateInput.definition;

/**
 * This represents the definition of a variable inside a java class.
 *
 * @author Daan
 */
public class VariableDefinition extends InitializedTypeDefinition {

  public VariableDefinition() {
    // explicitly make constructor visible
  }

  /**
   * Copy constructor
   *
   * @param var
   */
  public VariableDefinition(VariableDefinition var) {
    super(var);
  }

  private VariableDefinition(Builder builder) {
    super(builder);
  }

  /**
   * Creates builder to build {@link VariableDefinition}.
   *
   * @return created builder
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Builder to build {@link VariableDefinition}.
   */
  public static final class Builder extends InitializedTypeDefinition.Builder<VariableDefinition.Builder> {

    private Builder() {
      super();
    }

    public VariableDefinition build() {
      return new VariableDefinition(this);
    }
  }

}
