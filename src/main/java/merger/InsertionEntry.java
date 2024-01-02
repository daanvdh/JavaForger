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
package merger;

import java.util.Map;

public class InsertionEntry implements Map.Entry<CodeSnippetLocation, CodeSnippetLocation> {

  private CodeSnippetLocation from;
  private CodeSnippetLocation to;

  public InsertionEntry(CodeSnippetLocation from, CodeSnippetLocation to) {
    this.from = from;
    this.to = to;
  }

  public InsertionEntry(Map.Entry<CodeSnippetLocation, CodeSnippetLocation> entry) {
    this(entry.getKey(), entry.getValue());
  }

  public CodeSnippetLocation getFrom() {
    return this.from;
  }

  public CodeSnippetLocation getTo() {
    return this.to;
  }

  @Override
  public String toString() {
    return from.toString() + " ==> " + to.toString();
  }

  @Override
  public CodeSnippetLocation getKey() {
    return getFrom();
  }

  @Override
  public CodeSnippetLocation getValue() {
    return getTo();
  }

  @Override
  public CodeSnippetLocation setValue(CodeSnippetLocation value) {
    CodeSnippetLocation old = this.from;
    this.from = value;
    return old;
  }

}
