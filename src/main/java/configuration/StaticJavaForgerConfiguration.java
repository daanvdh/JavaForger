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
package configuration;

import generator.JavaForger;
import merger.CodeSnipitMerger;
import reader.ClassContainerReader;

/**
 * Contains all static configurations for {@link JavaForger}.
 *
 * @author Daan
 */
public class StaticJavaForgerConfiguration {

  private ClassContainerReader reader = new ClassContainerReader();
  private CodeSnipitMerger merger = new CodeSnipitMerger();

  private static StaticJavaForgerConfiguration config;

  private StaticJavaForgerConfiguration() {
    // don't create it via any constructor
  }

  public static StaticJavaForgerConfiguration getConfig() {
    if (config == null) {
      setupConfig();
    }
    return config;
  }

  public static ClassContainerReader getReader() {
    return getConfig().reader;
  }

  public void setReader(ClassContainerReader classReader) {
    config.reader = classReader;
  }

  public static CodeSnipitMerger getMerger() {
    return getConfig().merger;
  }

  public void setMerger(CodeSnipitMerger merger) {
    config.merger = merger;
  }

  private static synchronized void setupConfig() {
    config = new StaticJavaForgerConfiguration();
  }

}
