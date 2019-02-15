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

import java.io.File;
import java.io.IOException;

import com.github.javaparser.JavaParser;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;

import freemarker.cache.FileTemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
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
  private Configuration freeMarkerConfiguration;

  /** Used to gather more data about a parsed class, such as resolving imports or super classes. */
  private JavaSymbolSolver symbolSolver;

  private static final StaticJavaForgerConfiguration config = new StaticJavaForgerConfiguration();

  private StaticJavaForgerConfiguration() {
    // don't create it via any constructor
    this.freeMarkerConfiguration = FreeMarkerConfiguration.getDefaultConfig();
  }

  public static StaticJavaForgerConfiguration getConfig() {
    return config;
  }

  public static ClassContainerReader getReader() {
    return getConfig().reader;
  }

  /**
   * Resets the {@link StaticJavaForgerConfiguration} default values.
   */
  public static void reset() {
    StaticJavaForgerConfiguration conf = StaticJavaForgerConfiguration.getConfig();
    conf.setReader(new ClassContainerReader());
    conf.setMerger(new CodeSnipitMerger());
    conf.setFreeMarkerConfiguration(FreeMarkerConfiguration.getDefaultConfig());
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

  public Configuration getFreeMarkerConfiguration() {
    return freeMarkerConfiguration;
  }

  public void setFreeMarkerConfiguration(Configuration freeMarkerConfig) {
    this.freeMarkerConfiguration = freeMarkerConfig;
  }

  public void addTemplateLocation(String templateLocation) throws IOException {
    FileTemplateLoader loader = new FileTemplateLoader(new File(templateLocation));
    TemplateLoader original = this.getFreeMarkerConfiguration().getTemplateLoader();
    MultiTemplateLoader mtl = new MultiTemplateLoader(new TemplateLoader[] {original, loader});
    this.freeMarkerConfiguration.setTemplateLoader(mtl);
  }

  public void setSymbolSolver(JavaSymbolSolver symbolSolver) {
    this.symbolSolver = symbolSolver;
    JavaParser.getStaticConfiguration().setSymbolResolver(symbolSolver);
  }

  public JavaSymbolSolver getSymbolSolver() {
    return symbolSolver;
  }

}
