package io.ebean.querybean.generator;

import java.util.Set;

/**
 * Property type for associated beans (OneToMany, ManyToOne etc).
 */
class PropertyTypeAssoc extends PropertyType {

  private final String importName;

  PropertyTypeAssoc(String qAssocTypeName, String importName) {
    super(qAssocTypeName);
    this.importName = importName;
  }

  @Override
  void addImports(Set<String> allImports) {
    allImports.add(importName);
  }

}
