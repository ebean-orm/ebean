package io.ebeaninternal.server.deploy.parse;

import io.ebeaninternal.annotation.CustomAnnotationParser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.util.HashSet;
import java.util.Set;

/**
 * AnnotationCustomDeploy is executed after all DeployBeanInfo is created. 
 */
public class AnnotationCustomDeploy extends AnnotationParser {

  private static final Logger logger = LoggerFactory.getLogger(AnnotationCustomDeploy.class);
  private final CustomAnnotationParser.Stage stage;



  /**
   * Create for normal early parse of class level annotations.
   */
  public AnnotationCustomDeploy(DeployBeanInfo<?> info, boolean validationAnnotations, CustomAnnotationParser.Stage stage) {
    super(info, validationAnnotations);
    this.stage = stage;
  }


  /**
   * Read the class level deployment annotations.
   */
  @Override
  public void parse() {
    Set<CustomAnnotationParser> customParserAnnotations = AnnotationBase.findAnnotationsRecursive(descriptor.getBeanType(), CustomAnnotationParser.class);
    Set<Class<? extends AnnotationParser>> parserClasses = new HashSet<>();
    for (CustomAnnotationParser customParserAnnotation : customParserAnnotations) {
      if (customParserAnnotation.stage().equals(stage)) {
        parserClasses.add(customParserAnnotation.value());
      }
    }
    for (Class<? extends AnnotationParser> annotationParserClass: parserClasses) {
      logger.trace("Running custom annotation parser class: {}", annotationParserClass );
      createInstance(annotationParserClass).parse();
    }
  }

  private AnnotationParser createInstance(Class<? extends AnnotationParser> customDeployParserClass) {
    try {
      Constructor<? extends AnnotationParser> ctor = customDeployParserClass.getConstructor(DeployBeanInfo.class, Boolean.TYPE);
      return ctor.newInstance(info, validationAnnotations);
    } catch (Exception e) {
      throw new RuntimeException("Cannot create instance " + customDeployParserClass, e);
    }
  }

}