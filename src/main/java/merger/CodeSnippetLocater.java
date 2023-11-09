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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.CallableDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.comments.LineComment;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.utils.Log;

import configuration.JavaForgerConfiguration;
import configuration.MergeLevel;
import generator.JavaForgerException;

/**
 * Determines the location of code to be added, within existing code. Receives code that is already parsed. Ordering is based on the order defined within the
 * {@link NodeEqualityChecker}.
 *
 * @author Daan
 */
public class CodeSnippetLocater {

  private NodeEqualityChecker equalityChecker = new NodeEqualityChecker();
  private NodeComparator comparator = new NodeComparator();

  /**
   * Receives two {@link CompilationUnit}s and determines the location of a package, imports, fields, constructors, methods or inner classes. This method will
   * retain the order of Nodes in both existing and new code. It can therefore happen that not all nodes are ordered according to the
   * {@link NodeEqualityChecker} if the inserted code was also not ordered like that.
   *
   * @param existingCode {@link CompilationUnit} representing the existing class
   * @param newCode {@link CompilationUnit} representing the code to be added
   * @return An {@link LinkedHashMap} with as keys a {@link CodeSnippetLocation} of code to be added and as value a {@link CodeSnippetLocation} where in the
   *         existing class the code should be added. The map is ordered on increasing insert locations (map values).
   */
  public LinkedHashMap<CodeSnippetLocation, CodeSnippetLocation> locate(CompilationUnit existingCode, Node newCode, JavaForgerConfiguration config) {
    LinkedHashMap<CodeSnippetLocation, CodeSnippetLocation> locations = new LinkedHashMap<>();
    if (MergeLevel.FILE.equals(config.getMergeLevel())) {
      locations.put(CodeSnippetLocation.of(newCode), CodeSnippetLocation.of(existingCode));
    } else {
      locations = recursiveLocator(existingCode.getChildNodes(), newCode.getChildNodes(), config);
    }

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
  protected LinkedHashMap<CodeSnippetLocation, CodeSnippetLocation> recursiveLocator(List<Node> existingNodes, List<Node> insertNodes,
      JavaForgerConfiguration config) {
    LinkedHashMap<CodeSnippetLocation, CodeSnippetLocation> locations = new LinkedHashMap<>();

    // -1 indicates that the new node needs to be inserted before the first node within the existing nodes.
    int insertAfter = -1;

    int lastEqualNodeIndex = 0;
    for (Node insertNode : insertNodes) {
      int equalNodeIndex = findEqualNode(existingNodes, insertNode, lastEqualNodeIndex);
      if (equalNodeIndex >= 0) { // Found equal node
        if (!(insertNode instanceof ImportDeclaration)) {
          // lastEqualNodeIndex = equalNodeIndex;
        }
        locations.putAll(handleEqualNodesRecursively(insertNode, existingNodes.get(equalNodeIndex), config));
        insertAfter = Integer.max(insertAfter, equalNodeIndex);
      } else {
        insertAfter = findInsertAfterIndex(existingNodes, insertAfter, insertNode);
        if (insertAfter < 0) {
          locations.put(CodeSnippetLocation.of(insertNode), CodeSnippetLocation.before(existingNodes.get(0)));
        } else {
          locations.put(CodeSnippetLocation.of(insertNode), CodeSnippetLocation.after(existingNodes.get(insertAfter)));
        }
      }
    }
    return locations;
  }

  /**
   * Finds insert locations for all nodes, keeps the ordering of nodes the same. Each node not equal to an existing node will be placed directly after the last
   * found equal node, or at the start of the existing code block if no equal node was found yet. This method is not recursive, all nodes will be individually
   * located within the existing nodes.
   *
   * @param existingNodes
   * @param insertNodes
   * @param config
   * @return
   */
  private LinkedHashMap<CodeSnippetLocation, CodeSnippetLocation> createCodeBlockLocationMap(List<Node> existingNodes, List<Node> insertNodes,
      JavaForgerConfiguration config) {
    LinkedHashMap<CodeSnippetLocation, CodeSnippetLocation> locations = new LinkedHashMap<>();
    if (existingNodes.isEmpty()) {
      // LOG.error("Cannot insert the following nodes in empty existing code block", insertNodes);
      return locations;
    }

    // -1 indicates that the new node needs to be inserted before the first node within the existing nodes.
    int previousMatchingExistingNodeIndex = -1;
    for (Node insertNode : insertNodes) {
      int equalNodeIndex = -1;
      // Add one to the index so that we never compare the same node twice.
      int searchIndex = previousMatchingExistingNodeIndex + 1;
      if (searchIndex < existingNodes.size()) {
        equalNodeIndex = findEqualNode(existingNodes, insertNode, searchIndex);
      }
      if (equalNodeIndex >= 0) {
        Node existingNode = existingNodes.get(equalNodeIndex);
        Optional<BlockStmt> innerBlockStmt = insertNode.findFirst(BlockStmt.class);
        if (innerBlockStmt.isPresent()) {
          BlockStmt insertBlockStmt = innerBlockStmt.get();
          List<Node> insertChildren = insertBlockStmt.getChildNodes();
          BlockStmt existingBlockStmt = existingNode.findFirst(BlockStmt.class).get();
          List<Node> existingChildren = existingBlockStmt.getChildNodes();

          if (insertChildren.isEmpty() || existingChildren.isEmpty()) {
            locations.put(CodeSnippetLocation.of(insertBlockStmt), CodeSnippetLocation.of(existingBlockStmt));
          } else {
            // for readibillity we will show which part of the following example statement is added on which line: if(x) {y;}
            // if(x) {
            locations.put(CodeSnippetLocation.fromUntil(insertNode, insertChildren.get(0)),
                CodeSnippetLocation.fromUntil(existingNode, existingChildren.get(0)));
            // y;
            locations.putAll(createCodeBlockLocationMap(existingChildren, insertChildren, config));
            // }
            locations.put(CodeSnippetLocation.fromAfterUntilIncluding(insertChildren.get(insertChildren.size() - 1), insertBlockStmt),
                CodeSnippetLocation.fromAfterUntilIncluding(existingChildren.get(existingChildren.size() - 1), existingBlockStmt));
          }

        } else {
          locations.put(CodeSnippetLocation.of(insertNode), CodeSnippetLocation.of(existingNode));
          previousMatchingExistingNodeIndex = equalNodeIndex;
        }
      } else { // No equal node was found
        // If a similar line exists, merge that line
        Node mergeLineNode = findExistingNodeToMergeWith(config, existingNodes, insertNode, previousMatchingExistingNodeIndex);
        if (mergeLineNode != null) {
          // TODO experimental code that still needs to be tested. The CodeSnippetInserted does not support this yet.
          LinkedHashMap<CodeSnippetLocation, CodeSnippetLocation> subLinelocations = mergeNodes(insertNode, mergeLineNode);
          subLinelocations.keySet().stream().forEach(k -> locations.put(k, subLinelocations.get(k)));
        } else {
          // No equal node is found, determine in between which nodes the insert node needs to be inserted.
          // Here we should use a comparator comparing the insertNode with the existingNodes, starting from the searchIndex.
          // Put the insertNode at the first location possible and update the searchIndex.
          CodeSnippetLocation newLocation = null;
          for (int i = Math.max(0, previousMatchingExistingNodeIndex); i < existingNodes.size(); i++) {
            int compare = comparator.compare(insertNode, existingNodes.get(i));
            if (compare <= 0) {
              newLocation = CodeSnippetLocation.before(existingNodes.get(i));
              previousMatchingExistingNodeIndex = i;
              break;
            }
          }
          if (newLocation == null) {
            newLocation = CodeSnippetLocation.after(existingNodes.get(existingNodes.size() - 1));
            previousMatchingExistingNodeIndex = existingNodes.size();
          }
          locations.put(CodeSnippetLocation.of(insertNode), newLocation);
        }
      }
    }
    return locations;
  }

  private LinkedHashMap<CodeSnippetLocation, CodeSnippetLocation> mergeNodes(Node insertNode, Node existingNode) {
    LinkedHashMap<CodeSnippetLocation, CodeSnippetLocation> subLinelocations = new LinkedHashMap<>();
    int existingIndex = 0, insertIndex = 0;
    List<Node> mergeLineChildNodes = existingNode.getChildNodes();
    while (insertIndex < insertNode.getChildNodes().size()) {
      Node insertSubLine = insertNode.getChildNodes().get(insertIndex);
      Node existingSubLine = mergeLineChildNodes.get(existingIndex);
      if (existingIndex < mergeLineChildNodes.size() && insertIndex < insertNode.getChildNodes().size()) {
        // TODO first analyse the children of the node before analysing the node itself.
        if (equalityChecker.isEqualIgnoreChildren(insertSubLine, existingSubLine)) {
          // TODO This does put a way bigger location on both sides than intended, because all it's children are also incuded.
          subLinelocations.put(CodeSnippetLocation.of(insertSubLine), CodeSnippetLocation.of(existingSubLine));
          existingIndex++;
          insertIndex++;
        } else {
          subLinelocations.put(CodeSnippetLocation.of(insertSubLine), CodeSnippetLocation.before(existingSubLine));
          insertIndex++;
        }
      } else if (insertIndex < insertNode.getChildNodes().size()) {
        subLinelocations.put(CodeSnippetLocation.of(insertSubLine), CodeSnippetLocation.after(mergeLineChildNodes.get(mergeLineChildNodes.size() - 1)));
        insertIndex++;
      }
    }
    return subLinelocations;
  }

  private Node findExistingNodeToMergeWith(JavaForgerConfiguration config, List<Node> existingNodes, Node insertNode, int previousMatchingExistingNodeIndex) {
    Node mergeLineNode = null;
    if (config.getMergeLevel().isAsFineGrainedAs(MergeLevel.SUB_LINE) && this.subLineMergeIsSupportedFor(insertNode)) {
      // find first of same type where the first 'something' matches.
      for (int i = Math.max(0, previousMatchingExistingNodeIndex); i < existingNodes.size() && mergeLineNode == null; i++) {
        Node existingNode = existingNodes.get(i);
        if (existingNode.getClass().equals(insertNode.getClass()) && !existingNode.getChildNodes().isEmpty() && !insertNode.getChildNodes().isEmpty()
        // && existingNode.getChildNodes().get(0).toString().equals(insertNode.getChildNodes().get(0).toString())
        ) {
          Log.trace("Determined existing node {} and insert node {} to be equal enough to try and merge them", () -> existingNode, () -> insertNode);
          mergeLineNode = existingNode;
        }
      }
    }
    return mergeLineNode;
  }

  /**
   * Only return nodes are supported to be merged on sub_line level at the moment.
   *
   * @param node The {@link Node} to be checked.
   * @return true if supported, false otherwise.
   */
  private boolean subLineMergeIsSupportedFor(Node node) {
    return Set.of(ReturnStmt.class).contains(node.getClass());
  }

  private LinkedHashMap<CodeSnippetLocation, CodeSnippetLocation> handleEqualNodesRecursively(Node insertNode, Node existingNode,
      JavaForgerConfiguration config) {
    LinkedHashMap<CodeSnippetLocation, CodeSnippetLocation> locationMap = new LinkedHashMap<>();
    if (isClass(existingNode) && isClass(insertNode)) {
      List<Node> insertNodes = getClassChildNodes(insertNode);
      List<Node> existingNodes = getClassChildNodes(existingNode);
      if (!insertNodes.isEmpty()) {
        if (existingNodes.isEmpty()) {
          CodeSnippetLocation firstInsertLocation = getFirstInsertLocation((ClassOrInterfaceDeclaration) existingNode);
          locationMap = insertNodes.stream().collect(Collectors.toMap(CodeSnippetLocation::of, c -> firstInsertLocation, (a, b) -> a, LinkedHashMap::new));
        } else {
          // Recursive call
          locationMap = recursiveLocator(existingNodes, insertNodes, config);
        }
      }
    } else if (MergeLevel.LINE.isAsFineGrainedAs(config.getMergeLevel()) && isMethodOrConstructor(existingNode, insertNode)) {
      List<Node> insertNodes = getMethodChildNodes(insertNode);
      List<Node> existingNodes = getMethodChildNodes(existingNode);
      if (!insertNodes.isEmpty()) {
        if (existingNodes.isEmpty()) {
          CodeSnippetLocation firstInsertLocation = getFirstInsertLocation((ClassOrInterfaceDeclaration) existingNode);
          locationMap = insertNodes.stream().collect(Collectors.toMap(CodeSnippetLocation::of, c -> firstInsertLocation, (a, b) -> a, LinkedHashMap::new));
        } else {
          // TODO handle existingNodes.isEmpty()
          locationMap = createCodeBlockLocationMap(existingNodes, insertNodes, config);
        }
      }
      // TODO support annotations, javadoc and parameter naming to be inserted.

    } else {
      locationMap.put(CodeSnippetLocation.of(insertNode), CodeSnippetLocation.of(existingNode));
    }
    return locationMap;
  }

  private CodeSnippetLocation getFirstInsertLocation(ClassOrInterfaceDeclaration existingNode) {
    return CodeSnippetLocation.after(getNodeAfterToInsert(existingNode));
  }

  private Node getNodeAfterToInsert(ClassOrInterfaceDeclaration existingNode) {
    return existingNode.getChildNodes().stream().filter(node -> SimpleName.class.isAssignableFrom(node.getClass())).findFirst().orElseThrow(
        () -> new JavaForgerException("Cannot insert code into a class without a simpleName defined. Existing node is: " + existingNode.toString()));
  }

  private boolean isClass(Node existingNode) {
    return ClassOrInterfaceDeclaration.class.isAssignableFrom(existingNode.getClass());
  }

  private boolean isMethodOrConstructor(Node existingNode, Node insertNode) {
    return CallableDeclaration.class.isAssignableFrom(existingNode.getClass()) && CallableDeclaration.class.isAssignableFrom(insertNode.getClass());
  }

  /**
   * Gets the child nodes and strips off any nodes that are part of the definition of the parent node, such as the name of the class or what it extends.
   *
   * @param node
   * @return
   */
  private List<Node> getClassChildNodes(Node node) {
    // return node.findFirst(BlockStmt.class).map(Node::getChildNodes).orElse(Collections.emptyList());
    return node.getChildNodes().stream().filter(this::classNodeTypeIsSupported).collect(Collectors.toList());
  }

  private boolean classNodeTypeIsSupported(Node node) {
    return Arrays.asList( //
        ClassOrInterfaceDeclaration.class //
        , BodyDeclaration.class //
        , FieldDeclaration.class //
        , BlockStmt.class //
        , LineComment.class //
    ).stream().anyMatch(claz -> claz.isAssignableFrom(node.getClass()));
    // return true;
  }

  /**
   * Gets the child nodes and strips off any nodes that are part of the definition of the parent node, such as the name of the class or what it extends.
   *
   * @param node
   * @return
   */
  private List<Node> getMethodChildNodes(Node node) {
    return node.getChildNodes().stream().filter(n -> BlockStmt.class.isAssignableFrom(n.getClass())).findFirst().map(Node::getChildNodes)
        .orElse(new ArrayList<>());
  }

  /**
   * Returns an index (integer) within existingNodes or -1 if it does not exist, so that we can set the existingIndex to that value if it's higher.
   *
   * @param existingNodes The nodes to check if one is equal to the insertNode.
   * @param insertNode The node to check.
   * @return The index of the node equal to the insertNode if it exists, -1 otherwise.
   */
  private int findEqualNode(List<Node> existingNodes, Node insertNode) {
    int startIndex = 0;
    return findEqualNode(existingNodes, insertNode, startIndex);
  }

  /**
   * Returns an index (integer) within existingNodes or -1 if it does not exist, so that we can set the existingIndex to that value if it's higher.
   *
   * @param existingNodes The nodes to check if one is equal to the insertNode.
   * @param insertNode The node to check.
   * @param startIndex The index from which to start the search in the existing nodes. This can be used if code needs to be inserted in order.
   * @return The index of the node equal to the insertNode if it exists, -1 otherwise.
   */
  private int findEqualNode(List<Node> existingNodes, Node insertNode, int startIndex) {
    if (existingNodes.size() <= startIndex || startIndex < 0) {
      return -1;
    }
    for (int index = startIndex; index < existingNodes.size(); index++) {
      Node existingNode = existingNodes.get(index);
      if (equalityChecker.compare(existingNode, insertNode) == 0) {
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
    int compare = equalityChecker.compare(existingNodes.get(Integer.max(0, index)), insertNode);

    // While the current index is before the insertIndex
    while (compare < 0 && index < existingNodes.size() - 1) {
      // compare the next existing node
      compare = equalityChecker.compare(existingNodes.get(index + 1), insertNode);
      // only increment if next existing node should be placed before insertNode
      if (compare < 0) {
        index++;
      }
    }
    return index;
  }

}
