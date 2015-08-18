package com.avaje.ebeaninternal.server.deploy.parse;

import com.avaje.ebeaninternal.api.ClassUtil;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptorManager;
import com.avaje.ebeaninternal.server.deploy.generatedproperty.GeneratedPropertyFactory;


/**
 * Read the deployment annotations for the bean.
 */
public class ReadAnnotations {

  /**
   * Creates appropriate generated properties - WhenXXX, WhoXXX, Version etc.
   */
  private final GeneratedPropertyFactory generatedPropFactory;

  /**
   * Typically _with_history and when appended to the base table derives the name of
   * the view that unions the base table with the history table to support asOf queries.
   */
  private final String asOfViewSuffix;

  private final String versionsBetweenSuffix;

  /**
   * True if the javax validation annotations are present in the classpath.
   */
  private final boolean javaxValidationAnnotations;

  /**
   * True if the jackson annotations are present in the classpath.
   */
  private final boolean jacksonAnnotations;

  public ReadAnnotations(GeneratedPropertyFactory generatedPropFactory, String asOfViewSuffix, String versionsBetweenSuffix) {
    this.generatedPropFactory = generatedPropFactory;
    this.asOfViewSuffix = asOfViewSuffix;
    this.versionsBetweenSuffix = versionsBetweenSuffix;
    this.javaxValidationAnnotations = ClassUtil.isJavaxValidationAnnotationsPresent();
    this.jacksonAnnotations = ClassUtil.isJacksonAnnotationsPresent();
  }

  /**
   * Read the initial non-relationship annotations included Id and EmbeddedId.
   * <p>
   * We then have enough to create BeanTables which are used in readAssociations
   * to resolve the relationships etc.
   * </p>
   */
  public void readInitial(DeployBeanInfo<?> info, boolean eagerFetchLobs) {

    try {
      new AnnotationClass(info, javaxValidationAnnotations, asOfViewSuffix, versionsBetweenSuffix).parse();
      new AnnotationFields(generatedPropFactory, info, javaxValidationAnnotations, jacksonAnnotations, eagerFetchLobs).parse();

    } catch (RuntimeException e) {
      throw new RuntimeException("Error reading annotations for " + info, e);
    }
  }

  /**
   * Read and process the associated relationship annotations.
   * <p>
   * These can only be processed after the BeanTables have been created
   * </p>
   * <p>
   * This uses the factory as a call back to get the BeanTable for a given
   * associated bean.
   * </p>
   */
  public void readAssociations(DeployBeanInfo<?> info, BeanDescriptorManager factory) {

    try {

      new AnnotationAssocOnes(info, javaxValidationAnnotations, factory).parse();
      new AnnotationAssocManys(info, javaxValidationAnnotations, factory).parse();

      // read the Sql annotations last because they may be
      // dependent on field level annotations
      new AnnotationSql(info, javaxValidationAnnotations).parse();

    } catch (RuntimeException e) {
      throw new RuntimeException("Error reading annotations for " + info, e);
    }
  }
}
