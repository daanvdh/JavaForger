/*
 * Copyright (c) 2023 by Eyefreight BV (www.eyefreight.com). All rights reserved.
 *
 * This software is provided by the copyright holder and contributors "as is" and any express or implied warranties, including, but
 * not limited to, the implied warranties of merchantability and fitness for a particular purpose are disclaimed. In no event shall
 * Eyefreight BV or contributors be liable for any direct, indirect, incidental, special, exemplary, or consequential damages
 * (including, but not limited to, procurement of substitute goods or services; * loss of use, data, or profits; or business
 * interruption) however caused and on any theory of liability, whether in contract, strict liability, or tort (including
 * negligence or otherwise) arising in any way out of the use of this software, even if advised of the possibility of such damage.
 */
package merger.git;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;

import com.github.javaparser.ast.CompilationUnit;

import configuration.JavaForgerConfiguration;
import configuration.merger.GitFileResolver;
import configuration.merger.GitMergerConfiguration;
import freemarker.template.TemplateException;
import generator.CodeSnipit;
import generator.Generator;
import merger.CodeSnipitInserter;
import merger.CodeSnipitLocater;
import merger.CodeSnipitLocation;
import merger.CodeSnipitMerger;
import merger.CodeSnipitReader;
import merger.MergeType;

/**
 * A {@link CodeSnipitMerger} that uses git to find what was newly generated. This merger has to have access to the git repository for it to work. It relies on
 * the code that would force something to change compared to earlier generated code, is not yet committed. This class will stash everything to create an
 * original generated code (o). Then it will pop the stash and generate a new version of the code (n). Only lines of code that are in n but not in o, will be
 * merged into the original.
 *
 * @author daan.vandenheuvel
 */
public class GitMerger extends CodeSnipitMerger {

  private CodeSnipitLocater locater = new CodeSnipitLocater();
  private CodeSnipitInserter inserter = new CodeSnipitInserter();
  private CodeSnipitReader reader = new CodeSnipitReader();
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
  protected void executeMerge(JavaForgerConfiguration config, CodeSnipit codeSnipit, String mergeClassPath, String inputFilePath) throws IOException {

    CompilationUnit existingCode = reader.read(mergeClassPath);
    CompilationUnit newCode = reader.read(codeSnipit, mergeClassPath);
    LinkedHashMap<CodeSnipitLocation, CodeSnipitLocation> newCodeInsertionLocations = locater.locate(existingCode, newCode, config);

    // ===========================================================================
    // ===========================================================================
    // ===========================================================================

    LinkedHashMap<CodeSnipitLocation, CodeSnipitLocation> insertLocations = new LinkedHashMap<>();
    String fileContent;

    // GitMergerConfiguration gitConfig = (GitMergerConfiguration) config.getMergerConfiguration();

    // String gitRepository = "C:\\gitrepo\\tms";
    // final String inputFilePath = "tms-core/src/main/java/com/eyefreight/tms/platform/core/transformation/template/data/BusinessUnitTemplateData.java";
    // =================================== new try via git show ========================================
    String gitRepository = ((GitMergerConfiguration) config.getMergerConfiguration()).getGitRepository();
    File file = new File(gitRepository);
    try (Repository repository = Git.open(file).getRepository()) {

      // TODO fix this call
      // String inputFile = config.getInputClassProvider().provide(mergeClassPath);
      // String fileContent = gitFileResolver.getFileContentFromHead(inputFile);

      ObjectId lastCommitId = repository.resolve(Constants.HEAD);

      // a RevWalk allows to walk over commits based on some filtering that is defined
      try (RevWalk revWalk = new RevWalk(repository)) {
        RevCommit commit = revWalk.parseCommit(lastCommitId);
        // and using commit's tree find the path
        RevTree tree = commit.getTree();
        System.out.println("Having tree: " + tree);

        // now try to find a specific file
        String gitFilePath = inputFilePath.replace(gitRepository, "").substring(1);
        try (TreeWalk treeWalk = new TreeWalk(repository)) {
          treeWalk.addTree(tree);
          treeWalk.setRecursive(true);
          treeWalk.setFilter(PathFilter.create(gitFilePath));
          if (!treeWalk.next()) {
            throw new IllegalStateException("Did not find expected file " + gitFilePath);
          }

          ObjectId objectId = treeWalk.getObjectId(0);
          ObjectLoader loader = repository.open(objectId);

          fileContent = new String(loader.getBytes());
          // and then one can the loader to read the file
          // loader.copyTo(System.out);
        }

        revWalk.dispose();
      }
    }

    System.out.println("We got the following original file content from the last git commit:");
    System.out.println(fileContent);
    // =================================== new try via git show ========================================

    JavaForgerConfiguration copyConfig = JavaForgerConfiguration.builder(config).build();
    copyConfig.setChildConfigs(new ArrayList<JavaForgerConfiguration>());
    copyConfig.setMerge(false);

    try {
      // String fullFileName = gitRepository + "/" + inputFilePath;
      CodeSnipit originallyGenerated = generator.executeFromContent(copyConfig, fileContent, inputFilePath, mergeClassPath);
      System.out.println("newly generated:");
      codeSnipit.printWithLineNumbers();
      System.out.println("originally generated:");
      originallyGenerated.printWithLineNumbers();
      CompilationUnit originallyGeneratedCu = reader.read(originallyGenerated, mergeClassPath);
      LinkedHashMap<CodeSnipitLocation, CodeSnipitLocation> newCodeInsertionsIntoOriginal = locater.locate(originallyGeneratedCu, newCode, config);

      // Remove insertLocations that are not in the originalGenerated
      List<CodeSnipitLocation> remainingNewCodeToBeInserted =
          newCodeInsertionsIntoOriginal.entrySet().stream().filter((e) -> e.getValue().getNumberOfLines() == 0).map(Entry::getKey).collect(Collectors.toList());
      LinkedHashMap<CodeSnipitLocation, CodeSnipitLocation> remainingNewCodeInsertLocations =
          newCodeInsertionLocations.entrySet().stream().filter(e -> remainingNewCodeToBeInserted.contains(e.getKey()))
              .collect(Collectors.toMap(Entry::getKey, Entry::getValue, (a, b) -> a, LinkedHashMap::new));
      insertLocations = remainingNewCodeInsertLocations;
    } catch (TemplateException e1) {
      System.out.println("An exception was caught during generating of either the original or the new version of file " + mergeClassPath + " from input file "
          + inputFilePath);
      e1.printStackTrace();
    }

    // ===========================================================================
    // ===========================================================================
    // ===========================================================================

    inserter.insert(config, mergeClassPath, codeSnipit.toString(), insertLocations);

  }

}
