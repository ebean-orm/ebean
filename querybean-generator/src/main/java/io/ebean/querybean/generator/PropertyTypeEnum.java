package io.ebean.querybean.generator;


import java.util.Set;

/**
 * Enum property type.
 */
class PropertyTypeEnum extends PropertyType {

  private final String enumClass;

  private final String enumShortName;

  PropertyTypeEnum(String enumClass, String enumShortName) {
    super("PEnum");
    this.enumClass = enumClass;
    this.enumShortName = enumShortName;
  }

  @Override
  String getTypeDefn(String shortName, boolean assoc, boolean fullyQualify) {
    String q = fullyQualify ? pkg : "";
    if (assoc) {
      return q + "PEnum<R," + enumShortName + ">";
    } else {
      return q + "PEnum<Q" + shortName + "," + enumShortName + ">";
    }
  }

  @Override
  void addImports(Set<String> allImports, boolean fullyQualify) {
    super.addImports(allImports, fullyQualify);
    allImports.add(enumClass);
  }

}
