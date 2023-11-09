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
package configuration;

/**
 * Class indicating at what level new code will be inserted into existing code. Each subsequent merge level is will merge code on a more fine-grained level. If
 * for instance this is set to method level in the {@link JavaForgerConfiguration}, then if a method already exists, it is either overridden as a whole or not
 * inserted at all (depending on the override setting in the {@link JavaForgerConfiguration}.
 *
 * @author daan.vandenheuvel
 */
public enum MergeLevel {
  /** Don't merge anything, just override the whole file (if override in the {@link JavaForgerConfiguration} is set to true) */
  FILE(1),
  // TODO this is not supported yet.
  // /** Merge the class, imports and package independently from each other. */
  // CLASS(2),
  /** Merge considering fields, methods and constructors independently. */
  METHOD(3),
  /** Merge based on individual lines. */
  LINE(4), 
  /** Merge based on individual method calls or variable usages, for example: when inserting Entity.builder().a("a").b("b").c("c").build(); into 
   * Entity.builder().a("a").c("c").d("d").build(); the result should be Entity.builder().a("a").b("b").c("c").d("d").build(); */
  SUB_LINE(5);

  private int level;

  MergeLevel(int level) {
    this.level = level;
  }

  /**
   * @param mergeLevel The {@link MergeLevel} to compare.
   * @return true if this {@link MergeLevel} is as fine grained or more fine grained compared to the input {@link MergeLevel}.
   */
  public boolean isAsFineGrainedAs(MergeLevel mergeLevel) {
    return this.level <= mergeLevel.getLevel();
  }

  public boolean isLessFineGrainedComparedTo(MergeLevel mergeLevel) {
    return this.level >= mergeLevel.getLevel();
  }

  private int getLevel() {
    return this.level;
  }

}
