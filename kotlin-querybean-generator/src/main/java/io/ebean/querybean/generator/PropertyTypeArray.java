package io.ebean.querybean.generator;


import java.util.Set;

/**
 * Array property type.
 */
class PropertyTypeArray extends PropertyType {

  private final String elementClass;

  private final String elementShortName;

  PropertyTypeArray(String elementClass, String elementShortName) {
    super("PArray");
    this.elementClass = elementClass;
    this.elementShortName = elementShortName;
  }

  @Override
  String getTypeDefn(String shortName, boolean assoc) {
    if (assoc) {
      return "PArray<R," + elementShortName + ">";

    } else {
      return "PArray<Q" + shortName + "," + elementShortName + ">";
    }
  }

  @Override
  void addImports(Set<String> allImports) {
    super.addImports(allImports);
    allImports.add(elementClass);
  }

}
