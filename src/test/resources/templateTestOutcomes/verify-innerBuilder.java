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
  private ClassWithEverything(Builder builder) {
    this.prod = builder.prod == null ? this.prod : builder.prod;
    this.i = builder.i == null ? this.i : builder.i;
    this.c = builder.c == null ? this.c : builder.c;
    this.s = builder.s == null ? this.s : builder.s;
  }

  public void method1() {
    method2(i, s);
  }

  /**
   * Creates builder to build {@link ClassWithEverything}.
   * @return created builder
   */
  public static Builder builder() {
    return new Builder();
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
  /**
   * Builder to build {@link ClassWithEverything}.
   */
  public static final class Builder {
    private Set<Product> prod = new HashSet<>();
    private Integer i;
    private ClassWithEverything c;
    private String s;

    private Builder() {
      // Builder should only be constructed via the parent class
    }

    public Builder prod(Set<Product> prod) {
      this.prod.clear();
      this.prod.addAll(prod);
      return this;
    }
    
    public Builder i(Integer i) {
      this.i = i;
      return this;
    }
    
    public Builder c(ClassWithEverything c) {
      this.c = c;
      return this;
    }
    
    public Builder s(String s) {
      this.s = s;
      return this;
    }
    

    public ClassWithEverything build() {
      return new ClassWithEverything(this);
    }
  }

}
