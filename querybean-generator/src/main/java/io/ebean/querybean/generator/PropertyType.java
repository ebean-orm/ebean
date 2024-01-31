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
  final String pkg;

  /**
   * Construct with a className of primitive name for the type.
   */
  PropertyType(String propertyType) {
    this.propertyType = propertyType;
    this.pkg = "io.ebean.typequery.";
  }

  PropertyType(String propertyType, String pkg) {
    this.propertyType = propertyType;
    this.pkg = pkg;
  }

  String propertyType() {
    return propertyType;
  }

  @Override
  public String toString() {
    return propertyType;
  }

  /**
   * Return the type definition for this property.
   *
   * @param shortName    The short name of the property type
   * @param assoc        flag set to true if the property is on an association bean
   * @param fullyQualify flag set to fully qualify the type
   */
  String getTypeDefn(String shortName, boolean assoc, boolean fullyQualify) {
    String q = fullyQualify ? pkg : "";
    if (assoc) {
      // PLong<R>
      return q + propertyType + "<R>";
    } else {
      // PLong<QCustomer>
      return q + propertyType + "<Q" + shortName + ">";
    }
  }

  /**
   * Add any required imports for this property to the allImports set.
   */
  void addImports(Set<String> allImports, boolean fullyQualify) {
    if (!fullyQualify) {
      allImports.add(pkg + propertyType);
    }
  }

}
