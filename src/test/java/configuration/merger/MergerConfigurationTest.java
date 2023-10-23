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
package configuration.merger;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import configuration.ClassProvider;
import configuration.MergeLevel;
import merger.MergeType;

/**
 * Unit test for {@link MergerConfiguration}.
 *
 * @author Daan
 */
@RunWith(MockitoJUnitRunner.class)
public class MergerConfigurationTest {
  private static final MergeType MERGE_TYPE = MergeType.DEFAULT_JAVA;
  private static final ClassProvider MERGE_CLASS_PROVIDER = ClassProvider.forMavenUnitTestFromInput();
  private static final boolean MERGE = true;
  private static final MergeLevel MERGE_LEVEL = MergeLevel.FILE;

  @Test
  public void testMergerConfiguration_minimum() {
    MergerConfiguration mergerConfiguration = MergerConfiguration.builder().build();

    Assert.assertEquals("Unexpected mergeType", MergeType.DEFAULT_JAVA, mergerConfiguration.getMergeType());
    Assert.assertNull("Unexpected mergeClassProvider", mergerConfiguration.getMergeClassProvider());
    Assert.assertEquals("Unexpected merge", true, mergerConfiguration.isMerge());
    Assert.assertEquals("Unexpected mergeLevel", MergeLevel.LINE, mergerConfiguration.getMergeLevel());
  }

  @Test
  public void testMergerConfiguration_maximum() {
    MergerConfiguration mergerConfiguration = createAndFillBuilder().build();

    Assert.assertEquals("Unexpected mergeType", MERGE_TYPE, mergerConfiguration.getMergeType());
    Assert.assertEquals("Unexpected mergeClassProvider", MERGE_CLASS_PROVIDER, mergerConfiguration.getMergeClassProvider());
    Assert.assertTrue("Unexpected merge", mergerConfiguration.isMerge());
    Assert.assertEquals("Unexpected mergeLevel", MERGE_LEVEL, mergerConfiguration.getMergeLevel());
  }

  private MergerConfiguration.Builder<?> createAndFillBuilder() {
    return MergerConfiguration.builder().mergeType(MERGE_TYPE).mergeClassProvider(MERGE_CLASS_PROVIDER).merge(MERGE).mergeLevel(MERGE_LEVEL);
  }

}
