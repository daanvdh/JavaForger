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
package configuration.merger;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.CorruptObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class responsible for getting different file versions from a git repository.
 *
 * @author daan.vandenheuvel
 */
public class GitFileResolver {
  private static final Logger LOG = LoggerFactory.getLogger(GitFileResolver.class);

  public Optional<String> getFileFromHead(String gitRepository, String absoluteInputFilePath) {
    String fileContent = null;
    try {
      fileContent = getFileFromHeadUnsafe(gitRepository, absoluteInputFilePath);
    } catch (Exception e) {
      LOG.error("Unable to get file " + absoluteInputFilePath + " from git repository " + gitRepository + " error: " + e.getMessage());
    }

    LOG.debug("We got the following original file content from the last git commit:");
    LOG.debug(fileContent);

    return Optional.ofNullable(fileContent);
  }

  private String getFileFromHeadUnsafe(String gitRepository, String absoluteInputFilePath)
      throws AmbiguousObjectException, IncorrectObjectTypeException, IOException, MissingObjectException, CorruptObjectException {
    String fileContent;
    File file = new File(gitRepository);
    try (Repository repository = Git.open(file).getRepository()) {
      ObjectId lastCommitId = repository.resolve(Constants.HEAD);

      // a RevWalk allows to walk over commits based on some filtering that is defined
      try (RevWalk revWalk = new RevWalk(repository)) {
        RevCommit commit = revWalk.parseCommit(lastCommitId);
        // and using commit's tree find the path
        RevTree tree = commit.getTree();
        LOG.debug("Having tree: " + tree);

        // now try to find a specific file
        String gitFilePath = getRelativeGitPath(gitRepository, absoluteInputFilePath);
        try (TreeWalk treeWalk = new TreeWalk(repository)) {
          treeWalk.addTree(tree);
          treeWalk.setRecursive(true);
          treeWalk.setFilter(PathFilter.create(gitFilePath));
          if (!treeWalk.next()) {
            throw new IllegalStateException(
                "In repository: " + gitRepository + " did not find expected file: " + gitFilePath + " with absolute path: " + absoluteInputFilePath);
          }

          ObjectId objectId = treeWalk.getObjectId(0);
          ObjectLoader loader = repository.open(objectId);

          fileContent = new String(loader.getBytes());
        }

        revWalk.dispose();
      }
    }
    return fileContent;
  }

  private String getRelativeGitPath(String repo, String path) {
    String pathStandardized = path.replace("\\", "/");
    String repoStandardized = repo.replace("\\", "/");
    String relativePath = pathStandardized.replace(repoStandardized, "");
    if (relativePath.startsWith("/")) {
      relativePath = relativePath.substring(1);
    }
    return relativePath;
  }

}