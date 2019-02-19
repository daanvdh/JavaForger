/*
 * Copyright 2019 by Daan van den Heuvel.
 *
 * This file is part of JavaForger.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dataflow;

import org.junit.Assert;
import org.junit.Test;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;

/**
 * TODO javadoc
 *
 * @author Daan
 */
public class DataFlowGrapFactoryTest {

  private DataFlowGrapFactory factory = new DataFlowGrapFactory();

  @Test
  public void testCreateGraph_setter() {
    String setter = //
        "public class Claz {\n" + //
            "  private String s;\n" + //
            "  public void setN(String s) {\n" + //
            "    this.s = s;\n" + //
            "  }\n" + //
            "}"; //
    CompilationUnit cu = JavaParser.parse(setter);

    TestDataFlowGraph expected = TestDataFlowGraph.builder().withField("n")
        .withMethod(TestDataFlowMethod.builder().withParameter("n").withInputField("n").withChangedFieldEdge("n", "n").build()).build();

    DataFlowGraph graph = factory.createGraph(cu);

    Assert.assertTrue(expected.equalsGraph(graph));
  }

}
