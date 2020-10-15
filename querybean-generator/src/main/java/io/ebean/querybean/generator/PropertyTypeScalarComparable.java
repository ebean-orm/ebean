package io.ebean.querybean.generator;

/**
 * Property type for associated beans (OneToMany, ManyToOne etc).
 */
class PropertyTypeScalarComparable extends PropertyTypeScalar {

  /**
   * Construct given the associated bean type name and package.
   *
   * @param attributeClass   the type in the database bean that will be serialized via ScalarType
   */
  PropertyTypeScalarComparable(String attributeClass) {
    super("PScalarComparable", attributeClass);
  }
}
