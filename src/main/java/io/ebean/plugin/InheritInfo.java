package io.ebean.plugin;

import java.util.Set;
import java.util.function.Consumer;

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

  void visitPropertiesLocal(Consumer<Property> visitor);

  void appendCheckConstraintValues(String name, Set<String> checkConstraintValues);

  Property getProperty(String propertyName);

}
