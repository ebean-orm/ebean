package io.ebean.querybean.generator;

import java.util.Set;

/**
 * Property type for associated beans (OneToMany, ManyToOne etc).
 */
class PropertyTypeAssoc extends PropertyType {

  /**
   * The package name for this associated query bean.
   */
  private final String assocPackage;

  /**
   * Construct given the associated bean type name and package.
   *
   * @param qAssocTypeName the associated bean type name.
   * @param assocPackage   the associated bean package.
   */
  PropertyTypeAssoc(String qAssocTypeName, String assocPackage) {
    super(qAssocTypeName);
    this.assocPackage = assocPackage;
  }

  /**
   * All required imports to the allImports set.
   */
  @Override
  void addImports(Set<String> allImports) {
    allImports.add(assocPackage + "." + propertyType);
  }

}
