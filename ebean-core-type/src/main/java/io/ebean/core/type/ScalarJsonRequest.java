package io.ebean.core.type;

import io.ebean.annotation.MutationDetection;

/**
 * A request to create a ScalarType for a bean property (typically annotated with {@code @DbJson}).
 */
public final class ScalarJsonRequest {

  private final ScalarJsonManager manager;
  private final int dbType;
  private final DocPropertyType docType;
  private final Class<?> beanType;
  private final MutationDetection mode;
  private final String name;

  public ScalarJsonRequest(ScalarJsonManager manager, int dbType, DocPropertyType docType, Class<?> beanType, MutationDetection mode, String name) {
    this.manager = manager;
    this.dbType = dbType;
    this.docType = docType;
    this.beanType = beanType;
    this.mode = mode;
    this.name = name;
  }

  /**
   * Return the manager.
   */
  public ScalarJsonManager manager() {
    return manager;
  }

  /**
   * Return the jdbc type this property maps to (e.g. JSON, JSONB etc).
   */
  public int dbType() {
    return dbType;
  }

  /**
   * Return the document store docType.
   */
  public DocPropertyType docType() {
    return docType;
  }

  /**
   * Return the type of the bean that this property belongs to.
   */
  public Class<?> beanType() {
    return beanType;
  }

  /**
   * Return the mutation detection mode the property should use.
   */
  public MutationDetection mode() {
    return mode;
  }

  /**
   * Return the property name.
   */
  public String name() {
    return name;
  }
}
