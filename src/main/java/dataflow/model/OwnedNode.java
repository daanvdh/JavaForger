/*
 * Copyright (c) 2019 by Eyefreight BV (www.eyefreight.com). All rights reserved.
 *
 * This software is provided by the copyright holder and contributors "as is" and any express or implied warranties, including, but
 * not limited to, the implied warranties of merchantability and fitness for a particular purpose are disclaimed. In no event shall
 * Eyefreight BV or contributors be liable for any direct, indirect, incidental, special, exemplary, or consequential damages
 * (including, but not limited to, procurement of substitute goods or services; * loss of use, data, or profits; or business
 * interruption) however caused and on any theory of liability, whether in contract, strict liability, or tort (including
 * negligence or otherwise) arising in any way out of the use of this software, even if advised of the possibility of such damage.
 */
package dataflow.model;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import com.github.javaparser.ast.Node;

/**
 * Interface for {@link DataFlowGraph} classes that own one or more {@link DataFlowNode}s. This interface contains a self reference since a node can be owned by
 * a {@link DataFlowMethod}, and that method can then be owned by a graph.
 *
 * @author Daan
 */
public abstract class OwnedNode<T extends Node> extends NodeRepresenter<T> {

  protected OwnedNode() {
    super();
  }

  public OwnedNode(T representedNode) {
    super(representedNode);
  }

  public OwnedNode(String name, T representedNode) {
    super(name, representedNode);
  }

  public OwnedNode(NodeRepresenter.Builder<T, ?> builder) {
    super(builder);
  }

  public OwnedNode(String name) {
    super(name);
  }

  /**
   * True when this owner is either a direct owner or is an indirect owner of the input node.
   *
   * @param node The {@link OwnedNode} to check if it's owned by this.
   * @return true if this owns it, false otherwise.
   */
  public boolean owns(DataFlowNode node) {
    return getOwnedNodes().contains(node);
  }

  /**
   * Gets all direct or indirectly owned nodes.
   *
   * @return {@link Set} of {@link OwnedNode}.
   */
  // TODO this has to be made abstract.
  public Set<DataFlowNode> getOwnedNodes() {
    // TODO the idea is to not have one list of nodes in DFM containing everything, but to let other owners like NodeCall, ParameterList and later "FlowBlock"
    // (representing BlockStatement) have a list of nodes of their own. Then recursively get all owned nodes of a specific OwnedNode via this method.
    return Collections.emptySet();
  }

  /**
   * @return An optional of the {@link OwnedNode} of this node. The optional will be empty in case of a {@link DataFlowGraph} representing a non inner class or
   *         a method for which the rest of the graph was not parsed.
   */
  public abstract Optional<OwnedNode<?>> getOwner();

}
