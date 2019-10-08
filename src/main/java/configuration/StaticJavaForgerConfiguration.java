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
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

import freemarker.cache.FileTemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import generator.JavaForger;
import initialization.InitializationService;
import merger.CodeSnipitMerger;
import merger.LineMerger;
import reader.ClassContainerReader;

/**
 * Contains all static configurations for {@link JavaForger}.
 *
 * @author Daan
 */
public class StaticJavaForgerConfiguration {

  private ClassContainerReader reader;
  private InitializationService initializer;
  private CodeSnipitMerger merger;
  private Configuration freeMarkerConfiguration;

  /** Used to gather more data about a parsed class, such as resolving imports or super classes. */
  private JavaSymbolSolver symbolSolver;

  private static StaticJavaForgerConfiguration config;

  private StaticJavaForgerConfiguration() {
    // don't create it via any constructor
    this.freeMarkerConfiguration = FreeMarkerConfiguration.getDefaultConfig();
    setupSymbolSolver();
  }

  public static StaticJavaForgerConfiguration getConfig() {
    if (config == null) {
      config = new StaticJavaForgerConfiguration();
      config.reader = new ClassContainerReader();
      config.initializer = new InitializationService();
      config.merger = new LineMerger();
    }
    return config;
  }

  public static ClassContainerReader getReader() {
    return getConfig().reader;
  }

  public void setReader(ClassContainerReader classReader) {
    getConfig().reader = classReader;
  }

  public static InitializationService getInitializer() {
    return getConfig().initializer;
  }

  public void setInitializer(InitializationService initializer) {
    getConfig().initializer = initializer;
  }

  /**
   * Resets the {@link StaticJavaForgerConfiguration} default values.
   */
  public static void reset() {
    StaticJavaForgerConfiguration conf = StaticJavaForgerConfiguration.getConfig();
    conf.setReader(new ClassContainerReader());
    conf.setMerger(new LineMerger());
    conf.setFreeMarkerConfiguration(FreeMarkerConfiguration.getDefaultConfig());
  }

  public static CodeSnipitMerger getMerger() {
    return getConfig().merger;
  }

  public void setMerger(CodeSnipitMerger merger) {
    getConfig().merger = merger;
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

  public final void setSymbolSolver(JavaSymbolSolver symbolSolver) {
    this.symbolSolver = symbolSolver;
    JavaParser.getStaticConfiguration().setSymbolResolver(symbolSolver);
  }

  public JavaSymbolSolver getSymbolSolver() {
    return symbolSolver;
  }

  private final void setupSymbolSolver() {
    TypeSolver reflTypeSolver = new ReflectionTypeSolver();
    JavaSymbolSolver symbolSolver = new JavaSymbolSolver(reflTypeSolver);
    this.setSymbolSolver(symbolSolver);
  }

}
