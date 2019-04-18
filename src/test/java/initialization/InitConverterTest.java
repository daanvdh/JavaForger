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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit test for {@link InitConverter}
 *
 * @author Daan
 */
public class InitConverterTest {

  public InitConverter converter = new InitConverter();

  @Before
  public void setup() {
    converter.reset();
  }

  @Test
  public void testConvert() {
    Assert.assertEquals("integer = 1", converter.convert("integer = %d"));
  }

  @Test
  public void testConvert_() {
    Assert.assertEquals("integer = 1 and 2", converter.convert("integer = %d and %d"));
  }

  @Test
  public void testConvert_String() {
    Assert.assertEquals("string = abc", converter.convert("string = %s%s%s"));
  }

  @Test
  public void testConvert_2Strings() {
    Assert.assertEquals("string = abc", converter.convert("string = %s%s%s"));
    Assert.assertEquals("string = d", converter.convert("string = %s"));
  }
}
