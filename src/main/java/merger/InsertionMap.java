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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
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
public class InsertionMap extends LinkedHashMap<CodeSnippetLocation, List<InsertionEntry>> implements Iterable<InsertionEntry> {
  private static final long serialVersionUID = -7252456444269462648L;

  // LinkedHashMap<CodeSnippetLocation, List<InsertionEntry>> entries = new HashMap<>();

  // /**
  // * This only exists for entries that need to be deleted, in all other scenarios the key {@link CodeSnippetLocation} needs to be unique.
  // */
  // private List<InsertionEntry> nullEntries = new ArrayList<>();
  // private Map<CodeSnippetLocation, InsertionEntry> entries = new HashMap<>();

  public void put(java.util.Map.Entry<CodeSnippetLocation, CodeSnippetLocation> entry) {
    this.put(entry.getKey(), entry.getValue());
  }

  public List<InsertionEntry> entries() {
    return stream().collect(Collectors.toList());
  }

  public Stream<InsertionEntry> stream() {
    return this.values().stream().flatMap(Collection::stream);
  }

  public InsertionMap merge(InsertionMap that) {
    return merge(this, that);
  }

  @Override
  public String toString() {
    return entries().toString();
  }

  /**
   * Merges 2 {@link InsertionMap}s into one while retaining the insertion order based on the insert location.
   * 
   * @param map1 if 2 entries are equal, the entry of map1 gets precedence over the entry from map2.
   * @param map2
   * @return {@link InsertionMap}.
   */
  public static InsertionMap merge(InsertionMap map1, InsertionMap map2) {
    Iterator<InsertionEntry> it1 = map1.iterator();
    Iterator<InsertionEntry> it2 = map2.iterator();
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
    return stream().iterator();
  }

  public static <T> Collector<T, ?, InsertionMap> collect(Function<? super T, CodeSnippetLocation> keyMapper,
      Function<? super T, CodeSnippetLocation> valueMapper) {
    Function<? super T, List<InsertionEntry>> valueListMapper = t -> {
      List<InsertionEntry> list = new ArrayList<>();
      CodeSnippetLocation from = keyMapper.apply(t);
      CodeSnippetLocation to = valueMapper.apply(t);
      list.add(new InsertionEntry(from, to));
      return list;
    };
    return Collectors.toMap(keyMapper, valueListMapper, (a, b) -> a, InsertionMap::new);
  }

  public static Collector<InsertionEntry, ?, InsertionMap> collect() {
    return InsertionMap.collect(Entry::getKey, Entry::getValue);
  }

  public void put(CodeSnippetLocation from, CodeSnippetLocation to) {
    this.put(from, new InsertionEntry(from, to));
  }

  private void put(CodeSnippetLocation from, InsertionEntry insertionEntry) {
    List<InsertionEntry> list = super.get(from);
    if (list == null) {
      list = new ArrayList<>();
    }
    list.add(insertionEntry);
    super.put(from, list);
  }

  public CodeSnippetLocation getTo(CodeSnippetLocation from) {
    List<InsertionEntry> list = super.get(from);
    return (list != null && !list.isEmpty()) ? list.get(0).getTo() : null;
  }

  public boolean hasLocationKeyIncluded(CodeSnippetLocation location) {
    return this.getLocationKeyThatIncludes(location).isPresent();
  }

  public Optional<InsertionEntry> getLocationKeyThatIncludes(CodeSnippetLocation location) {
    return this.stream().filter(e -> e.getKey().includes(location)).findFirst();
  }

  public boolean containsFromNode(com.github.javaparser.ast.Node t) {
    return this.keySet().stream().map(CodeSnippetLocation::getNode).filter(t::equals).findFirst().isPresent();
  }

  public boolean containsToNode(com.github.javaparser.ast.Node to) {
    return getLocationFromToNode(to).isPresent();
  }

  public Optional<CodeSnippetLocation> getLocationFromToNode(com.github.javaparser.ast.Node to) {
    return stream().map(InsertionEntry::getTo).filter(l -> to.equals(l.getNode())).findFirst();
  }
}
