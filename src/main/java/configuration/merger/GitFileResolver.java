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
package configuration.merger;

import java.io.IOException;

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
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;

/**
 * TODO javadoc
 *
 * @author daan.vandenheuvel
 */
public class GitFileResolver {

  // public class ReadFileFromCommit {

  public static void main(String[] args) throws IOException {
    new GitFileResolver().getFileFromHead();
  }

  private void getFileFromHead() throws AmbiguousObjectException, IncorrectObjectTypeException, IOException, MissingObjectException, CorruptObjectException {
    try (Repository repository = openJGitCookbookRepository()) {
      // find the HEAD
      ObjectId lastCommitId = repository.resolve(Constants.HEAD);

      // a RevWalk allows to walk over commits based on some filtering that is defined
      try (RevWalk revWalk = new RevWalk(repository)) {
        RevCommit commit = revWalk.parseCommit(lastCommitId);
        // and using commit's tree find the path
        RevTree tree = commit.getTree();
        System.out.println("Having tree: " + tree);

        // now try to find a specific file
        try (TreeWalk treeWalk = new TreeWalk(repository)) {
          treeWalk.addTree(tree);
          treeWalk.setRecursive(true);
          treeWalk.setFilter(PathFilter.create("README.md"));
          if (!treeWalk.next()) {
            throw new IllegalStateException("Did not find expected file 'README.md'");
          }

          ObjectId objectId = treeWalk.getObjectId(0);
          ObjectLoader loader = repository.open(objectId);

          // and then one can the loader to read the file
          loader.copyTo(System.out);
        }

        revWalk.dispose();
      }
    }
  }

  public static Repository openJGitCookbookRepository() throws IOException {
    FileRepositoryBuilder builder = new FileRepositoryBuilder();
    return builder.readEnvironment() // scan environment GIT_* variables
        .findGitDir() // scan up the file system tree
        .build();
  }

}
