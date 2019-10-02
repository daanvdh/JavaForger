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

import java.util.Optional;

/**
 * A call from
 *
 * @author Daan
 */
public class NodeCall extends OwnedNode {

  /**
   * The {@link ParameterList}s that contain the {@link DataFlowNode}s that where used for a specific {@link DataFlowMethod} call to the owner
   * {@link DataFlowMethod}.
   */
  private ParameterList in;
  /** The {@link ParameterList}s that contain the {@link DataFlowNode}s that where used as input for a call to another {@link DataFlowMethod}. */
  private ParameterList out;
  /** The method/constructor/codeBlock from which the method is called */
  private OwnedNode owner;
  /** The called method, this can be null in case that the given method is not parsed. */
  private DataFlowMethod calledMethod;

  public NodeCall(OwnedNode owner) {
    this.owner = owner;
  }

  @Override
  public Optional<OwnedNode> getOwner() {
    return Optional.of(owner);
  }

}
