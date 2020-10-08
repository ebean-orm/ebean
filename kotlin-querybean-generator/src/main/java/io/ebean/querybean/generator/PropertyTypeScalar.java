package io.ebean.querybean.generator;

import java.util.Set;

/**
 * Property type for associated beans (OneToMany, ManyToOne etc).
 */
class PropertyTypeScalar extends PropertyType {

  /**
   * The package name for this associated query bean.
   */
  private final String assocPackage;
  private final String attributeSimpleName;

  /**
   * Construct given the associated bean type name and package.
   *
   * @param attributeClass   the type in the database bean that will be serialized via ScalarType
   */
  PropertyTypeScalar(String attributeClass) {
    super("PScalar");
    int split = attributeClass.lastIndexOf('.');
    this.assocPackage = attributeClass.substring(0, split);
    this.attributeSimpleName = attributeClass.substring(split + 1);
  }

  @Override
  String getTypeDefn(String shortName, boolean assoc) {
    if (assoc) {
      // PScalarType<R, PhoneNumber>
      return "PScalar<R, " + attributeSimpleName + ">";
    } else {
      // PScalarType<QCustomer, PhoneNumber>
      return "PScalar<Q" + shortName + ", " + attributeSimpleName + ">";
    }
  }

  /**
   * All required imports to the allImports set.
   */
  @Override
  void addImports(Set<String> allImports) {
    super.addImports(allImports);
    allImports.add(assocPackage + "." + attributeSimpleName);
  }

}
