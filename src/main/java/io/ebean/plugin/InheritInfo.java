package io.ebean.plugin;

import java.util.Set;

public interface InheritInfo {

  String getDiscriminatorColumn();

  int getColumnLength();

  int getDiscriminatorType();

  void visitChildren(InheritInfoVisitor visitor);

  String getDiscriminatorStringValue();

  Class<?> getType();

  InheritInfo getRoot();

  boolean isRoot();

  boolean hasChildren();

  String getColumnDefn();

  Property[] getPropertiesLocal();

  void appendCheckConstraintValues(String name, Set<String> checkConstraintValues);

  Property getProperty(String propertyName);

}
