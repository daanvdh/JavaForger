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

import java.util.Iterator;
import java.util.LinkedHashMap;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;

/**
 * Determines the location of code to be added, within existing code. Receives code that is already parsed. Ordering is based on the order defined within the
 * {@link NodeComparator}.
 *
 * @author Daan
 */
public class CodeSnipitLocater {

  NodeComparator comparator = new NodeComparator();

  /**
   * Receives two {@link CompilationUnit}s and determines the location of a package, imports, fields, constructors, methods or inner classes. This method will
   * retain the order of Nodes in both existing and new code. It can therefore happen that not all nodes are ordered according to the {@link NodeComparator} if
   * the inserted code was also not ordered like that.
   *
   * @param existingCode {@link CompilationUnit} representing the existing class
   * @param newCode {@link CompilationUnit} representing the code to be added
   * @return An {@link LinkedHashMap} with as keys a {@link CodeSnipitLocation} of code to be added and as value a {@link CodeSnipitLocation} where in the
   *         existing class the code should be added. The map is ordered on lines where the code should be added.
   */
  public LinkedHashMap<CodeSnipitLocation, CodeSnipitLocation> locate(CompilationUnit existingCode, CompilationUnit newCode) {
    LinkedHashMap<CodeSnipitLocation, CodeSnipitLocation> locations = new LinkedHashMap<>();

    recursiveLocator(locations, existingCode, newCode);

    return locations;
  }

  private void recursiveLocator(LinkedHashMap<CodeSnipitLocation, CodeSnipitLocation> newCodeInsertionlocations, CompilationUnit existingCode,
      CompilationUnit newCode) {

    Iterator<Node> existingNodes = existingCode.getChildNodes().iterator();
    Iterator<Node> newNodes = newCode.getChildNodes().iterator();

    Node existingNode = existingNodes.hasNext() ? existingNodes.next() : null;
    int lastNodeLocation = existingCode.getBegin().get().line + 1;

    while (newNodes.hasNext() && existingNode != null) {
      Node insertNode = newNodes.next();
      int compare = comparator.compare(existingNode, insertNode);

      // while the existingNode is before the insertNode search for the next Node
      while (compare < 0) {
        lastNodeLocation = existingNode.getEnd().get().line + 1;
        existingNode = existingNodes.hasNext() ? existingNodes.next() : null;
        compare = comparator.compare(existingNode, insertNode);
      }

      if (existingNode != null) {
        if (compare == 0) {
          // TODO handle this recursively
          newCodeInsertionlocations.put(CodeSnipitLocation.of(insertNode), CodeSnipitLocation.of(existingNode));
        } else {
          // The insertion Node must be added before existingNode
          newCodeInsertionlocations.put(CodeSnipitLocation.of(insertNode), CodeSnipitLocation.before(existingNode));
        }
      }
    }

    // Add the rest of nodes after the last node
    while (newNodes.hasNext()) {
      Node insertNode = newNodes.next();
      newCodeInsertionlocations.put(CodeSnipitLocation.of(insertNode), CodeSnipitLocation.of(lastNodeLocation));
    }

  }

}
