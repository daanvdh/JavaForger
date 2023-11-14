/*
 * Copyright (c) 2023 by Eyefreight BV (www.eyefreight.com). All rights reserved.
 *
 * This software is provided by the copyright holder and contributors "as is" and any express or implied warranties, including, but
 * not limited to, the implied warranties of merchantability and fitness for a particular purpose are disclaimed. In no event shall
 * Eyefreight BV or contributors be liable for any direct, indirect, incidental, special, exemplary, or consequential damages
 * (including, but not limited to, procurement of substitute goods or services; * loss of use, data, or profits; or business
 * interruption) however caused and on any theory of liability, whether in contract, strict liability, or tort (including
 * negligence or otherwise) arising in any way out of the use of this software, even if advised of the possibility of such damage.
 */
package template;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

import configuration.JavaForgerConfiguration;
import configuration.StaticJavaForgerConfiguration;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.StringTemplateLoader;
import freemarker.cache.TemplateCache;
import freemarker.cache.TemplateLoader;
import freemarker.cache.TemplateLookupResult;
import freemarker.template.Configuration;
import freemarker.template.Template;
import generator.JavaForgerException;

/**
 * Service for logic related to FreeMarker Templates.
 *
 * @author daan.vandenheuvel
 */
public class TemplateService {

  private StaticJavaForgerConfiguration staticConfig = StaticJavaForgerConfiguration.getConfig();

  public synchronized Template getTemplate(JavaForgerConfiguration config) {
    Template template = null;
    if (config.getTemplateContent() != null) {
      // Setup the template loader to read the overridden template content
      TemplateLoader defaultLoader = staticConfig.getFreeMarkerConfiguration().getTemplateLoader();
      StringTemplateLoader stringTemplateLoader = new StringTemplateLoader();
      stringTemplateLoader.putTemplate(config.getTemplate(), config.getTemplateContent());
      TemplateLoader[] loaders = {stringTemplateLoader, defaultLoader};
      MultiTemplateLoader multiTemplateLoader = new MultiTemplateLoader(loaders);
      staticConfig.getFreeMarkerConfiguration().setTemplateLoader(multiTemplateLoader);

      template = getTemplateFromFreemarker(config);

      // Set the template loader back to it's original state
      staticConfig.getFreeMarkerConfiguration().setTemplateLoader(defaultLoader);
    } else {
      template = getTemplateFromFreemarker(config);
    }
    return template;
  }

  public Optional<String> getAbsoluteTemplatePath(String inputFilePath) {
    Configuration freeMarkerConfiguration = staticConfig.getFreeMarkerConfiguration();
    String absolutePath = getAbsolutePath(inputFilePath, freeMarkerConfiguration);
    return Optional.ofNullable(absolutePath);
  }

  private String getAbsolutePath(String inputFilePath, Configuration freeMarkerConfiguration) {
    String absolutePath = null;
    try {
      TemplateCache templateCache = getField(freeMarkerConfiguration, "cache", TemplateCache.class);
      Locale locale = freeMarkerConfiguration.getLocale();
      TemplateLookupResult lookup = callMethod(templateCache, "lookupTemplate", TemplateLookupResult.class, new Object[] {inputFilePath, locale, null});
      Object templateSource = callMethod(lookup, "getTemplateSource", Object.class, new Object[] {});
      File source = getField(templateSource, "source", File.class);
      absolutePath = source.getAbsolutePath();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return absolutePath;
  }

  private <T> T callMethod(Object source, String methodName, Class<T> claz, Object[] methodInput)
      throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
    Class<?>[] inputTypes = Arrays.asList(methodInput).stream().map(t -> t == null ? Object.class : t.getClass()).toArray(Class[]::new);
    return callMethod(source, methodName, claz, methodInput, inputTypes);
  }

  private <T> T callMethod(Object source, String methodName, Class<T> claz, Object[] methodInput, Class<?>[] inputTypes)
      throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
    Method method = source.getClass().getDeclaredMethod(methodName, inputTypes);
    method.setAccessible(true);
    Object result = method.invoke(source, methodInput);
    return claz.cast(result);
  }

  private <T> T getField(Object source, String fieldName, Class<T> claz)
      throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
    Field field = source.getClass().getDeclaredField(fieldName);
    field.setAccessible(true);
    Object value = field.get(source);
    return claz.cast(value);
  }

  private Template getTemplateFromFreemarker(JavaForgerConfiguration config) {
    return getTemplateFromFreemarker(config.getTemplate());
  }

  private Template getTemplateFromFreemarker(String templateName) {
    Template template = null;
    try {
      template = staticConfig.getFreeMarkerConfiguration().getTemplate(templateName);
    } catch (Exception e) {
      throw new JavaForgerException(e);
    }
    return template;
  }

}
