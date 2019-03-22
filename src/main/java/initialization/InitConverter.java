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
import java.util.List;

import generator.JavaForgerException;

/**
 * Converts strings for the initialization of fields in templates from {@link InitDefaultValues} so that numbers or random strings are filled in.
 *
 * @author Daan
 */
public class InitConverter {

  private InitDefaultValues defaults = new InitDefaultValues();

  private int i = 1;
  private char c = 'a';

  /**
   * Resets the initialization values to the first possible value. This method should be called as a start of any session so that for that session the
   * initialization values are predictable.
   */
  public void reset() {
    i = 1;
    c = 'a';
  }

  public String convert(String s) {
    return String.format(s, getArgsFor(s));
  }

  private Object[] getArgsFor(String s) {
    List<Object> parameters = new ArrayList<>();

    int index = s.indexOf("%");
    String substring = s;

    while (index >= 0) {
      String type = substring.substring(index + 1, index + 2);
      switch (type) {
      case "d":
        parameters.add(new Integer(i++));
        break;
      case "s":
        parameters.add(c);
        c = (char) (c + 1);
        break;
      default:
        throw new JavaForgerException("type " + type + "is not supported");
      }

      substring = substring.substring(index + 2);
      index = substring.indexOf("%");
    }
    return parameters.toArray();
  }

}
