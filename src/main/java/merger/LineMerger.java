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
import generator.CodeSnippet;

/**
 * Class to merge new code into an existing class, on line per line basis.
 *
 * @author Daan
 */
public class LineMerger extends CodeSnippetMerger {

  private CodeSnippetLocater locater = new CodeSnippetLocater();
  private CodeSnipitInserter inserter = new CodeSnipitInserter();
  private CodeSnippetReader reader = new CodeSnippetReader();

  @Override
  public boolean supports(JavaForgerConfiguration config) {
    return config.getMergerConfiguration().getMergeType() == MergeType.DEFAULT_JAVA;
  }

  @Override
  protected void executeMerge(JavaForgerConfiguration config, CodeSnippet codeSnipit, String mergeClassPath, String inputFilePath) throws IOException {
    CompilationUnit existingCode = reader.read(mergeClassPath);
    CompilationUnit newCode = reader.read(codeSnipit, mergeClassPath);
    LinkedHashMap<CodeSnippetLocation, CodeSnippetLocation> newCodeInsertionLocations = locater.locate(existingCode, newCode, config);
    inserter.insert(config, mergeClassPath, codeSnipit.toString(), newCodeInsertionLocations);
  }

}
