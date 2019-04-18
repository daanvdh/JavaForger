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

/**
 * Contains static methods for converting paths.
 *
 * @author Daan
 */
public class PathConverter {

  public static String toMavenUnitTestPath(String s) {
    return s.replace("\\", "/").replace("/main/", "/test/").replace(".java", "Test.java");
  }

  public static String toPackage(String mavenPath) {
    String clean = mavenPath.replace("\\", "/");
    String noClass = clean.substring(0, clean.lastIndexOf("/"));
    String sourceFolder = "/src/main/java/";
    String testFolder = "/src/test/java/";
    String folder = noClass.contains(sourceFolder) ? sourceFolder : testFolder;
    String pack = noClass.substring(noClass.indexOf(folder) + folder.length());
    return pack.replace("/", ".");
  }

  /**
   * Converts a complete path to only the name of the class.
   *
   * @param path The path containing the class name
   * @return The name of the class
   */
  public static String toClassName(String path) {
    String safePath = path.replace("\\", "/");
    String className = safePath.substring(safePath.lastIndexOf("/") + 1, safePath.indexOf(".java"));
    return className;
  }

}
