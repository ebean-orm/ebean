package io.ebean.querybean.generator;

import java.util.Set;

/**
 * Property type for associated beans (OneToMany, ManyToOne etc).
 */
class PropertyTypeAssoc extends PropertyType {

  /**
   * Construct given the associated bean type name and package.
   *
   * @param qAssocTypeName the associated bean type name.
   */
  PropertyTypeAssoc(String qAssocTypeName) {
    super(qAssocTypeName);
  }

  /**
   * All required imports to the allImports set.
   */
  @Override
  void addImports(Set<String> allImports) {
    // do nothing
  }

}
