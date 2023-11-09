/*
 * Copyright 2023 by Daan van den Heuvel.
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
package merger.git;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.CorruptObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.javaparser.ast.CompilationUnit;

import configuration.JavaForgerConfiguration;
import configuration.merger.GitFileResolver;
import configuration.merger.GitMergerConfiguration;
import freemarker.template.TemplateException;
import generator.CodeSnippet;
import generator.Generator;
import merger.CodeSnipitInserter;
import merger.CodeSnippetLocater;
import merger.CodeSnippetLocation;
import merger.CodeSnippetMerger;
import merger.CodeSnippetReader;
import merger.MergeType;

/**
 * A {@link CodeSnippetMerger} that uses git to find what was newly generated. This merger has to have access to the git repository for it to work. It relies on
 * the code that would force something to change compared to earlier generated code, is not yet committed. This class will stash everything to create an
 * original generated code (o). Then it will pop the stash and generate a new version of the code (n). Only lines of code that are in n but not in o, will be
 * merged into the original.
 *
 * @author daan.vandenheuvel
 */
public class GitMerger extends CodeSnippetMerger {
  private static final Logger LOG = LoggerFactory.getLogger(GitMerger.class);

  private CodeSnippetLocater locater = new CodeSnippetLocater();
  private CodeSnipitInserter inserter = new CodeSnipitInserter();
  private CodeSnippetReader reader = new CodeSnippetReader();
  private GitFileResolver gitFileResolver = new GitFileResolver();
  private Generator generator = null;

  public GitMerger(Generator generator) {
    this.generator = generator;
  }

  @Override
  public boolean supports(JavaForgerConfiguration config) {
    return config.getMergerConfiguration().getMergeType() == MergeType.GIT_JAVA;
  }

  @Override
  protected void executeMerge(JavaForgerConfiguration config, CodeSnippet codeSnipit, String mergeClassPath, String inputFilePath) throws IOException {
    try {
      CompilationUnit previouslyGeneratedCu = executeTemplateOnGitHeadFileVersion(config, codeSnipit, mergeClassPath, inputFilePath);
      CompilationUnit currentMergeFileCu = reader.read(mergeClassPath);
      CompilationUnit newlyGeneratedCu = reader.read(codeSnipit, mergeClassPath);

      // (x)
      LinkedHashMap<CodeSnippetLocation, CodeSnippetLocation> newCodeInsertionLocationsIntoCurrent = locater.locate(currentMergeFileCu, newlyGeneratedCu, config); 
      // (y)
      LinkedHashMap<CodeSnippetLocation, CodeSnippetLocation> newCodeInsertionsIntoPrevious = locater.locate(previouslyGeneratedCu, newlyGeneratedCu, config); 

      // Now we have both (x) where it should be inserted into the existing file and (y) where it would have been inserted in a file resulting from an unchanged
      // file generated from the input template. With this we can keep only the insertion locations from (x) for which the same new line from (y) is not a
      // replacement. This is because lines are only replaced if they are equal. If they where not equal, this indicates it's a line that would not have been
      // previously generated. Therefore it should be inserted.

      List<CodeSnippetLocation> remainingNewCodeToBeInserted =
          newCodeInsertionsIntoPrevious.entrySet().stream().filter((e) -> e.getValue().getNumberOfLines() == 0).map(Entry::getKey).collect(Collectors.toList());
      LinkedHashMap<CodeSnippetLocation, CodeSnippetLocation> insertLocations =
          newCodeInsertionLocationsIntoCurrent.entrySet().stream().filter(e -> remainingNewCodeToBeInserted.contains(e.getKey()))
              .collect(Collectors.toMap(Entry::getKey, Entry::getValue, (a, b) -> a, LinkedHashMap::new));

      inserter.insert(config, mergeClassPath, codeSnipit.toString(), insertLocations);
    } catch (TemplateException e1) {
      System.out.println("An exception was caught during generating of either the original or the new version of file " + mergeClassPath + " from input file "
          + inputFilePath);
      e1.printStackTrace();
    }
  }

  private CompilationUnit executeTemplateOnGitHeadFileVersion(JavaForgerConfiguration config, CodeSnippet codeSnipit, String mergeClassPath,
      String inputFilePath)
      throws AmbiguousObjectException, IncorrectObjectTypeException, IOException, MissingObjectException, CorruptObjectException, TemplateException {
    // TODO this needs to be cleaned up with logger
    String gitRepository = ((GitMergerConfiguration) config.getMergerConfiguration()).getInputGitRepository();
    String previousMergeFileContent = gitFileResolver.getFileFromHead(gitRepository, inputFilePath);
    JavaForgerConfiguration copyConfig = JavaForgerConfiguration.builder(config).build();
    copyConfig.setChildConfigs(new ArrayList<JavaForgerConfiguration>());
    copyConfig.setMerge(false);
    CodeSnippet originallyGenerated = generator.executeFromContent(copyConfig, previousMergeFileContent, inputFilePath, mergeClassPath);
    System.out.println("newly generated:");
    codeSnipit.printWithLineNumbers();
    System.out.println("originally generated:");
    originallyGenerated.printWithLineNumbers();
    CompilationUnit previouslyGeneratedCu = reader.read(originallyGenerated, mergeClassPath);
    return previouslyGeneratedCu;
  }

}
