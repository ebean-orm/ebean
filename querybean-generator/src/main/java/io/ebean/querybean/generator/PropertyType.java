package io.ebean.querybean.generator;

import java.util.Set;

/**
 * Property type definition.
 */
class PropertyType {

  /**
   * The property type className or primitive short name.
   */
  final String propertyType;

  /**
   * Construct with a className of primitive name for the type.
   */
  PropertyType(String propertyType) {
    this.propertyType = propertyType;
  }

  @Override
  public String toString() {
    return propertyType;
  }

  /**
   * Return the type definition for this property.
   *
   * @param shortName The short name of the property type
   * @param assoc     flag set to true if the property is on an association bean
   */
  String getTypeDefn(String shortName, boolean assoc) {
    if (assoc) {
      //    PLong<R>
      return propertyType + "<R>";
    } else {
      //    PLong<QCustomer>
      return propertyType + "<Q" + shortName + ">";
    }
  }

  /**
   * Add any required imports for this property to the allImports set.
   */
  void addImports(Set<String> allImports) {
    allImports.add("io.ebean.typequery." + propertyType);
  }

}
