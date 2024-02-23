package io.ebean.querybean.generator;

import java.util.Set;

/**
 * Property type for associated beans (OneToMany, ManyToOne etc).
 */
class PropertyTypeAssoc extends PropertyType {

  private final String importName;

  /**
   * Construct given the associated bean type name and package.
   *
   * @param qAssocTypeName the associated bean type name.
   * @param importName the import for the Assoc bean.
   */
  PropertyTypeAssoc(String qAssocTypeName, String importName) {
    super(qAssocTypeName, "");
    this.importName = importName;
  }

  /**
   * All required imports to the allImports set.
   */
  @Override
  void addImports(Set<String> allImports, boolean fullyQualify) {
    allImports.add(importName);
  }

}
