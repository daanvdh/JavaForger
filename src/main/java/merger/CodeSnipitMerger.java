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

import java.io.File;
import java.io.IOException;

import configuration.JavaForgerConfiguration;
import configuration.StaticJavaForgerConfiguration;
import generator.CodeSnipit;
import generator.JavaForgerException;

/**
 * Responsible for merging {@link CodeSnipit}s into java classes. The current default implementation is {@link LineMerger}. The default can be changed within
 * {@link StaticJavaForgerConfiguration}.
 *
 * @author Daan
 */
public abstract class CodeSnipitMerger {

  public void merge(JavaForgerConfiguration config, CodeSnipit codeSnipit, String mergeClassPath) throws IOException {
    if (validate(codeSnipit, mergeClassPath)) {
      executeMerge(config, codeSnipit, mergeClassPath);
      format(config, mergeClassPath);
    }
  }

  protected abstract void executeMerge(JavaForgerConfiguration config, CodeSnipit codeSnipit, String mergeClassPath) throws IOException;

  protected boolean validate(CodeSnipit codeSnipit, String mergeClassPath) {
    boolean success = true;
    if (mergeClassPath == null) {
      throw new JavaForgerException("merge class path may not be null");
    }
    if (mergeClassPath.isEmpty()) {
      throw new JavaForgerException("merge class path may not be empty");
    }
    if (!new File(mergeClassPath).exists()) {
      throw new JavaForgerException("merge class path does not point to existing file: " + mergeClassPath);
    }
    if (codeSnipit.getCode().isEmpty()) {
      System.err.println("CodeSnipit is empty and cannot be merged to: " + mergeClassPath);
      success = false;
    }
    return success;
  }

  protected void format(JavaForgerConfiguration config, String mergeClassPath) {
    // TODO implement formatting
    System.err.println("formatting is not yet supported and should be done manually for class " + mergeClassPath);
    // Probably best to call the formatter via the cmd:
    // https://stackoverflow.com/questions/15464111/run-cmd-commands-through-java
    // https://www.beyondjava.net/run-eclipse-formatter-command-line

    // Probably we have to let the user set the path to the desired java-formatter in the config
  }

}
