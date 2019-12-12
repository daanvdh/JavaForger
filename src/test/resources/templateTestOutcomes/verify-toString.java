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
package inputClassesForTests;

import java.util.Set;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * Input class for unit tests.
 *
 * @author Daan
 */
public class ClassWithEverything {

  public static final Boolean PUB_STAT_FIN = false;

  public Set<Product> prod;
  public int i = 0;
  protected ClassWithEverything c;
  private String s;

  public ClassWithEverything(int j) {
    this();
    i = j;
  }

  private ClassWithEverything() {
    // Do nothing
  }

  public void method1() {
    method2(i, s);
  }
  @Override
  public String toString() {
    return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
      .append("prod", prod)
      .append("i", i)
      .append("c", c)
      .append("s", s)
      .build();
  }

  private int method2(int j, String t2) {
    s = t2;
    return j + 1;
  }

  public class InnerClass {
    public double d = 0.3;
    private float f = 4.8F;

    public void method3() {
      method4(d, f);
    }

    private int method4(double e, float g) {
      d = e;
      return Float.floatToIntBits(g);
    }

  }

}
