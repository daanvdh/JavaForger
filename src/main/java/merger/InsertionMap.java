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

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * DTO containing a map of {@link CodeSnippetLocation} from an input file to {@link CodeSnippetLocation} of an output file, representing where code from the
 * input file should be inserted into the output file.
 * 
 * @author daan.vandenheuvel
 */
public class InsertionMap extends LinkedHashMap<CodeSnippetLocation, CodeSnippetLocation> implements Iterable<merger.InsertionMap.InsertionEntry> {
  private static final long serialVersionUID = -7252456444269462648L;

  public void put(java.util.Map.Entry<CodeSnippetLocation, CodeSnippetLocation> entry) {
    super.put(entry.getKey(), entry.getValue());
  }

  public List<InsertionEntry> entries() {
    return stream().collect(Collectors.toList());
  }

  public Stream<InsertionEntry> stream() {
    return this.entrySet().stream().map(InsertionEntry::new);
  }

  public InsertionMap merge(InsertionMap that) {
    return merge(this, that);
  }

  /**
   * Merges 2 {@link InsertionMap}s into one while retaining the insertion order based on the insert location.
   * 
   * @param map1 if 2 entries are equal, the entry of map1 gets precedence over the entry from map2.
   * @param map2
   * @return {@link InsertionMap}.
   */
  public static InsertionMap merge(InsertionMap map1, InsertionMap map2) {
    Iterator<Entry<CodeSnippetLocation, CodeSnippetLocation>> it1 = map1.entrySet().iterator();
    Iterator<Entry<CodeSnippetLocation, CodeSnippetLocation>> it2 = map2.entrySet().iterator();
    Entry<CodeSnippetLocation, CodeSnippetLocation> next1 = it1.hasNext() ? it1.next() : null;
    Entry<CodeSnippetLocation, CodeSnippetLocation> next2 = it2.hasNext() ? it2.next() : null;
    InsertionMap result = new InsertionMap();
    while (next1 != null || next2 != null) {
      if (next1 == null) {
        result.put(next2);
        next2 = it2.hasNext() ? it2.next() : null;
      } else if (next2 == null) {
        result.put(next1);
        next1 = it1.hasNext() ? it1.next() : null;
      } else {
        if (next2.getValue().isBefore(next1.getValue())) {
          result.put(next2);
          next2 = it2.hasNext() ? it2.next() : null;
        } else {
          result.put(next1);
          next1 = it1.hasNext() ? it1.next() : null;
        }
      }
    }
    return result;
  }

  @Override
  public Iterator<InsertionEntry> iterator() {
    return entries().iterator();
  }

  public static <T> Collector<T, ?, InsertionMap> collect(Function<? super T, CodeSnippetLocation> keyMapper,
      Function<? super T, CodeSnippetLocation> valueMapper) {
    return Collectors.toMap(keyMapper, valueMapper, (a, b) -> a, InsertionMap::new);
  }

  public static Collector<InsertionEntry, ?, InsertionMap> collect() {
    return InsertionMap.collect(Entry::getKey, Entry::getValue);
  }

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
}
