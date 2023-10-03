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
package configuration.merger;

import configuration.ClassProvider;
import configuration.MergeLevel;
import merger.CodeSnipitMerger;
import merger.MergeType;

/**
 * Holds the configuration for {@link CodeSnipitMerger}, to determine how generated code should be merged into existing code.
 *
 * @author daan.vandenheuvel
 */
public class MergerConfiguration {

  private MergeType mergeType = MergeType.DEFAULT_JAVA;

  /** The {@link ClassProvider} to provide the class to merge the generated code with. */
  private ClassProvider mergeClassProvider;

  /** Determines if the generated code should be merged with the class given by the mergeClassProvider. */
  private boolean merge = true;

  /** Determines how fine grained the merging will be done. */
  private MergeLevel mergeLevel = MergeLevel.LINE;

  public MergeType getMergeType() {
    return this.mergeType;
  }

  public void setMergeType(MergeType mergeType) {
    this.mergeType = mergeType;
  }

  public boolean isMerge() {
    return merge;
  }

  public void setMerge(boolean merge) {
    this.merge = merge;
  }

  public ClassProvider getMergeClassProvider() {
    return mergeClassProvider;
  }

  public void setMergeClassProvider(ClassProvider mergeClassProvider) {
    this.mergeClassProvider = mergeClassProvider;
  }

  public void setMergeClass(String mergeClass) {
    this.mergeClassProvider = mergeClass == null ? null : new ClassProvider(mergeClass);
  }

  public MergeLevel getMergeLevel() {
    return this.mergeLevel;
  }

  public void setMergeLevel(MergeLevel mergeLevel) {
    this.mergeLevel = mergeLevel;
  }

}
