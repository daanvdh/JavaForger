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
package initialization;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Container class for a value to be assigned to a variable inside a template and the required imports for that value.
 *
 * @author Daan
 */
public class InitValue {

  private final String value;
  private final List<String> imports = new ArrayList<>();

  /**
   * Constructor for {@link InitValue}.
   *
   * @param value The value to be assigned to a variable.
   * @param imports The imports required for that value assignments.
   */
  public InitValue(String value, String... imports) {
    this.value = value;
    this.imports.addAll(Arrays.asList(imports));
  }

  /**
   * Constructor for {@link InitValue}.
   *
   * @param value The value to be assigned to a variable.
   */
  public InitValue(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  public List<String> getImports() {
    return Collections.unmodifiableList(imports);
  }

}
