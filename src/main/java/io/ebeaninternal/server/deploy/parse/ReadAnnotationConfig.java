package io.ebeaninternal.server.deploy.parse;

import io.ebean.config.ServerConfig;
import io.ebeaninternal.server.deploy.generatedproperty.GeneratedPropertyFactory;

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
  private final boolean jacksonAnnotations;

  ReadAnnotationConfig(GeneratedPropertyFactory generatedPropFactory, String asOfViewSuffix, String versionsBetweenSuffix, ServerConfig serverConfig) {

    this.generatedPropFactory = generatedPropFactory;
    this.asOfViewSuffix = asOfViewSuffix;
    this.versionsBetweenSuffix = versionsBetweenSuffix;
    this.disableL2Cache = serverConfig.isDisableL2Cache();
    this.eagerFetchLobs = serverConfig.isEagerFetchLobs();

    this.javaxValidationAnnotations = generatedPropFactory.getClassLoadConfig().isJavaxValidationAnnotationsPresent();
    this.jacksonAnnotations = generatedPropFactory.getClassLoadConfig().isJacksonAnnotationsPresent();
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

  boolean isJavaxValidationAnnotations() {
    return javaxValidationAnnotations;
  }

  boolean isJacksonAnnotations() {
    return jacksonAnnotations;
  }
}
