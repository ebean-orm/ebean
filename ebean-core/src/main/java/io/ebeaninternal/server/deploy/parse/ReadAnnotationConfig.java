package io.ebeaninternal.server.deploy.parse;

import io.ebean.annotation.Aggregation;
import io.ebean.annotation.Formula;
import io.ebean.annotation.Where;
import io.ebean.config.ClassLoadConfig;
import io.ebean.config.DatabaseConfig;
import io.ebeaninternal.server.deploy.generatedproperty.GeneratedPropertyFactory;
import io.ebeaninternal.server.deploy.meta.DeployBeanProperty;

import javax.persistence.Column;
import java.util.HashSet;
import java.util.Set;

/**
 * Configuration used when reading the deployment annotations.
 */
class ReadAnnotationConfig {

  private final GeneratedPropertyFactory generatedPropFactory;
  private final String asOfViewSuffix;
  private final String versionsBetweenSuffix;
  private final boolean disableL2Cache;
  private final boolean eagerFetchLobs;
  private final boolean javaxValidationAnnotations;
  private final boolean jakartaValidationAnnotations;
  private final boolean jacksonAnnotations;
  private final boolean idGeneratorAutomatic;
  private final boolean useValidationNotNull;
  private final ReadValidationAnnotations javaxValidation;
  private final ReadValidationAnnotations jakartaValidation;
  private final Set<Class<?>> metaAnnotations = new HashSet<>();

  ReadAnnotationConfig(GeneratedPropertyFactory generatedPropFactory, String asOfViewSuffix, String versionsBetweenSuffix, DatabaseConfig config) {
    this.generatedPropFactory = generatedPropFactory;
    this.asOfViewSuffix = asOfViewSuffix;
    this.versionsBetweenSuffix = versionsBetweenSuffix;
    this.disableL2Cache = config.isDisableL2Cache();
    this.eagerFetchLobs = config.isEagerFetchLobs();
    this.idGeneratorAutomatic = config.isIdGeneratorAutomatic();
    this.useValidationNotNull = config.isUseValidationNotNull();
    ClassLoadConfig classLoadConfig = generatedPropFactory.getClassLoadConfig();
    this.javaxValidationAnnotations = classLoadConfig.isJavaxValidationAnnotationsPresent();
    this.jakartaValidationAnnotations = classLoadConfig.isJakartaValidationAnnotationsPresent();
    this.jacksonAnnotations = classLoadConfig.isJacksonAnnotationsPresent();
    this.metaAnnotations.add(Column.class);
    this.metaAnnotations.add(Formula.class);
    this.metaAnnotations.add(Formula.List.class);
    this.metaAnnotations.add(Where.class);
    this.metaAnnotations.add(Where.List.class);
    this.metaAnnotations.add(Aggregation.class);
    this.javaxValidation = javaxValidationAnnotations ? new ReadValidationAnnotationsJavax(this) : null;
    this.jakartaValidation = jakartaValidationAnnotations ? new ReadValidationAnnotationsJakarta(this) : null;
    if (jacksonAnnotations) {
      InitMetaJacksonAnnotation.init(this);
    }
  }

  void addMetaAnnotation(Class<?> annotation) {
    metaAnnotations.add(annotation);
  }

  boolean checkValidationAnnotations() {
    return javaxValidationAnnotations || jakartaValidationAnnotations;
  }

  GeneratedPropertyFactory getGeneratedPropFactory() {
    return generatedPropFactory;
  }

  String getAsOfViewSuffix() {
    return asOfViewSuffix;
  }

  String getVersionsBetweenSuffix() {
    return versionsBetweenSuffix;
  }

  boolean isDisableL2Cache() {
    return disableL2Cache;
  }

  boolean isEagerFetchLobs() {
    return eagerFetchLobs;
  }

  boolean isIdGeneratorAutomatic() {
    return idGeneratorAutomatic;
  }

  boolean isJacksonAnnotations() {
    return jacksonAnnotations;
  }

  public Set<Class<?>> getMetaAnnotations() {
    return metaAnnotations;
  }

  /**
   * Return true if a NotNull validation annotation is on the property.
   */
  boolean isValidationNotNull(DeployBeanProperty property) {
    if (!useValidationNotNull) {
      return false;
    }
    if (javaxValidation != null && javaxValidation.isValidationNotNull(property)) {
      return true;
    }
    if (jakartaValidation != null && jakartaValidation.isValidationNotNull(property)) {
      return true;
    }
    return false;
  }

  /**
   * Return the max size of all validation @Size annotations.
   */
  int maxValidationSize(DeployBeanProperty prop) {
    int maxSize = 0;
    if (javaxValidation != null) {
      maxSize = Math.max(maxSize, javaxValidation.maxSize(prop));
    }
    if (jakartaValidation != null) {
      maxSize = Math.max(maxSize, jakartaValidation.maxSize(prop));
    }
    return maxSize;
  }

}
