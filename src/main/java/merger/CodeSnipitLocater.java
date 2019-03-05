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

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.SimpleName;

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
  public LinkedHashMap<CodeSnipitLocation, CodeSnipitLocation> locate(CompilationUnit existingCode, Node newCode) {
    return recursiveLocator(existingCode.getChildNodes(), newCode.getChildNodes());
  }

  /**
   * Calculates the insertion locations for the insertNodes within the existingNodes. Recursively handles classes. The order of the insertNodes is retained,
   * unless an earlier existing node was equal to an insert node.
   *
   * @param existingNodes The nodes from the existing class. May not be empty.
   * @param insertNodes The nodes from the class to be inserted. May not be empty.
   * @return The map of where insertion-code (from) needs to be inserted (to).
   */
  protected LinkedHashMap<CodeSnipitLocation, CodeSnipitLocation> recursiveLocator(List<Node> existingNodes, List<Node> insertNodes) {
    LinkedHashMap<CodeSnipitLocation, CodeSnipitLocation> locations = new LinkedHashMap<>();

    // -1 indicates that the new node needs to be inserted before the first node within the existing nodes.
    int insertAfter = -1;

    for (Node insertNode : insertNodes) {
      if (!skipNode(insertNode)) {
        int equalNodeIndex = findEqualNode(existingNodes, insertNode);
        if (equalNodeIndex >= 0) {
          Node existingNode = existingNodes.get(equalNodeIndex);
          insertAfter = Integer.max(insertAfter, equalNodeIndex);
          if (ClassOrInterfaceDeclaration.class.isAssignableFrom(existingNode.getClass())
              && ClassOrInterfaceDeclaration.class.isAssignableFrom(insertNode.getClass())) {
            // Recursive call
            locations.putAll(recursiveLocator(existingNode.getChildNodes(), insertNode.getChildNodes()));
          } else {
            locations.put(CodeSnipitLocation.of(insertNode), CodeSnipitLocation.of(existingNode));
          }
        } else {
          insertAfter = findInsertAfterIndex(existingNodes, insertAfter, insertNode);
          if (insertAfter < 0) {
            locations.put(CodeSnipitLocation.of(insertNode), CodeSnipitLocation.before(existingNodes.get(0)));
          } else {
            locations.put(CodeSnipitLocation.of(insertNode), CodeSnipitLocation.after(existingNodes.get(insertAfter)));
          }
        }
      }
    }
    return locations;
  }

  /**
   * Indicates if the input node needs to be skipped, because it is not supported.
   *
   * @param node The node to be checked.
   * @return true if the input needs to be skipped, false otherwise.
   */
  private boolean skipNode(Node node) {
    return Arrays.asList(SimpleName.class).stream().anyMatch(claz -> claz.isAssignableFrom(node.getClass()));
  }

  /**
   * Returns an index (integer) within existingNodes or -1 if it does not exist, so that we can set the existingIndex to that value if it's higher.
   *
   * @param existingNodes The nodes to check if one is equal to the insertNode.
   * @param insertNode The node to check.
   * @return The index of the node equal to the insertNode if it exists, -1 otherwise.
   */
  private int findEqualNode(List<Node> existingNodes, Node insertNode) {
    for (int index = 0; index < existingNodes.size(); index++) {
      if (comparator.compare(existingNodes.get(index), insertNode) == 0) {
        return index;
      }
    }
    return -1;
  }

  /**
   * Finds the next index after which the insertNode needs to be inserted.
   *
   * @param existingNodes The existing nodes we need to compare the insertNode with.
   * @param previousIndex The index used in a previous iteration to insert a next node. This index might be -1 it is the first time a node is inserted for the
   *          input existingNodes.
   * @param insertNode The node for which we need to find an insert location.
   * @return An index between the previousIndex (inclusive) and existingNodes.size (exclusive)
   */
  private int findInsertAfterIndex(List<Node> existingNodes, int previousIndex, Node insertNode) {
    int index = previousIndex;

    // Check if the previous index should be before the insertNode
    int compare = comparator.compare(existingNodes.get(Integer.max(0, index)), insertNode);

    // While the current index is before the insertIndex
    while (compare < 0 && index < existingNodes.size() - 1) {
      // compare the next existing node
      compare = comparator.compare(existingNodes.get(index + 1), insertNode);
      // only increment if next existing node should be placed before insertNode
      if (compare < 0) {
        index++;
      }
    }
    return index;
  }

}
