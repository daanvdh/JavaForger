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

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;

import common.SymbolSolverSetup;
import dataflow.GraphBuilder;
import dataflow.NodeBuilder;
import dataflow.model.DataFlowGraph;
import dataflow.model.DataFlowMethod;
import templateInput.definition.MethodDefinition;
import templateInput.definition.MethodDefinition.Builder;
import templateInput.definition.VariableDefinition;

/**
 * Unit test for {@link MethodDefinitionFactory}.
 *
 * @author Daan
 */
@RunWith(MockitoJUnitRunner.class)
public class MethodDefinitionFactoryTest {

  @Mock
  private VariableDefintionFactory fieldFactory;
  @Mock
  private DataFlowGraph dfg;

  @InjectMocks
  private MethodDefinitionFactory methodFactory = new MethodDefinitionFactory();

  private ClassContainerReader sut = new ClassContainerReader();

  @Before
  public void setup() {
    SymbolSolverSetup.setup();
  }

  @Test
  public void testAddChangedFields() {
    DataFlowGraph setterDfg = GraphBuilder.withStartingNodes(NodeBuilder.ofParameter("setS", "a").to("setS.s").to(NodeBuilder.ofField("s"))).build();
    Node inputMethod = new MethodDeclaration();
    DataFlowMethod methodDfn = setterDfg.getMethods().iterator().next();
    Mockito.when(dfg.getMethod(inputMethod)).thenReturn(methodDfn);

    VariableDeclarator javaParserNode = new VariableDeclarator();
    setterDfg.getFields().get(0).setRepresentedNode(javaParserNode);
    String name = "unique";
    VariableDefinition fieldVariDef = VariableDefinition.builder().name(name).build();
    Mockito.when(fieldFactory.createSingle(javaParserNode)).thenReturn(fieldVariDef);

    MethodDefinition method = methodFactory.createMethod(inputMethod, dfg);

    assertEquals(1, method.getChangedFields().size());
    assertEquals(name, method.getChangedFields().get(0).getName().toString());
  }

  @Test
  public void testRead_Methods() throws IOException {
    String input = "src/test/java/inputClassesForTests/Product.java";
    List<? extends MethodDefinition> methods = sut.read(input).getMethods();

    Builder build = MethodDefinition.builder().accessModifiers(Collections.singleton("public")).type("String");
    MethodDefinition m1 = build.name("getUrl").lineNumber(46).column(3).build();
    MethodDefinition m2 = build.name("getName").lineNumber(50).column(3).build();
    MethodDefinition m3 = build.name("toString").lineNumber(54).column(3).annotations(Collections.singleton("Override")).build();
    MethodDefinition m4 = build.name("hashCode").lineNumber(59).column(3).annotations(Collections.singleton("Override")).type("int").build();
    MethodDefinition m5 = build.name("equals").lineNumber(64).column(3).annotations(Collections.singleton("Override")).type("boolean")
        .parameters(VariableDefinition.builder().type("Object").name("obj").build()).build();

    Assert.assertThat(methods, Matchers.contains(m1, m2, m3, m4, m5));
  }

  @Test
  public void testRead_Constructors() throws IOException {
    String input = "src/test/java/inputClassesForTests/Product.java";
    List<? extends MethodDefinition> constructors = sut.read(input).getConstructors();

    Builder build = MethodDefinition.builder().accessModifiers(Collections.singleton("public")).type("Product").name("Product").column(3)
        .typeImports("inputClassesForTests");
    MethodDefinition m1 = build.lineNumber(36)
        .parameters(VariableDefinition.builder().type("String").name("name2").build(), VariableDefinition.builder().type("String").name("url").build()).build();
    MethodDefinition m2 = build.lineNumber(42).parameters().build();

    Assert.assertThat(constructors, Matchers.contains(m1, m2));
  }

  @Test
  public void testAddInputMethods() {
    // TODO implement
  }

}
