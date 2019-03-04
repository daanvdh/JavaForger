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
package templateInput.definition;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.mockito.internal.util.collections.Sets;

import configuration.FreeMarkerConfiguration;
import freemarker.template.TemplateException;
import templateInput.TemplateInputParameters;
import templateTests.AbstractTemplateExecutingTest;

/**
 * Integration test for {@link AnnotationDefinition} testing if the template engine {@link FreeMarkerConfiguration} can still be used.
 *
 * @author Daan
 */
public class AnnotationDefinitionTest extends AbstractTemplateExecutingTest {

  private static final String TEMPLATE_LOCATION_PREFIX = "AnnotationDefinition/";

  @Test
  public void testContainsAnnotationTrue() throws IOException, TemplateException {
    String template = "contains.javat";
    String expected = "annotations contains one";
    TemplateInputParameters parameters = new TemplateInputParameters();
    parameters.put("annotations", Sets.newSet(new AnnotationDefinition("one"), new AnnotationDefinition("two")));
    executeAndVerify(template, null, parameters, expected);
  }

  @Test
  public void testContainsAnnotationFalse() throws IOException, TemplateException {
    String template = "contains.javat";
    String expected = "annotations does not contain one";
    TemplateInputParameters parameters = new TemplateInputParameters();
    parameters.put("annotations", Sets.newSet(new AnnotationDefinition("two"), new AnnotationDefinition("three")));
    executeAndVerify(template, null, parameters, expected);
  }

  @Test
  public void test_accessParameter() throws IOException, TemplateException {
    String template = "access.javat";
    String expected = "for annotation one the value for name is Gerrit";

    Map<String, String> annotationParameters = new HashMap<>();
    annotationParameters.put("number", "1");
    annotationParameters.put("name", "Gerrit");
    AnnotationDefinition annotation = new AnnotationDefinition("one");
    annotation.setParameters(annotationParameters);
    TemplateInputParameters parameters = new TemplateInputParameters();
    parameters.put("annotations", Collections.singletonMap(annotation.getName(), annotation));

    executeAndVerify(template, null, parameters, expected);
  }

  @Override
  protected void executeAndVerify(String template, String inputClass, TemplateInputParameters map, String expected) throws IOException, TemplateException {
    super.executeAndVerify(TEMPLATE_LOCATION_PREFIX + template, inputClass, map, expected);
  }

}
