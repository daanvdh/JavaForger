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
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;

import common.SymbolSolverSetup;
import dataflow.DataFlowGraph;
import dataflow.DataFlowMethod;
import dataflow.GraphBuilder;
import dataflow.NodeBuilder;
import templateInput.definition.MethodDefinition;
import templateInput.definition.MethodDefinition.Builder;
import templateInput.definition.VariableDefinition;

/**
 * Unit test for {@link MethodFactory}.
 *
 * @author Daan
 */
@RunWith(MockitoJUnitRunner.class)
public class MethodFactoryTest {

  @Mock
  private FieldFactory fieldFactory;
  @Mock
  private DataFlowGraph dfg;

  @InjectMocks
  private MethodFactory methodFactory = new MethodFactory();

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

    FieldDeclaration javaParserNode = new FieldDeclaration();
    setterDfg.getFields().get(0).setRepresentedNode(javaParserNode);
    String name = "unique";
    VariableDefinition fieldVariDef = VariableDefinition.builder().withName(name).build();
    Mockito.when(fieldFactory.create(javaParserNode)).thenReturn(fieldVariDef);

    MethodDefinition method = methodFactory.createMethod(inputMethod, dfg);

    assertEquals(1, method.getChangedFields().size());
    assertEquals(name, method.getChangedFields().get(0).getName().toString());
  }

  @Test
  public void testRead_Methods() throws IOException {
    String input = "src/test/java/inputClassesForTests/Product.java";
    List<? extends MethodDefinition> methods = sut.read(input).getMethods();

    Builder build = MethodDefinition.builder().withAccessModifiers(Collections.singleton("public")).withType("String");
    MethodDefinition m1 = build.withName("getUrl").withLineNumber(46).withColumn(3).build();
    MethodDefinition m2 = build.withName("getName").withLineNumber(50).withColumn(3).build();
    MethodDefinition m3 = build.withName("toString").withLineNumber(54).withColumn(3).withAnnotations(Collections.singleton("Override")).build();
    MethodDefinition m4 =
        build.withName("hashCode").withLineNumber(59).withColumn(3).withAnnotations(Collections.singleton("Override")).withType("int").build();
    MethodDefinition m5 = build.withName("equals").withLineNumber(64).withColumn(3).withAnnotations(Collections.singleton("Override")).withType("boolean")
        .withParameters(VariableDefinition.builder().withType("Object").withName("obj").build()).build();

    Assert.assertThat(methods, Matchers.contains(m1, m2, m3, m4, m5));
  }

  @Test
  public void testRead_Constructors() throws IOException {
    String input = "src/test/java/inputClassesForTests/Product.java";
    List<? extends MethodDefinition> constructors = sut.read(input).getConstructors();

    Builder build = MethodDefinition.builder().withAccessModifiers(Collections.singleton("public")).withType("Product").withName("Product").withColumn(3)
        .withTypeImports("inputClassesForTests");
    MethodDefinition m1 = build.withLineNumber(36).withParameters(VariableDefinition.builder().withType("String").withName("name2").build(),
        VariableDefinition.builder().withType("String").withName("url").build()).build();
    MethodDefinition m2 = build.withLineNumber(42).withParameters().build();

    Assert.assertThat(constructors, Matchers.contains(m1, m2));
  }

}
