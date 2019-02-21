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
package merger;

import java.io.IOException;
import java.util.LinkedHashMap;

import com.github.javaparser.ast.CompilationUnit;

import configuration.JavaForgerConfiguration;
import generator.CodeSnipit;

/**
 * TODO javadoc
 *
 * @author Daan
 */
public class LineMerger extends CodeSnipitMerger {

  private CodeSnipitLocater locater = new CodeSnipitLocater();
  private CodeSnipitInserter inserter = new CodeSnipitInserter();

  @Override
  protected void executeMerge(JavaForgerConfiguration config, CodeSnipit codeSnipit, String mergeClassPath) throws IOException {
    CompilationUnit existingCode = read(mergeClassPath);
    String completeClass = toCompleteClass(codeSnipit);
    CompilationUnit newCode = readClass(completeClass);

    LinkedHashMap<CodeSnipitLocation, CodeSnipitLocation> newCodeInsertionLocations = locater.locate(existingCode, newCode);
    inserter.insert(mergeClassPath, completeClass, newCodeInsertionLocations);
  }

}
