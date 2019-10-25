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

import dataflow.GraphUtil;
import dataflow.model.DataFlowGraph;
import dataflow.model.DataFlowMethod;
import dataflow.model.DataFlowNode;
import dataflow.model.NodeCall;
import dataflow.model.ParameterList;
import templateInput.StringConverter;
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

  public MethodDefinition createMethod(Node node, DataFlowGraph dfg) {
    MethodDeclaration md = (MethodDeclaration) node;
    MethodDefinition method = parseCallable(md).build();
    method.setType(md.getTypeAsString());
    importResolver.resolveImport(md.getType()).forEach(method::addTypeImport);
    if (dfg != null) {
      addChangedFields(method, dfg.getMethod(md));
      addMethodsCalls(method, dfg.getMethod(md));
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
    for (DataFlowNode changedField : changedFieldsNodes) {
      Node javaParserNode = changedField.getRepresentedNode();
      if (javaParserNode instanceof VariableDeclarator) {

        List<DataFlowNode> fieldAssigners = dataFlowMethod.getDirectInputNodesFor(changedField);
        List<DataFlowNode> receivedNodes =
            fieldAssigners.stream().map(n -> n.walkBackUntil(dataFlowMethod::isInputBoundary)).flatMap(Collection::stream).collect(Collectors.toList());
        List<String> receivedNames = receivedNodes.stream().map(DataFlowNode::getName).collect(Collectors.toList());
        VariableDefinition field = fieldFactory.createSingle(javaParserNode);
        FlowReceiverDefinition receiver = FlowReceiverDefinition.builder().copy(field).receivedValues(receivedNames).build();
        changedFields.add(receiver);
      } else {
        LOG.error("Cannot add changed field {} to method {} because represented node {} with type {} was not a VariableDeclarator", changedField.getName(),
            newMethod.getName(), javaParserNode, javaParserNode.getClass());
      }
    }
    newMethod.setChangedFields(changedFields);
  }

  private void addMethodsCalls(MethodDefinition newMethod, DataFlowMethod dataFlowMethod) {
    for (NodeCall call : dataFlowMethod.getNodeCalls()) {
      if (call.isReturnRead()) {
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
    call.getInstanceName().ifPresent(name -> callSignature.append(name + "."));
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

    String returnSignature = "return" + new StringConverter(call.getName()).getUpperFirst();
    method.setReturnSignature(returnSignature);
    method.setCallSignature(callSignature.toString());

    newMethod.addInputMethod(method);

    if (call.getReturnNode().isPresent() && dataFlowMethod.getReturnNode().isPresent()) {
      List<DataFlowNode> returnNode =
          GraphUtil.walkForwardUntil(call.getReturnNode().get(), dataFlowMethod.getReturnNode().get()::equals, dataFlowMethod.getNodes()::contains);

      // If the returnNode was returned, that means that there exists a path from call.getReturnNode()
      if (returnNode.size() > 0) {
        String expectedReturn = newMethod.getExpectedReturn() == null ? returnSignature : newMethod.getExpectedReturn() + "__" + returnSignature;
        newMethod.setExpectedReturn(expectedReturn);
      }
    }

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

    List<DataFlowNode> inputNodes = GraphUtil.walkBackUntil(param, isField.or(isParam).or(isMethodReturn));

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
        sb.append(inputNode.getName());
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
