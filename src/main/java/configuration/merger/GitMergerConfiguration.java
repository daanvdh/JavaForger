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

/**
 * TODO javadoc
 *
 * @author daan.vandenheuvel
 */
public class GitMergerConfiguration extends MergerConfiguration {

  private String gitRepository;

  public String getGitRepository() {
    return gitRepository;
  }

  public void setGitRepository(String gitRepository) {
    this.gitRepository = gitRepository;
  }

}
