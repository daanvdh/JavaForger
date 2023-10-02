/*
 * Copyright (c) 2023 by Eyefreight BV (www.eyefreight.com). All rights reserved.
 *
 * This software is provided by the copyright holder and contributors "as is" and any express or implied warranties, including, but
 * not limited to, the implied warranties of merchantability and fitness for a particular purpose are disclaimed. In no event shall
 * Eyefreight BV or contributors be liable for any direct, indirect, incidental, special, exemplary, or consequential damages
 * (including, but not limited to, procurement of substitute goods or services; * loss of use, data, or profits; or business
 * interruption) however caused and on any theory of liability, whether in contract, strict liability, or tort (including
 * negligence or otherwise) arising in any way out of the use of this software, even if advised of the possibility of such damage.
 */
package merger;

/**
 * With this we can set how code will be merged into existing code.
 *
 * @author daan.vandenheuvel
 */
public enum MergeType {
  DEFAULT_JAVA,
  /**
   * Use this if the template was already executed before for the given input class. This will only merge code into the mergeClass that results from new and
   * un-commited changes. Let's consider class A {int a;}. Let the template be generating and inserting the method: String toString() { StringBuilder sb = new
   * StringBuilder; sb.append(a); return sb.toString()}. Then if we added custom logic to it later (for example sb.append("something");), we do not want that to
   * get lost when re-generating this method. Therefore, if we add field b to class A and not yet commit it, we only want "sb.append(b);" to be inserted.
   */
  GIT_JAVA,
  JAVA_PARSER

}
