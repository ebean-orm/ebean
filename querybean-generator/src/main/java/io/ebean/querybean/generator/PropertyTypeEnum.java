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
  String getTypeDefn(String shortName, boolean assoc) {
    if (assoc) {
      return "PEnum<R," + enumShortName + ">";

    } else {
      return "PEnum<Q" + shortName + "," + enumShortName + ">";
    }
  }

  @Override
  void addImports(Set<String> allImports) {
    super.addImports(allImports);
    allImports.add(enumClass);
  }

}
