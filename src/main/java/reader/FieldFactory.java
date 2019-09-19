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

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.expr.Expression;

import templateInput.definition.VariableDefinition;

/**
 * Class for creating {@link VariableDefinition} from parsed fields.
 *
 * @author Daan
 */
public class FieldFactory {

  private ImportResolver importResolver = new ImportResolver();

  public VariableDefinition create(Node node) {
    FieldDeclaration fd = (FieldDeclaration) node;
    Set<String> annotations = fd.getAnnotations().stream().map(annotation -> annotation.getName().toString()).collect(Collectors.toSet());
    Set<String> accessModifiers = fd.getModifiers().stream().map(modifier -> modifier.asString()).collect(Collectors.toSet());
    Optional<String> originalInit = depthFirstSearch(fd, Expression.class);
    VariableDefinition variable = VariableDefinition.builder().withName(fd.getVariable(0).getName().asString()).withType(fd.getElementType().asString())
        .withAnnotations(annotations).withLineNumber(fd.getBegin().map(p -> p.line).orElse(-1)).withColumn(fd.getBegin().map(p -> p.column).orElse(-1))
        .withAccessModifiers(accessModifiers).originalInit(originalInit.orElse(null)).build();

    importResolver.resolveAndSetImport(fd.getElementType(), variable);
    return variable;
  }

  private Optional<String> depthFirstSearch(Node node, Class<Expression> claz) {
    if (claz.isAssignableFrom(node.getClass())) {
      return Optional.of(node.toString());
    }
    return node.getChildNodes().stream().map(n -> depthFirstSearch(n, claz)).filter(Optional::isPresent).map(Optional::get).map(Object::toString).findFirst();
  }
}
