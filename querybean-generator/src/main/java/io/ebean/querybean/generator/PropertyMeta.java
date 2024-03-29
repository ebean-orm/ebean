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

  /**
   * Return the type definition given the type short name and flag indicating if it is an associated bean type.
   */
  private String getTypeDefn(String shortName, boolean assoc, boolean fullyQualify) {
    return type.getTypeDefn(shortName, assoc, fullyQualify);
  }

  void writeFieldDefn(Append writer, String shortName, boolean assoc, boolean fullyQualify) {
    writer.append("  public ");
    writer.append(getTypeDefn(shortName, assoc, fullyQualify));
    writer.append(" ").append(name).append(";");
  }

  void writeFieldAliasDefn(Append writer, String shortName, boolean fullyQualify) {
    writer.append("    public static ");
    writer.append(getTypeDefn(shortName, false, fullyQualify));
    writer.append(" ").append(name).append(" = _alias.").append(name).append(";");
  }
}
