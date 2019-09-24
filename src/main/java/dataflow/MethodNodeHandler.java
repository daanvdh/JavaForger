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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.javaparser.JavaParser;
import com.github.javaparser.Position;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;

/**
 * Class for handling {@link JavaParser} {@link Node}s while filling a {@link DataFlowMethod}.
 *
 * @author Daan
 */
public class MethodNodeHandler {
  private static final Logger LOG = LoggerFactory.getLogger(MethodNodeHandler.class);

  private DataFlowResolver resolver = new DataFlowResolver();

  /**
   * Recursively handles the input {@link Node}s. This method assumes all methods to already exist in the {@link DataFlowGraph}, including the
   * {@link DataFlowNode}s for the input parameters and return value. Other {@link DataFlowNode}s will be added to the input {@link DataFlowMethod} if needed.
   * If external method calls are done and they do not exist in the graph yet, they will be created.
   *
   * @param graph {@link DataFlowGraph}
   * @param method {@link DataFlowMethod} to add {@link DataFlowNode} to
   * @param overriddenValues The values that have been overridden in previous iterations.
   * @param n The {@link Node} to handle. ChildNodes will recursively be handled if needed.
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
    DataFlowNode created = method.addNode(n.getNameAsString(), n);
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
    Optional<DataFlowMethod> optionalCalledMethod = resolver.getDataFlowMethod(graph, method, n);
    if (!optionalCalledMethod.isPresent()) {
      LOG.warn("In method {} could not resolve method call {} of type {}", method.getName(), n, n.getClass());
      return Optional.empty();
    }
    DataFlowMethod calledMethod = optionalCalledMethod.get();

    NodeList<Expression> arguments = n.getArguments();
    if (arguments.size() != calledMethod.getInputParameters().size()) {
      LOG.warn("In method {} for called method {} the used nof arguments {} is not equal to the expected nof arguments {}", method.getName(), arguments.size(),
          calledMethod.getInputParameters().size());
      return Optional.empty();
    }

    for (int i = 0; i < arguments.size(); i++) {
      Optional<DataFlowNode> arg = handleNode(graph, method, overriddenValues, arguments.get(i));
      if (arg.isPresent()) {
        arg.get().addEdgeTo(calledMethod.getInputParameters().get(i));
      }
    }

    if (n.getParentNode().isPresent()) {
      if (n.getParentNode().get() instanceof ReturnStmt) {
        method.addInputMethod(calledMethod);
      } else if (n.getParentNode().get() instanceof AssignExpr) {
        method.addInputMethod(calledMethod);
        // TODO these are not the only cases that we need to handle.
        // TODO make sure that we indeed need to add the input method in this case.
      } else {
        method.addOutputMethod(calledMethod);
      }
    }

    // Return the return node of the called method so that the return value can be assigned to the caller.
    return Optional.ofNullable(calledMethod.getReturnNode());
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
        createdReturn = method.addNode(method.getName() + ".return" + n.getBegin().map(Position::toString).orElse("?"), n);
        assignToReturn.get().addEdgeTo(createdReturn);
        createdReturn.addEdgeTo(method.getReturnNode());
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
    return resolver.getDataFlowNode(graph, method, overriddenValues, n);
  }

  private Optional<DataFlowNode> handleExpressionStmt(DataFlowGraph graph, DataFlowMethod method, Map<Node, DataFlowNode> overriddenValues, ExpressionStmt n) {
    for (Node c : n.getChildNodes()) {
      handleNode(graph, method, overriddenValues, c);
    }
    return null;
  }

  private Optional<DataFlowNode> handleAssignExpr(DataFlowGraph graph, DataFlowMethod method, Map<Node, DataFlowNode> overriddenValues, AssignExpr expr) {
    Expression assignedJP = expr.getTarget();
    Expression assignerJP = expr.getValue();
    // This is the original field
    Optional<DataFlowNode> assignedDF = resolver.getDataFlowNode(graph, method, overriddenValues, assignedJP);
    Optional<DataFlowNode> assignerDF = resolver.getDataFlowNode(graph, method, overriddenValues, assignerJP);
    if (assignedDF.isPresent() && assignerDF.isPresent()) {
      // This is the version of the field that will receive the assigner edge.
      // If this is the last assignment to the field, an edge to the original field will be created.
      DataFlowNode flowNode = method.addNode(assignedJP.toString(), assignedJP);
      if (isField(assignedDF.get())) {
        overriddenValues.put(assignedDF.get().getRepresentedNode(), flowNode);
      }

      flowNode.setName(method.getName() + "." + assignedDF.get().getName());

      assignerDF.get().addEdgeTo(flowNode);
    }
    return null;
  }

  private boolean isField(DataFlowNode assignedDF) {
    // TODO handle the fact that this can represent a field in an in-between state, in that case we have to walk back to figure that out.
    // The question is how far do we have to walk back...
    Node representedNode = assignedDF.getRepresentedNode();
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
