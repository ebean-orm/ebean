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
  String getTypeDefn(String shortName, boolean assoc, boolean fullyQualify) {
    String q = fullyQualify ? pkg : "";
    if (assoc) {
      return q + "PArray<R," + elementShortName + ">";
    } else {
      return q + "PArray<Q" + shortName + "," + elementShortName + ">";
    }
  }

  @Override
  void addImports(Set<String> allImports, boolean fullyQualify) {
    super.addImports(allImports, fullyQualify);
    allImports.add(elementClass);
  }

}
