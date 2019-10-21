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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
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
import dataflow.model.NodeCall;
import dataflow.model.ParameterList;
import templateInput.definition.FlowReceiverDefinition;
import templateInput.definition.MethodDefinition;
import templateInput.definition.MethodDefinition.Builder;
import templateInput.definition.VariableDefinition;

/**
 * Factory for creating {@link MethodDefinition}.
 *
 * @author Daan
 */
public class MethodDefinitionFactory {
  private static final Logger LOG = LoggerFactory.getLogger(MethodDefinitionFactory.class);

  private ImportResolver importResolver = new ImportResolver();
  private VariableDefintionFactory fieldFactory = new VariableDefintionFactory();
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
    for (NodeCall call : dataFlowMethod.getCalledMethods()) {
      // If method call has a return node and it's return node is read
      if (call.getReturnNode().map(t -> !t.getOut().isEmpty()).orElse(false)) {
        addInputMethod(newMethod, dataFlowMethod, call);
      }
    }
  }

  private void addInputMethod(MethodDefinition newMethod, DataFlowMethod dataFlowMethod, NodeCall call) {
    Builder builder = call.getCalledMethod().map(DataFlowMethod::getRepresentedNode).map(this::parseCallable).orElse(MethodDefinition.builder());
    String type = call.getReturnNode().map(DataFlowNode::getType).orElse("void");
    builder.name(call.getName()).type(type);

    List<DataFlowNode> inputParameters = call.getIn().map(ParameterList::getNodes).orElse(new ArrayList<>());
    StringBuilder callSignature = new StringBuilder();
    callSignature.append(call.getName()).append("(");
    boolean first = true;
    for (DataFlowNode param : inputParameters) {
      if (!first) {
        callSignature.append(", ");
      } else {
        first = false;
      }
      String inputName = getInputName(dataFlowMethod, param);
      callSignature.append(inputName);
    }
    callSignature.append(")");

    MethodDefinition method = null;
    if (call.getCalledMethod().isPresent()) {
      // TODO fuck private methods for now, just add them.
      method = parseCallable(call.getCalledMethod().get().getRepresentedNode()).build();
      // TODO is this needed?
      // String type = dfm.getReturnNode().map(DataFlowNode::getType).orElse("void");
      // method.setName(dfm.getName());
      // method.setType(type);
    } else {
      method = MethodDefinition.builder().type(call.getClaz()).build();
    }

    // TODO fix this
    method.setReturnSignature("ret");
    method.setCallSignature(callSignature.toString());

    newMethod.addInputMethod(method);

    // If it's not a class field or method parameter, or return value from another method. It must be defined within the method itself, we therefore need
    // to define it in test data as well

    // get the input for
  }

  private String getInputName(DataFlowMethod dataFlowMethod, DataFlowNode param) {
    Predicate<DataFlowNode> isField = DataFlowNode::isField;
    Predicate<DataFlowNode> isParam = DataFlowNode::isInputParameter;
    // TODO this logic needs to be in the DFN itself
    Predicate<DataFlowNode> isMethodReturn = n -> n.getOwner() //
        // if it's a return node from a nodeCall (that is the only type of node directly owned by a NodeCall)
        .filter(o -> NodeCall.class.isAssignableFrom(o.getClass())).map(NodeCall.class::cast)
        // if the method is not present it is not parsed and is therefore part of another class.
        .filter(t -> !t.getCalledMethod().isPresent())
        // If the method is present but the owning class is different than the current class.
        .filter(t -> !t.getCalledMethod().get().getOwner().equals(dataFlowMethod.getOwner()))
        // If it's still present after the filters it is a method return node.
        .isPresent();

    List<DataFlowNode> inputNodes = graphService.walkBackUntil(param, isField.or(isParam).or(isMethodReturn));
    // TODO construct the call signature for this method
    // TODO construct the return signature for this method
    String returnSignature = "returnSignature";

    StringBuilder sb = new StringBuilder();
    boolean first = true;
    for (DataFlowNode inputNode : inputNodes) {
      if (!first) {
        sb.append("_");
      } else {
        first = false;
      }

      if (param.isField()) {
        // TODO handle field: field must be set inside the template
      } else if (inputNode.isInputParameter()) {
        // TODO handle param: Should probably not have to do anything, since parameters will already have to be set inside any test calling this method.
        sb.append(inputNode.getName());
      } else if (isMethodReturn.test(inputNode)) {
        // TODO handle return node: Maybe we have to make sure that the name of something else returning it is equal to this methodCall receiving it.
        // Maybe we do not need to do anything.
      } else {
        // TODO handle nodes that are constructed inside the method. These will have to be constructed and initialized separately.
      }
    }
    return sb.toString();
  }

}
