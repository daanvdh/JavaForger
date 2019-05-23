/*
 * Copyright 2018 by Daan van den Heuvel.
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
package configuration;

import freemarker.core.PlainTextOutputFormat;
import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import generator.Generator;

/**
 * This class holds the default configurations for Freemarker.
 *
 * @author Daan
 */
public class FreeMarkerConfiguration {
  public static final String TEMPLATE_LOCATION = "/templates/";

  public static Configuration getDefaultConfig() {
    return initConfig();
  }

  private static Configuration initConfig() {
    Configuration config;
    // TODO maybe put this method in a separate singleton class with a lazy getter for it.

    // Create your Configuration instance, and specify if up to what FreeMarker
    // version (here 2.3.27) do you want to apply the fixes that are not 100%
    // backward-compatible. See the Configuration JavaDoc for details.
    config = new Configuration(Configuration.VERSION_2_3_28);

    // Specify the source where the template files come from. Here I set a
    // plain directory for it, but non-file-system sources are possible too:
    config.setClassForTemplateLoading(Generator.class, TEMPLATE_LOCATION);

    // Set the preferred charset template files are stored in. UTF-8 is
    // a good choice in most applications:
    config.setDefaultEncoding("UTF-8");

    // Sets how errors will appear.
    // During web page *development* TemplateExceptionHandler.HTML_DEBUG_HANDLER is better.
    config.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);

    // Don't log exceptions inside FreeMarker that it will thrown at you anyway:
    config.setLogTemplateExceptions(false);

    // Wrap unchecked exceptions thrown during template processing into TemplateException-s.
    config.setWrapUncheckedExceptions(true);

    // Sets the output format type
    config.setOutputFormat(PlainTextOutputFormat.INSTANCE);

    // This prevents special characters (like <>{}& ) from being escaped.
    config.setAutoEscapingPolicy(Configuration.DISABLE_AUTO_ESCAPING_POLICY);

    return config;
  }

}
