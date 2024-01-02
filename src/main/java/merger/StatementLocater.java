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
import java.util.List;
import java.util.Optional;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.Statement;

/**
 * This class is responsible for determining how to merge one {@link JavaParser} {@link Statement} into another {@link Statement}, by going through the
 * childNodes and finding equal nodes. This class is typically used if the {@link CodeSnippetLocater} was not able to determine an exact location for a given
 * {@link Node}, but was able to find a very similar {@link Node}. The following is not a 100% sure but can help in understanding: The
 * {@link CodeSnippetLocater} locates code on a line level (everything ending with a semicolon) and the {@link StatementLocater} locates everything on a
 * sub-line level (method calls, expressions etc.)
 */
public class StatementLocater {

  private NodeEqualityChecker equalityChecker = new NodeEqualityChecker();

  /**
   * Construct an {@link InsertionMap} containing the {@link CodeSnippetLocation}s for how to merge the from {@link Node} into the to {@link Node}.
   * 
   * @param from The {@link Node} to locate inside the to {@link Node}
   * @param to The {@link Node} in which locations are to be found to insert the 'to' {@link Node}.
   * @return {@link InsertionMap}
   */

  public InsertionMap locate(Node from, Node to) {
    InsertionMap locationMap = new InsertionMap();
    locate(from, to, locationMap);
    return locationMap;
  }

  // TODO we need to make the return type a InsertionEntry that we can insert directly into the InsertionMap when needed.
  public CodeSnippetLocation locate(Node from, Node to, InsertionMap locationMap) {

    if (locationMap.containsFromNode(from)) {
      // location was already found, no need to look further.
      return null;
    }
    Optional<CodeSnippetLocation> locationFromToNode = locationMap.getLocationFromToNode(to);
    if (locationFromToNode.isPresent()) {
      // If further up in the tree we can also not find a location, this is the first place we can put it.
      return CodeSnippetLocation.after(locationFromToNode.get());
    }

    List<Node> fromChildNodes = from.getChildNodes();
    List<Node> toChildNodes = to.getChildNodes();

    if (!fromChildNodes.isEmpty()) {
      // First go all the way down the tree for the 'from' Node
      for (Node f : getUnhandledChildNodes(from)) {
        locate(f, to, locationMap);

        if (!locationMap.containsFromNode(f)) {

          CodeSnippetLocation firstPossibleLocationForF = null; // This needs to come as output from the locate(from, to, map) function

          if (firstPossibleLocationForF == null) {
            // We need to set the first possible location
            // firstPossibleLocationForF = CodeSnippetLocation;
          }

          return firstPossibleLocationForF;
        } else {
          // return locationMap;
          return null;
        }

      }

      // At this point we went all the way down for node from, we either

    } else if (!toChildNodes.isEmpty()) {
      // secondly go all the way down the tree for the 'to' Node
      for (Node t : getUnhandledChildNodes(from)) {
        locate(from, t, locationMap);
        // TODO start from here:

        // if locationMap.containsFromNode(t)
        if (!locationMap.containsFromNode(from)) {
          // check on this level
          if (equalityChecker.isEqualIgnoreChildren(from, t)) {
            // locationMap.put();
          }

        } else {
          // return locationMap;
          return null;
        }
      }
    } else {
      if (from instanceof SimpleName && to instanceof SimpleName) {
        locationMap.put(CodeSnippetLocation.of(from), CodeSnippetLocation.of(to));
        // return locationMap;
        return null;
      }
    }

    if (from instanceof ReturnStmt && to instanceof ReturnStmt) {
      locationMap = handleReturnStmt(from, to, fromChildNodes, toChildNodes);
    } else if (from instanceof MethodCallExpr && to instanceof MethodCallExpr) {
      handleMethodCallExpr(from, to, fromChildNodes, toChildNodes);
    } else if (from instanceof SimpleName && to instanceof SimpleName) {
      locationMap.put(CodeSnippetLocation.of(from), CodeSnippetLocation.of(to));
    } else if (!fromChildNodes.isEmpty()) {

    }

    // ===================================

    int existingIndex = 0, insertIndex = 0;
    List<Node> mergeLineChildNodes = to.getChildNodes();
    while (insertIndex < from.getChildNodes().size()) {
      Node insertSubLine = from.getChildNodes().get(insertIndex);
      Node existingSubLine = mergeLineChildNodes.get(existingIndex);
      if (existingIndex < mergeLineChildNodes.size() && insertIndex < from.getChildNodes().size()) {
        // TODO first analyse the children of the node before analysing the node itself.
        if (equalityChecker.isEqualIgnoreChildren(insertSubLine, existingSubLine)) {
          // TODO This does put a way bigger location on both sides than intended, because all it's children are also included.
          locationMap.put(CodeSnippetLocation.of(insertSubLine), CodeSnippetLocation.of(existingSubLine));
          existingIndex++;
          insertIndex++;
        } else {
          locationMap.put(CodeSnippetLocation.of(insertSubLine), CodeSnippetLocation.before(existingSubLine));
          insertIndex++;
        }
      } else if (insertIndex < from.getChildNodes().size()) {
        locationMap.put(CodeSnippetLocation.of(insertSubLine), CodeSnippetLocation.after(mergeLineChildNodes.get(mergeLineChildNodes.size() - 1)));
        insertIndex++;
      }
    }
    // return locationMap;
    return null;
  }

  private List<Node> getUnhandledChildNodes(Node n) {
    List<Node> children = new ArrayList<>();
    if (n instanceof MethodCallExpr) {
      // TODO: this should probably be conditional, because you can also call a method without specifying an object to call it on,
      // for example b(c) instead of a.b(c)
      children.add(n.getChildNodes().get(0));
    } else if (n instanceof ReturnStmt) {
      children.add(n.getChildNodes().get(0));
    }
    return children;
  }

  private void handleMethodCallExpr(Node from, Node to, List<Node> fromChildNodes, List<Node> toChildNodes) {
    MethodCallExpr fromMethod = (MethodCallExpr) from;
    MethodCallExpr toMethod = (MethodCallExpr) to;
    Node fromInstance = fromMethod.getChildNodes().get(0);
    Node toInstance = toMethod.getChildNodes().get(0);
    SimpleName fromMethodName = fromMethod.getName();
    SimpleName toMethodName = toMethod.getName();
    NodeList<Expression> fromArguments = fromMethod.getArguments();
    NodeList<Expression> toArguments = toMethod.getArguments();

    if (methodNameAndArgumentsEqual(fromMethod, toMethod)) {

      // TODO: this should probably be conditional, because you can also call a method without specifying an object to call it on,
      // for example b(c) instead of a.b(c)
      InsertionMap locate = locate(fromChildNodes.get(0), toChildNodes.get(0));

      if (locate.isEmpty()) {
        // No locations where found, therefore all remaining nodes need to be located.

      }

      CodeSnippetLocation fromLocation = CodeSnippetLocation.fromAfterUntilIncluding(fromInstance, fromMethod);
      CodeSnippetLocation toLocation = CodeSnippetLocation.fromAfterUntilIncluding(toInstance, toMethod);
      locate.put(fromLocation, toLocation);
    } else {
      // recursive call
      InsertionMap locate = locate(fromChildNodes.get(0), toMethod);

    }
  }

  private InsertionMap handleReturnStmt(Node from, Node to, List<Node> fromChildNodes, List<Node> toChildNodes) {
    CodeSnippetLocation fromLocation = CodeSnippetLocation.fromUntil(from, fromChildNodes.get(0));
    CodeSnippetLocation toLocation = CodeSnippetLocation.fromUntil(to, toChildNodes.get(0));
    InsertionMap locate = locate(fromChildNodes.get(0), toChildNodes.get(0));
    locate.put(fromLocation, toLocation);
    return locate;
  }

  private boolean methodNameAndArgumentsEqual(MethodCallExpr fromMethod, MethodCallExpr toMethod) {
    // TODO Auto-generated method stub
    return false;
  }

}
