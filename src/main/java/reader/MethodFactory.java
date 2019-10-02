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
package reader;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.CallableDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AnnotationExpr;

import dataflow.GraphService;
import dataflow.model.DataFlowGraph;
import dataflow.model.DataFlowMethod;
import dataflow.model.DataFlowNode;
import templateInput.definition.FlowReceiverDefinition;
import templateInput.definition.MethodDefinition;
import templateInput.definition.VariableDefinition;

/**
 * Factory for creating {@link MethodDefinition}.
 *
 * @author Daan
 */
public class MethodFactory {
  private static final Logger LOG = LoggerFactory.getLogger(MethodFactory.class);

  private ImportResolver importResolver = new ImportResolver();
  private VariableFactory fieldFactory = new VariableFactory();
  private GraphService graphService = new GraphService();

  public MethodDefinition createMethod(Node node, DataFlowGraph dfg) {
    MethodDeclaration md = (MethodDeclaration) node;
    MethodDefinition method = parseCallable(md).build();
    method.setType(md.getTypeAsString());
    importResolver.resolveImport(md.getType()).forEach(method::addTypeImport);
    if (dfg != null) {
      addChangedFields(method, dfg.getMethod(md));
      addInputMethods(method, dfg.getMethod(md));
    }
    return method;
  }

  public MethodDefinition createConstructor(Node node) {
    ConstructorDeclaration md = (ConstructorDeclaration) node;
    MethodDefinition method = parseCallable(md).build();
    method.setType(md.getNameAsString());
    return method;
  }

  private MethodDefinition.Builder parseCallable(CallableDeclaration<?> md) {
    Set<String> accessModifiers = md.getModifiers().stream().map(Modifier::asString).collect(Collectors.toSet());
    Set<String> annotations = md.getAnnotations().stream().map(AnnotationExpr::getNameAsString).collect(Collectors.toSet());

    return MethodDefinition.builder().name(md.getNameAsString()).accessModifiers(accessModifiers).annotations(annotations)
        .lineNumber(md.getBegin().map(p -> p.line).orElse(-1)).column(md.getBegin().map(p -> p.column).orElse(-1)).parameters(getParameters(md));
  }

  private List<VariableDefinition> getParameters(CallableDeclaration<?> md) {
    LinkedHashMap<Parameter, VariableDefinition> params = new LinkedHashMap<>();
    md.getParameters().stream().forEach(p -> params.put(p, VariableDefinition.builder().name(p.getNameAsString()).type(p.getTypeAsString()).build()));
    params.entrySet().forEach(p -> importResolver.resolveAndSetImport(p.getKey().getType(), p.getValue()));
    List<VariableDefinition> parameters = params.values().stream().collect(Collectors.toList());
    return parameters;
  }

  private void addChangedFields(MethodDefinition newMethod, DataFlowMethod dataFlowMethod) {
    List<DataFlowNode> changedFieldsNodes = dataFlowMethod.getChangedFields();
    List<FlowReceiverDefinition> changedFields = new ArrayList<>();
    for (DataFlowNode dfn : changedFieldsNodes) {
      Node javaParserNode = dfn.getRepresentedNode();
      if (javaParserNode instanceof VariableDeclarator) {
        List<DataFlowNode> receivedNodes = graphService.walkBackUntil(dfn, dataFlowMethod);
        List<String> receivedNames = receivedNodes.stream().map(DataFlowNode::getName).collect(Collectors.toList());
        VariableDefinition field = fieldFactory.createSingle(javaParserNode);
        FlowReceiverDefinition receiver = FlowReceiverDefinition.builder().copy(field).receivedValues(receivedNames).build();
        changedFields.add(receiver);
      } else {
        LOG.error("Cannot add changed field {} to method {} because represented node {} with type {} was not a VariableDeclarator", dfn.getName(),
            newMethod.getName(), javaParserNode, javaParserNode.getClass());
      }
    }
    newMethod.setChangedFields(changedFields);
  }

  private void addInputMethods(MethodDefinition newMethod, DataFlowMethod dataFlowMethod) {
    Collection<DataFlowMethod> inputMethodNodes = dataFlowMethod.getInputMethods();
    for (DataFlowMethod dfm : inputMethodNodes) {

      MethodDefinition.Builder builder = parseCallable(dfm.getRepresentedNode());
      String type = dfm.getReturnNode().map(DataFlowNode::getType).orElse("void");
      builder.name(dfm.getName()).type(type);

      List<DataFlowNode> inputParameters = dfm.getInputParameters().getNodes();
      // TODO walk back in the direction of the given dataFlowMethod
      for (DataFlowNode param : inputParameters) {
        List<DataFlowNode> nodeInMethod = graphService.walkBackUntil(param, dataFlowMethod);
        // Get the list of nodes that are on the edge of the graph, or

        // Returns all [read fields from the class, inputParameters, method return values, nodes that are part of this method] that influence the state
        // of the current parameter.
        List<DataFlowNode> nodeInGraph = graphService.walkBackUntilLastInScopeOfMethod(nodeInMethod, dataFlowMethod);

        // TODO The cast most likely fails now
        VariableDefinition createSingle = fieldFactory.createSingle(param.getRepresentedNode());

        FlowReceiverDefinition flowParam = new FlowReceiverDefinition();
        flowParam.setReceivedValues(nodeInGraph.stream().map(DataFlowNode::getName).collect(Collectors.toList()));

      }

      newMethod.addInputMethod(builder.build());
    }
  }

}
