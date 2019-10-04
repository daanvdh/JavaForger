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

import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * Represents a call to a node (method, constructor or other code block). This node will be owned by the calling method. This class groups all in/output data
 * from one method to another.
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
  /**
   * The return Node of a node call, this will be null if method is void. If the return value is not read, the outgoing edges of this node will be empty. There
   * should only be a single incoming edge from the return node of the called method. This NodeCall is the owner of the returnNode.
   */
  private DataFlowNode returnNode;

  private String claz;
  private String peckage;

  public NodeCall(OwnedNode owner) {
    this.owner = owner;
  }

  private NodeCall(Builder builder) {
    super(builder);
    this.in = builder.in == null ? this.in : builder.in;
    this.out = builder.out == null ? this.out : builder.out;
    this.owner = builder.owner == null ? this.owner : builder.owner;
    this.calledMethod = builder.calledMethod == null ? this.calledMethod : builder.calledMethod;
    this.claz = builder.claz == null ? this.claz : builder.claz;
    this.peckage = builder.peckage == null ? this.peckage : builder.peckage;
    this.returnNode = builder.returnNode == null ? this.returnNode : builder.returnNode;
  }

  @Override
  public Optional<OwnedNode> getOwner() {
    return Optional.of(owner);
  }

  public ParameterList getIn() {
    return in;
  }

  public void setIn(ParameterList in) {
    this.in = in;
  }

  public ParameterList getOut() {
    return out;
  }

  public void setOut(ParameterList out) {
    this.out = out;
  }

  public Optional<DataFlowMethod> getCalledMethod() {
    return Optional.ofNullable(calledMethod);
  }

  public void setCalledMethod(DataFlowMethod calledMethod) {
    this.calledMethod = calledMethod;
    this.in.connectTo(calledMethod.getInputParameters());
  }

  public String getClaz() {
    return claz;
  }

  public void setClaz(String claz) {
    this.claz = claz;
  }

  public String getPeckage() {
    return peckage;
  }

  public void setPeckage(String peckage) {
    this.peckage = peckage;
  }

  public void setOwner(OwnedNode owner) {
    this.owner = owner;
  }

  public Optional<DataFlowNode> getReturnNode() {
    return Optional.ofNullable(returnNode);
  }

  public void setReturnNode(DataFlowNode returnNode) {
    this.returnNode = returnNode;
  }

  /**
   * Creates builder to build {@link NodeCall}.
   *
   * @return created builder
   */
  public static Builder builder() {
    return new Builder();
  }

  @Override
  public boolean equals(Object obj) {
    boolean equals = false;
    if (this == obj) {
      equals = true;
    } else if (obj != null && getClass() == obj.getClass()) {
      NodeCall other = (NodeCall) obj;
      equals = new EqualsBuilder().appendSuper(super.equals(obj)).append(in, other.in).append(out, other.out).append(calledMethod, other.calledMethod)
          .append(claz, other.claz).append(peckage, other.peckage).isEquals();
    }
    return equals;
  }

  @Override
  public int hashCode() {
    return Objects.hash(in, out, calledMethod, claz, peckage);
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).appendSuper(super.toString()).append("in", in).append("out", out)
        .append("calledMethod", calledMethod).append("returnNode", returnNode).append("class", claz).append("package", peckage).build();
  }

  /**
   * Builder to build {@link NodeCall}.
   */
  public static final class Builder extends NodeRepresenter.Builder<NodeCall.Builder> {
    private ParameterList in;
    private ParameterList out;
    private OwnedNode owner;
    private DataFlowMethod calledMethod;
    private String claz;
    private String peckage;
    private DataFlowNode returnNode;

    private Builder() {
      // Builder should only be constructed via the parent class
    }

    public Builder in(ParameterList in) {
      this.in = in;
      return this;
    }

    public Builder out(ParameterList out) {
      this.out = out;
      return this;
    }

    public Builder owner(OwnedNode owner) {
      this.owner = owner;
      return this;
    }

    public Builder calledMethod(DataFlowMethod calledMethod) {
      this.calledMethod = calledMethod;
      return this;
    }

    public Builder claz(String claz) {
      this.claz = claz;
      return this;
    }

    public Builder peckage(String peckage) {
      this.peckage = peckage;
      return this;
    }

    public Builder returnNode(DataFlowNode node) {
      this.returnNode = node;
      return this;
    }

    public NodeCall build() {
      return new NodeCall(this);
    }

  }

}
