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

import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithSimpleName;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;

import dataflow.model.DataFlowEdge;
import dataflow.model.DataFlowGraph;
import dataflow.model.DataFlowMethod;
import dataflow.model.DataFlowNode;
import dataflow.model.NodeCall;

/**
 * Class for handling {@link JavaParser} {@link Node}s while filling a {@link DataFlowMethod}.
 *
 * @author Daan
 */
public class MethodNodeHandler {
  private static final Logger LOG = LoggerFactory.getLogger(MethodNodeHandler.class);

  private NodeCallFactory resolver = new NodeCallFactory();
  private ParserUtil parserUtil = new ParserUtil();

  /**
   * Recursively creates new {@link DataFlowNode} or finds existing ones and creates {@link DataFlowEdge} between those nodes if needed. This is done within the
   * scope of a single method. This method assumes all methods to already exist in the {@link DataFlowGraph}, including the {@link DataFlowNode}s for the input
   * parameters and return value. If external method calls are done, {@link NodeCall}s representing them will also be created.
   *
   * @param graph            {@link DataFlowGraph}
   * @param method           {@link DataFlowMethod} to add {@link DataFlowNode} to
   * @param overriddenValues The values that have been overridden in previous iterations.
   * @param n                The {@link Node} to handle. ChildNodes will recursively be handled if needed.
   * @return An optional of the {@link DataFlowNode} of the input node. If multiple head nodes are created, (In case of a {@link BlockStmt}) the optional will
   *         be empty.
   */
  public Optional<DataFlowNode> handleNode(DataFlowGraph graph, DataFlowMethod method, Map<Node, DataFlowNode> overriddenValues, Node n) {
    Optional<DataFlowNode> created = Optional.empty();
    if (n instanceof BlockStmt) {
      created = handleBlockStmt(graph, method, overriddenValues, (BlockStmt) n);
    } else if (n instanceof ExpressionStmt) {
      created = handleExpressionStmt(graph, method, overriddenValues, (ExpressionStmt) n);
    } else if (n instanceof AssignExpr) {
      created = handleAssignExpr(graph, method, overriddenValues, (AssignExpr) n);
    } else if (n instanceof ReturnStmt) {
      created = handleReturnStmt(graph, method, overriddenValues, (ReturnStmt) n);
    } else if (n instanceof NameExpr) {
      created = handleNameExpr(graph, method, overriddenValues, (NameExpr) n);
    } else if (n instanceof MethodCallExpr) {
      created = handleMethodCallExpr(graph, method, overriddenValues, (MethodCallExpr) n);
    } else if (n instanceof VariableDeclarationExpr) {
      created = handleVariableDeclarationExpr(graph, method, overriddenValues, (VariableDeclarationExpr) n);
    } else if (n instanceof VariableDeclarator) {
      created = handleVariableDeclarator(graph, method, overriddenValues, (VariableDeclarator) n);
    } else {
      LOG.warn("In method {} could not handle node [{}] of type {}", method.getName(), n, n.getClass());
    }
    return created;
  }

  private Optional<DataFlowNode> handleVariableDeclarator(DataFlowGraph graph, DataFlowMethod method, Map<Node, DataFlowNode> overriddenValues,
      VariableDeclarator n) {
    DataFlowNode created = method.createAndAddNode(n.getNameAsString(), n);
    Optional<Expression> initializer = n.getInitializer();
    if (initializer.isPresent()) {
      Optional<DataFlowNode> assigner = handleNode(graph, method, overriddenValues, initializer.get());
      if (assigner.isPresent()) {
        assigner.get().addEdgeTo(created);
      } else {
        LOG.warn("In method {} was not able to resolve {} of type {}", method.getName(), initializer.get(), initializer.get().getClass());
      }
    }

    return Optional.ofNullable(created);
  }

  private Optional<DataFlowNode> handleVariableDeclarationExpr(DataFlowGraph graph, DataFlowMethod method, Map<Node, DataFlowNode> overriddenValues,
      VariableDeclarationExpr n) {
    NodeList<VariableDeclarator> variables = n.getVariables();
    for (VariableDeclarator vd : variables) {
      handleNode(graph, method, overriddenValues, vd);
    }
    return Optional.empty();
  }

  private Optional<DataFlowNode> handleMethodCallExpr(DataFlowGraph graph, DataFlowMethod method, Map<Node, DataFlowNode> overriddenValues, MethodCallExpr n) {
    Optional<NodeCall> optionalCalledMethod = resolver.createNodeCall(graph, method, n);
    if (!optionalCalledMethod.isPresent()) {
      LOG.warn("In method {} could not resolve method call {} of type {}", method.getName(), n, n.getClass());
      return Optional.empty();
    }
    NodeCall calledMethod = optionalCalledMethod.get();
    method.addMethodCall(calledMethod);

    NodeList<Expression> arguments = n.getArguments();
    if ((arguments.size() > 0 && calledMethod.getIn() == null) || arguments.size() != calledMethod.getIn().getNodes().size()) {
      LOG.warn("In method {} for called method {} the used nof arguments {} is not equal to the expected nof arguments {}", method.getName(),
          calledMethod.getName(), arguments, calledMethod.getIn());
      return Optional.empty();
    }

    // Connect to NodeCall arguments
    for (int i = 0; i < arguments.size(); i++) {
      Optional<DataFlowNode> arg = handleNode(graph, method, overriddenValues, arguments.get(i));
      if (arg.isPresent()) {
        arg.get().addEdgeTo(calledMethod.getIn().getNodes().get(i));
      }
    }
    
    calledMethod.getReturnNode().ifPresent(method::addNode);

    // Return the return node of the called method so that the return value can be assigned to the caller.
    return calledMethod.getReturnNode();
  }

  private Optional<DataFlowNode> handleBlockStmt(DataFlowGraph graph, DataFlowMethod method, Map<Node, DataFlowNode> overriddenValues, BlockStmt node) {
    for (Node n : node.getChildNodes()) {
      handleNode(graph, method, overriddenValues, n);
    }
    return Optional.empty();
  }

  private Optional<DataFlowNode> handleReturnStmt(DataFlowGraph graph, DataFlowMethod method, Map<Node, DataFlowNode> overriddenValues, ReturnStmt n) {
    DataFlowNode createdReturn = null;
    if (n.getExpression().isPresent()) {
      Expression expression = n.getExpression().get();
      Optional<DataFlowNode> assignToReturn = handleNode(graph, method, overriddenValues, expression);

      if (assignToReturn.isPresent()) {
        createdReturn = method.createAndAddNode(method.getName() + "_return_" + n.getBegin().map(t -> "line" + t.line + "_col" + t.column).orElse("?"), n);
        assignToReturn.get().addEdgeTo(createdReturn);
        if (method.getReturnNode().isPresent()) {
          createdReturn.addEdgeTo(method.getReturnNode().get());
        } else {
          throw new DataFlowException("Expected the method %s for which the return statement %s is handled to already have a return node", method, n);
        }
      } else {
        LOG.warn("In method {} could not find node for assigning to the return value for node {} of type {}", method.getName(), expression,
            expression.getClass());
      }
    }
    return Optional.ofNullable(createdReturn);
  }

  /**
   * Only gets an existing node for the given {@link NameExpr}.
   */
  private Optional<DataFlowNode> handleNameExpr(DataFlowGraph graph, DataFlowMethod method, Map<Node, DataFlowNode> overriddenValues, NameExpr n) {
    return getDataFlowNode(graph, method, overriddenValues, n);
  }

  private Optional<DataFlowNode> handleExpressionStmt(DataFlowGraph graph, DataFlowMethod method, Map<Node, DataFlowNode> overriddenValues, ExpressionStmt n) {
    for (Node c : n.getChildNodes()) {
      handleNode(graph, method, overriddenValues, c);
    }
    return Optional.empty();
  }

  private Optional<DataFlowNode> handleAssignExpr(DataFlowGraph graph, DataFlowMethod method, Map<Node, DataFlowNode> overriddenValues, AssignExpr expr) {
    Expression assignedJP = expr.getTarget();
    Expression assignerJP = expr.getValue();
    Optional<Node> optionalRealAssignedJP = parserUtil.getJavaParserNode(method, assignedJP);
    Optional<DataFlowNode> assignerDF = getDataFlowNode(graph, method, overriddenValues, assignerJP);

    if (!optionalRealAssignedJP.isPresent()) {
      // Logging is already done in the method call.
      return Optional.empty();
    }
    if (!(assignedJP instanceof NodeWithSimpleName)) {
      LOG.warn("Not able to create a new DFN if the assigned node does not implement NodeWithSimpleName, for node {}", assignedJP);
      return Optional.empty();
    }
    if (!assignerDF.isPresent()) {
      // Logging is already done in the method call.
      return Optional.empty();
    }

    Node realAssignedJP = optionalRealAssignedJP.get();
    String name = nameForInBetweenNode(method, overriddenValues, realAssignedJP, (NodeWithSimpleName<?>) assignedJP);
    DataFlowNode flowNode = method.createAndAddNode(name, expr);
    if (isField(realAssignedJP)) {
      // This is the version of the field that will receive the assigner edge.
      // If this is the last assignment to the field, an edge to the original field will be created.
      overriddenValues.put(realAssignedJP, flowNode);
    }

    assignerDF.get().addEdgeTo(flowNode);
    return Optional.of(flowNode);
  }
  /**
   * TODO javadoc
   *
   * @param graph
   * @param method
   * @param overwriddenValues
   * @param node
   * @return
   */
  private Optional<DataFlowNode> getDataFlowNode(DataFlowGraph graph, DataFlowMethod method, Map<Node, DataFlowNode> overwriddenValues, Node node) {
    Optional<Node> optionalResolvedNode = parserUtil.getJavaParserNode(method, node);
    DataFlowNode flowNode = null;
    if (optionalResolvedNode.isPresent()) {
      Node resolvedNode = optionalResolvedNode.get();
      flowNode = overwriddenValues.get(resolvedNode);
      flowNode = flowNode != null ? flowNode : graph.getNode(resolvedNode);
      flowNode = flowNode != null ? flowNode : method.getNode(resolvedNode);
    }
    if (flowNode == null) {
      LOG.warn("In method {} did not resolve the type of node {} of type {}", method.getName(), node, node.getClass());
    }
    return Optional.ofNullable(flowNode);
  }

  private String nameForInBetweenNode(DataFlowMethod method, Map<Node, DataFlowNode> overriddenValues, Node realAssignedJP,
      NodeWithSimpleName<?> nodeWithName) {
    String namePostFix = "";
    if (overriddenValues.containsKey(realAssignedJP)) {
      DataFlowNode overridden = overriddenValues.get(realAssignedJP);
      String stringNumber = overridden.getName().substring(overridden.getName().lastIndexOf("."));
      namePostFix = StringUtils.isNumeric(stringNumber) ? "." + (new Integer(stringNumber) + 1) : ".2";
    }

    // Make the name unique for multiple assignments to the same variable
    return method.getName() + "." + nodeWithName.getNameAsString() + namePostFix;
  }

  private boolean isField(Node representedNode) {
    boolean isField = false;
    if (representedNode instanceof VariableDeclarator) {
      VariableDeclarator vd = (VariableDeclarator) representedNode;
      if (vd.getParentNode().isPresent() && vd.getParentNode().get() instanceof FieldDeclaration) {
        isField = true;
      }
    }
    return isField;
  }

}
