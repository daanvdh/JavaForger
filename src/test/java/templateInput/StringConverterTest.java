/*
 * Copyright (c) 2020 by Eyefreight BV (www.eyefreight.com). All rights reserved.
 *
 * This software is provided by the copyright holder and contributors "as is" and any express or implied warranties, including, but
 * not limited to, the implied warranties of merchantability and fitness for a particular purpose are disclaimed. In no event shall
 * Eyefreight BV or contributors be liable for any direct, indirect, incidental, special, exemplary, or consequential damages
 * (including, but not limited to, procurement of substitute goods or services; * loss of use, data, or profits; or business
 * interruption) however caused and on any theory of liability, whether in contract, strict liability, or tort (including
 * negligence or otherwise) arising in any way out of the use of this software, even if advised of the possibility of such damage.
 */
package templateInput;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Unit test for {@link StringConverter}.
 *
 * @author Daan
 */
@RunWith(MockitoJUnitRunner.class)
public class StringConverterTest {

  private static final String STRING = "a";

  @Test
  public void testGetLowerDash() {
    Assert.assertEquals("ab-cd", new StringConverter("AbCd").getLowerDash());
  }

  @Test
  public void testGetSnakeCase_upperFirst() {
    Assert.assertEquals("SOME_CLASS", new StringConverter("SomeClass").getSnakeCase());
  }

  @Test
  public void testStringConverter_minimum() {
    StringConverter stringConverter = StringConverter.builder().build();

    Assert.assertNull("Unexpected string", stringConverter.getString());
  }

  @Test
  public void testStringConverter_maximum() {
    StringConverter stringConverter = createAndFillBuilder().build();

    Assert.assertEquals("Unexpected string", STRING, stringConverter.getString());
  }

  private StringConverter.Builder<?> createAndFillBuilder() {
    return StringConverter.builder().string(STRING);
  }

}
