package io.ebean.querybean.generator;

/**
 * Meta data for a property.
 */
class PropertyMeta {

  /**
   * The property name.
   */
  private final String name;

  /**
   * The property type.
   */
  private final PropertyType type;

  /**
   * Construct given the property name and type.
   */
  PropertyMeta(String name, PropertyType type) {
    this.name = name;
    this.type = type;
  }

  String getName() {
    return name;
  }

  /**
   * Return the type definition given the type short name and flag indicating if it is an associated bean type.
   */
  String getTypeDefn(String shortName, boolean assoc) {
    return type.getTypeDefn(shortName, assoc);
  }

}
