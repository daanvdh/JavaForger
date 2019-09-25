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

import java.util.EnumSet;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.CallableDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.resolution.Resolvable;
import com.github.javaparser.resolution.declarations.ResolvedParameterDeclaration;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserFieldDeclaration;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserMethodDeclaration;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserParameterDeclaration;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserSymbolDeclaration;
import com.github.javaparser.symbolsolver.reflectionmodel.ReflectionMethodDeclaration;

/**
 * Class for resolving {@link DataFlowMethod}s and {@link DataFlowNode}s from {@link Node}s and {@link DataFlowGraph}s.
 *
 * @author Daan
 */
public class DataFlowResolver {
  private static final Logger LOG = LoggerFactory.getLogger(DataFlowResolver.class);

  public Optional<DataFlowNode> getDataFlowNode(DataFlowGraph graph, DataFlowMethod method, Map<Node, DataFlowNode> overwriddenValues, Node node) {
    Optional<Node> optionalResolvedNode = getJavaParserNode(method, node);
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

  public Optional<DataFlowMethod> getDataFlowMethod(DataFlowGraph graph, DataFlowMethod method, MethodCallExpr node) {
    Object resolved = resolve(method, node);

    DataFlowMethod resolvedMethod = null;
    if (resolved instanceof JavaParserMethodDeclaration) {
      MethodDeclaration resolvedNode = ((JavaParserMethodDeclaration) resolved).getWrappedNode();
      resolvedMethod = graph.getMethod(resolvedNode);
    } else if (resolved instanceof ReflectionMethodDeclaration) {
      resolvedMethod = getOrCreateExternalMethod(graph, resolved);
    } else {
      LOG.warn("In method {}, resolving is not supported for node {} of type {}", method.getName(), node, resolved == null ? null : resolved.getClass());
    }
    return Optional.ofNullable(resolvedMethod);
  }

  private DataFlowMethod getOrCreateExternalMethod(DataFlowGraph graph, Object resolved) {
    ReflectionMethodDeclaration rmd = (ReflectionMethodDeclaration) resolved;
    DataFlowGraph dependedGraph = getGraph(graph, rmd);
    if (dependedGraph == null) {
      dependedGraph = createGraph(graph, rmd);
    }
    CallableDeclaration<?> methodName = new MethodDeclaration(EnumSet.of(Modifier.PUBLIC), new ClassOrInterfaceType(), rmd.getQualifiedSignature());
    DataFlowMethod resolvedMethod = dependedGraph.getMethod(methodName);
    if (resolvedMethod == null) {
      resolvedMethod = createMethod(rmd, methodName);
      dependedGraph.addMethod(resolvedMethod);
    }
    return resolvedMethod;
  }

  private DataFlowGraph getGraph(DataFlowGraph graph, ReflectionMethodDeclaration rmd) {
    String path = rmd.getQualifiedName();
    path = path.substring(0, path.lastIndexOf("."));
    DataFlowGraph dependedGraph = graph.getDependedGraph(path);
    return dependedGraph;
  }

  private DataFlowGraph createGraph(DataFlowGraph graph, ReflectionMethodDeclaration rmd) {
    DataFlowGraph dependedGraph;
    dependedGraph = DataFlowGraph.builder().name(rmd.getClassName()).classPackage(rmd.getPackageName()).build();
    graph.addDependedGraph(dependedGraph);
    return dependedGraph;
  }

  private DataFlowMethod createMethod(ReflectionMethodDeclaration rmd, CallableDeclaration<?> methodName) {
    DataFlowMethod newMethod;
    newMethod = DataFlowMethod.builder().name(rmd.getName()).representedNode(methodName).build();
    for (int i = 0; i < rmd.getNumberOfParams(); i++) {
      ResolvedParameterDeclaration p = rmd.getParam(i);
      DataFlowNode newNode = DataFlowNode.builder().name(p.getName()).type(p.getType().describe().toString()).build();
      newMethod.addParameter(newNode);
    }
    return newMethod;
  }

  private Optional<Node> getJavaParserNode(DataFlowMethod method, Node node) {
    Object resolved = resolve(method, node);
    Node resolvedNode = null;
    if (resolved instanceof JavaParserFieldDeclaration) {
      resolvedNode = ((JavaParserFieldDeclaration) resolved).getVariableDeclarator();
    } else if (resolved instanceof JavaParserParameterDeclaration) {
      resolvedNode = ((JavaParserParameterDeclaration) resolved).getWrappedNode();
    } else if (resolved instanceof JavaParserSymbolDeclaration) {
      resolvedNode = ((JavaParserSymbolDeclaration) resolved).getWrappedNode();
    } else {
      LOG.warn("In method {}, resolving is not supported for node {} of type {}", method.getName(), node, resolved == null ? null : resolved.getClass());
    }
    return Optional.ofNullable(resolvedNode);
  }

  private Object resolve(DataFlowMethod method, Node node) {
    if (!Resolvable.class.isAssignableFrom(node.getClass())) {
      LOG.warn("In method {}, node is not Resolvable for expression {} of type {}", method.getName(), node, node.getClass());
      return null;
    }

    Resolvable<?> resolvable = (Resolvable<?>) node;
    Object resolved = null;
    try {
      resolved = resolvable.resolve();
    } catch (Exception e) {
      LOG.warn(e.getMessage());
    }
    return resolved;
  }

}
