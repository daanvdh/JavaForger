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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.SimpleName;

import generator.JavaForgerException;

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
   *         existing class the code should be added. The map is ordered on increasing insert locations (map values).
   */
  public LinkedHashMap<CodeSnipitLocation, CodeSnipitLocation> locate(CompilationUnit existingCode, Node newCode) {
    LinkedHashMap<CodeSnipitLocation, CodeSnipitLocation> locations = recursiveLocator(existingCode.getChildNodes(), newCode.getChildNodes());

    // locations may not be sorted on increasing insertLocation if an existing code block will be overridden and occurs before the last determined insert
    // location. So we need to sort them here.
    locations = locations.entrySet().stream().sorted(Map.Entry.comparingByValue())
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, LinkedHashMap::new));

    return locations;
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

    List<Node> supportedInsertNodes = insertNodes.stream().filter(comparator::nodeTypeIsSupported).collect(Collectors.toList());

    for (Node insertNode : supportedInsertNodes) {
      int equalNodeIndex = findEqualNode(existingNodes, insertNode);
      if (equalNodeIndex >= 0) {
        locations.putAll(handleEqualNodesRecursively(insertNode, existingNodes.get(equalNodeIndex)));
        insertAfter = Integer.max(insertAfter, equalNodeIndex);
      } else {
        insertAfter = findInsertAfterIndex(existingNodes, insertAfter, insertNode);
        if (insertAfter < 0) {
          locations.put(CodeSnipitLocation.of(insertNode), CodeSnipitLocation.before(existingNodes.get(0)));
        } else {
          locations.put(CodeSnipitLocation.of(insertNode), CodeSnipitLocation.after(existingNodes.get(insertAfter)));
        }
      }
    }
    return locations;
  }

  private LinkedHashMap<CodeSnipitLocation, CodeSnipitLocation> handleEqualNodesRecursively(Node insertNode, Node existingNode) {
    LinkedHashMap<CodeSnipitLocation, CodeSnipitLocation> loc = new LinkedHashMap<>();
    if (isClass(existingNode) && isClass(insertNode)) {
      List<Node> insertNodes = getChildNodes(insertNode);
      List<Node> existingNodes = getChildNodes(existingNode);
      if (!insertNodes.isEmpty()) {
        if (existingNodes.isEmpty()) {
          CodeSnipitLocation firstInsertLocation = getFirstInsertLocation((ClassOrInterfaceDeclaration) existingNode);
          loc = insertNodes.stream().collect(Collectors.toMap(CodeSnipitLocation::of, c -> firstInsertLocation, (a, b) -> a, LinkedHashMap::new));
        } else {
          // Recursive call
          loc = recursiveLocator(existingNodes, insertNodes);
        }
      }
    } else {
      loc.put(CodeSnipitLocation.of(insertNode), CodeSnipitLocation.of(existingNode));
    }
    return loc;
  }

  private CodeSnipitLocation getFirstInsertLocation(ClassOrInterfaceDeclaration existingNode) {
    return CodeSnipitLocation.after(getNodeAfterToInsert(existingNode));
  }

  private Node getNodeAfterToInsert(ClassOrInterfaceDeclaration existingNode) {
    return existingNode.getChildNodes().stream().filter(node -> SimpleName.class.isAssignableFrom(node.getClass())).findFirst().orElseThrow(
        () -> new JavaForgerException("Cannot insert code into a class without a simpleName defined. Existing node is: " + existingNode.toString()));
  }

  private boolean isClass(Node existingNode) {
    return ClassOrInterfaceDeclaration.class.isAssignableFrom(existingNode.getClass());
  }

  /**
   * Gets the child nodes and strips off any nodes that are part of the definition of the parent node, such as the name of the class or what it extends.
   *
   * @param node
   * @return
   */
  private List<Node> getChildNodes(Node node) {
    return node.getChildNodes().stream().filter(comparator::nodeTypeIsSupported).collect(Collectors.toList());
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
