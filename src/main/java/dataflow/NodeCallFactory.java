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
package dataflow;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedMethodLikeDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedParameterDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;

import dataflow.model.DataFlowGraph;
import dataflow.model.DataFlowMethod;
import dataflow.model.DataFlowNode;
import dataflow.model.NodeCall;
import dataflow.model.ParameterList;

/**
 * Class for resolving {@link DataFlowMethod}s and {@link DataFlowNode}s from {@link Node}s and {@link DataFlowGraph}s.
 *
 * @author Daan
 */
public class NodeCallFactory {
  private static final Logger LOG = LoggerFactory.getLogger(NodeCallFactory.class);

  private ParserUtil parserUtil = new ParserUtil();

  public Optional<NodeCall> createNodeCall(DataFlowGraph graph, DataFlowMethod method, MethodCallExpr node) {
    Object resolved = parserUtil.resolve(method, node);

    NodeCall resolvedMethod = null;
    if (resolved instanceof ResolvedMethodLikeDeclaration) {
      resolvedMethod = createMethodCall(method, resolved, node);
    } else {
      LOG.warn("In method {}, resolving is not supported for node {} of type {}", method.getName(), node, resolved == null ? null : resolved.getClass());
    }
    return Optional.ofNullable(resolvedMethod);
  }

  private NodeCall createMethodCall(DataFlowMethod method, Object resolved, MethodCallExpr node) {
    ResolvedMethodLikeDeclaration rmd = (ResolvedMethodLikeDeclaration) resolved;
    NodeCall methodCall = NodeCall.builder().name(rmd.getName()).claz(rmd.getClassName()).peckage(rmd.getPackageName()).owner(method).representedNode(node)
        .build();
    if (rmd.getNumberOfParams() > 0) {
      ParameterList params = ParameterList.builder().build();
      for (int i = 0; i < rmd.getNumberOfParams(); i++) {
        ResolvedParameterDeclaration p = rmd.getParam(i);
        DataFlowNode newNode = DataFlowNode.builder().name(p.getName()).type(p.getType().describe().toString()).representedNode(node.getArgument(i))
            .owner(params).build();
        params.add(newNode);
      }
      methodCall.setIn(params);
    }

    if (rmd instanceof ResolvedMethodDeclaration) {
      ResolvedType returnType = ((ResolvedMethodDeclaration) rmd).getReturnType();
      if (!returnType.isVoid()) {
        DataFlowNode returnNode = DataFlowNode.builder().name("nodeCall_" + methodCall.getName() + "_return").representedNode(node).build();
        methodCall.setReturnNode(returnNode);
      }
    } else {
      LOG.warn("Not supported to create return node in NodeCall from resolved node of type {} in method {}", rmd.getClass(), method.getName());
    }

    return methodCall;
  }

}
