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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
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
import merger.InsertionMap;
import merger.InsertionMap.InsertionEntry;
import merger.MergeType;
import template.TemplateService;

/**
 * A {@link CodeSnippetMerger} that uses git to find what was newly generated. This merger has to have access to the git repository for it to work. It relies on
 * the code that would force something to change compared to earlier generated code, is not yet committed.
 *
 * @author daan.vandenheuvel
 */
public class GitMerger extends CodeSnippetMerger {
  private static final Logger LOG = LoggerFactory.getLogger(GitMerger.class);

  private CodeSnippetLocater locater = new CodeSnippetLocater();
  private CodeSnipitInserter inserter = new CodeSnipitInserter();
  private CodeSnippetReader reader = new CodeSnippetReader();
  private GitFileResolver gitFileResolver = new GitFileResolver();
  private TemplateService templateService = new TemplateService();
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
      // TODO LOG instead
      System.out.println("newly generated:");
      codeSnipit.printWithLineNumbers();

      CompilationUnit previousCu = executeTemplateOnGitHeadFileVersion(config, mergeClassPath, inputFilePath);
      CompilationUnit currentCu = reader.read(mergeClassPath);
      CompilationUnit newCu = reader.read(codeSnipit, mergeClassPath);

      InsertionMap newIntoCurrent = locater.locate(currentCu, newCu, config);
      InsertionMap newIntoPrevious = locater.locate(previousCu, newCu, config);
      InsertionMap previousIntoNew = locater.locate(newCu, previousCu, config);
      InsertionMap previousIntoCurrent = locater.locate(currentCu, previousCu, config);

      InsertionMap insertLocations = createInsertions(newIntoCurrent, newIntoPrevious);
      InsertionMap deleteLocations = createDeletions(previousIntoNew, previousIntoCurrent);
      InsertionMap replaceLocations = createReplacements(newIntoPrevious, previousIntoCurrent);
      InsertionMap allLocations = insertLocations.merge(deleteLocations).merge(replaceLocations);

      inserter.insert(config, mergeClassPath, codeSnipit.toString(), allLocations);
    } catch (TemplateException e1) {
      System.out.println("An exception was caught during generating of either the original or the new version of file " + mergeClassPath + " from input file "
          + inputFilePath);
      e1.printStackTrace();
    }
  }

  private InsertionMap createReplacements(InsertionMap newIntoPrevious, InsertionMap previousIntoCurrent) {
    // The code below is mostly for fields and variables which are equal because their name is equal, but have a different assignment.
    // This will find all new code, but if the from and to are NOT EQUAL AND from old to new it was EQUAL, we should also merge it.
    InsertionMap replaceFromNewToPrevious = newIntoPrevious.stream() //
        .filter((e) -> e.getTo().isNotEmpty()) //
        .filter((e) -> e.getTo().getNode() != null) //
        .filter((e) -> e.getFrom().getNode() != null) //
        .filter(e -> !this.isNodeEqual(e.getFrom(), e.getTo())) //
        .collect(InsertionMap.collect());

    InsertionMap replaceLocations = replaceFromNewToPrevious.stream()
        // In theory this should simply let everything thru since all nodes are represented.
        .filter(e -> previousIntoCurrent.containsKey(e.getTo())) //
        .collect(InsertionMap.collect(InsertionEntry::getFrom, e -> previousIntoCurrent.get(e.getTo())));
    return replaceLocations;
  }

  /**
   * Remove code that was generated in the previousCu AND was NOT present in the newCu IF it exists in the currentCu.
   * 
   * @param previousIntoNew
   * @param previousIntoCurrent
   * @return
   */
  private InsertionMap createDeletions(InsertionMap previousIntoNew, InsertionMap previousIntoCurrent) {
    List<CodeSnippetLocation> deleteFromPrevious = previousIntoNew.entries().stream()
        // Filter code locations that where previously generated, but is now not generated anymore.
        .filter(e2 -> e2.getTo().isEmpty()) //
        .map(InsertionEntry::getFrom).collect(Collectors.toList());

    // We need to map these values to the current CodeSnippetLocation counterparts.
    // TODO InsertionMap needs to allow duplicate keys
    InsertionMap deleteLocations = previousIntoCurrent.entrySet().stream().filter(e1 -> deleteFromPrevious.contains(e1.getKey())).map(Map.Entry::getValue)
        .collect(Collectors.toMap(v -> CodeSnippetLocation.of(0, 0, 0, 0), v -> v, (a1, b1) -> a1, InsertionMap::new));
    return deleteLocations;
  }

  private InsertionMap createInsertions(InsertionMap newIntoCurrent, InsertionMap newIntoPrevious) {
    // Now we have both (x = newIntoCurrent) where it should be inserted into the existing file and (y = newIntoPrevious) where it would have been inserted
    // in a file resulting from an unchanged file generated from the input template. With this we can keep only the insertion locations from (x) for which
    // the same new line from (y) is not a replacement. This is because lines are only replaced if they are equal. If they where not equal, this indicates
    // it's a line that would not have been previously generated. Therefore it should be inserted.

    List<CodeSnippetLocation> insertFromNew = newIntoPrevious.stream() //
        .filter((e) -> e.getTo().isEmpty()) //
        .map(InsertionEntry::getFrom).collect(Collectors.toList());
    InsertionMap insertLocations = newIntoCurrent.stream() //
        .filter(e -> insertFromNew.contains(e.getFrom())) //
        .collect(Collectors.toMap(Entry::getKey, Entry::getValue, (a, b) -> a, InsertionMap::new));
    return insertLocations;
  }

  private boolean isNodeEqual(CodeSnippetLocation from, CodeSnippetLocation to) {
    // TODO We cannot use the NodeEqualityChecker here, because that will be true for all node pairs at this point
    // Checking equality on the toString should at least ignore spaces.
    // The cleanest way is to use the equalityChecker on the field assignment.
    return from.getNode().toString().equals(to.getNode().toString());
  }

  private CompilationUnit executeTemplateOnGitHeadFileVersion(JavaForgerConfiguration config, String mergeClassPath, String inputFilePath)
      throws AmbiguousObjectException, IncorrectObjectTypeException, IOException, MissingObjectException, CorruptObjectException, TemplateException {
    GitMergerConfiguration gitConfig = (GitMergerConfiguration) config.getMergerConfiguration();

    Optional<String> previousInputContent = gitFileResolver.getFileFromHead(gitConfig.getInputGitRepository(), inputFilePath);
    Optional<String> absoluteTemplateFilePath = templateService.getAbsoluteTemplatePath(config.getTemplate());
    Optional<String> previousTemplateContent =
        absoluteTemplateFilePath.flatMap(path -> gitFileResolver.getFileFromHead(gitConfig.getTemplateGitRepository(), path));

    CodeSnippet originallyGenerated;
    JavaForgerConfiguration copyConfig = createJavaForgerConfiguration(config, previousTemplateContent.orElse(null));
    if (previousTemplateContent.isPresent()) {
      originallyGenerated = generator.executeFromContent(copyConfig, previousInputContent.get(), inputFilePath, mergeClassPath);
    } else if (previousTemplateContent.isPresent()) {
      originallyGenerated = generator.execute(copyConfig, inputFilePath, mergeClassPath);
    } else {
      originallyGenerated = new CodeSnippet("");
      LOG.warn("Did not find any previous file ({}, {}) in any git repository ({}, {})", config.getTemplate(), inputFilePath, gitConfig.getInputGitRepository(),
          gitConfig.getTemplateGitRepository());
    }

    // TODO this needs to be cleaned up with logger
    System.out.println("originally generated:");
    originallyGenerated.printWithLineNumbers();
    CompilationUnit previouslyGeneratedCu = reader.read(originallyGenerated, mergeClassPath);
    return previouslyGeneratedCu;
  }

  private JavaForgerConfiguration createJavaForgerConfiguration(JavaForgerConfiguration config, String templateContent) {
    JavaForgerConfiguration copyConfig = JavaForgerConfiguration.builder(config).template("randomTemplateName").build();
    copyConfig.clearChildConfigs();
    copyConfig.setMerge(false);
    copyConfig.setTemplateContent(templateContent);
    return copyConfig;
  }

}
