package io.ebean.querybean.generator;

import java.util.Map.Entry;
import java.util.Set;

/**
 * Property type for fields handled by ScalarTypes
 */
class PropertyTypeScalar extends PropertyType {
  /**
   * The package name for this associated query bean.
   */
  private final Set<String> assocImports;
  private final String attributeCompleteSignature;

  /**
   * Construct given the associated bean type name and package.
   *
   * @param attributeClass   the type in the database bean that will be serialized via ScalarType
   */
  PropertyTypeScalar(String attributeClass) {
    this("PScalar", attributeClass);
  }

  protected PropertyTypeScalar(String propertyType, String attributeClass) {
    super(propertyType);

    final Entry<String, Set<String>> signature = Split.genericsSplit(attributeClass);

    this.attributeCompleteSignature = signature.getKey();
    this.assocImports = signature.getValue();
  }

  @Override
  String getTypeDefn(String shortName, boolean assoc) {
    if (assoc) {
      // PScalarType<R, PhoneNumber>
      return propertyType + "<R, " + attributeCompleteSignature + ">";
    } else {
      // PScalarType<QCustomer, PhoneNumber>
      return propertyType + "<Q" + shortName + ", " + attributeCompleteSignature + ">";
    }
  }

  /**
   * All required imports to the allImports set.
   */
  @Override
  void addImports(Set<String> allImports) {
    super.addImports(allImports);
    allImports.addAll(assocImports);
  }
}
