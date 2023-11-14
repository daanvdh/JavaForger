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
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JarTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

import freemarker.cache.FileTemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import generator.JavaForger;
import initialization.InitializationService;
import reader.ClassContainerReader;
import reader.ClassContainerReaderInterface;

/**
 * Contains all static configurations for {@link JavaForger}.
 *
 * @author Daan
 */
public class StaticJavaForgerConfiguration {
  private static final Logger LOG = LoggerFactory.getLogger(StaticJavaForgerConfiguration.class);

  private ClassContainerReader reader;
  private InitializationService initializer;
  private Configuration freeMarkerConfiguration;

  /** Used to gather more data about a parsed class, such as resolving imports or super classes. */
  private JavaSymbolSolver symbolSolver;

  private List<TemplateLoader> templateLoaders = new ArrayList<>();

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
    }
    return config;
  }

  public static ClassContainerReaderInterface getReader() {
    return getConfig().reader;
    // return getConfig().readerProxy;
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
    conf.setFreeMarkerConfiguration(FreeMarkerConfiguration.getDefaultConfig());
  }

  public Configuration getFreeMarkerConfiguration() {
    return freeMarkerConfiguration;
  }

  public void setFreeMarkerConfiguration(Configuration freeMarkerConfig) {
    this.freeMarkerConfiguration = freeMarkerConfig;
  }

  public void addTemplateLocation(String templateLocation) throws IOException {
    FileTemplateLoader loader = new FileTemplateLoader(new File(templateLocation));
    this.addTemplateLoader(loader);
  }

  public List<TemplateLoader> getTemplateLoaders() {
    return templateLoaders;
  }

  /**
   * Add a custom template loader to look for templates given a template name or path.
   * 
   * @param templateLoader
   */
  public void addTemplateLoader(TemplateLoader templateLoader) {
    this.templateLoaders.add(templateLoader);
    resetTemplateLoaders();
  }

  public void removeTemplateLoader(TemplateLoader templateLoader) {
    boolean removed = this.templateLoaders.remove(templateLoader);
    if (removed) {
      resetTemplateLoaders();
    }
  }

  public final void setSymbolSolver(JavaSymbolSolver symbolSolver) {
    this.symbolSolver = symbolSolver;
    StaticJavaParser.getConfiguration().setSymbolResolver(symbolSolver);
  }

  public JavaSymbolSolver getSymbolSolver() {
    return symbolSolver;
  }

  /**
   * Sets the project paths to be used to find classes related to an input class for {@link JavaForger}. This can be used to find imports, types or other data
   * that can then be used in templates. Note that these paths should be the full path to the source folder, typically ending with ".../src/main/java" for maven
   * projects. This method will override anything set by the method {@link StaticJavaForgerConfiguration#setSymbolSolver(JavaSymbolSolver)}.
   *
   * @param paths The full paths to source folders where JavaForger needs to look for classes that any input class depends on.
   * @throws IOException
   */
  public void setProjectPaths(String... paths) throws IOException {
    Stream.of(paths).filter(p -> !Files.exists(new File(p).toPath())).forEach(p -> LOG.error("Could not find the folder located at: " + p));
    JavaParserTypeSolver[] solvers =
        Stream.of(paths).filter(p -> Files.exists(new File(p).toPath())).map(JavaParserTypeSolver::new).toArray(JavaParserTypeSolver[]::new);
    TypeSolver[] reflTypeSolver =
        {new ReflectionTypeSolver(), new JarTypeSolver("C:\\Users\\daan.vandenheuvel\\.m2\\repository\\org\\slf4j\\slf4j-api\\1.7.32\\slf4j-api-1.7.32.jar")};
    TypeSolver typeSolver = new CombinedTypeSolver(ArrayUtils.addAll(reflTypeSolver, solvers));
    JavaSymbolSolver symbolSolver = new JavaSymbolSolver(typeSolver);
    setSymbolSolver(symbolSolver);
  }

  private final void setupSymbolSolver() {
    TypeSolver reflTypeSolver = new ReflectionTypeSolver();
    JavaSymbolSolver symbolSolver = new JavaSymbolSolver(reflTypeSolver);
    this.setSymbolSolver(symbolSolver);
  }

  private void resetTemplateLoaders() {
    MultiTemplateLoader mtl = new MultiTemplateLoader(this.templateLoaders.toArray(TemplateLoader[]::new));
    this.freeMarkerConfiguration.setTemplateLoader(mtl);
  }

}
