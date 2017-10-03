package io.ebean.dbmigration.model.visitor;

import io.ebean.dbmigration.model.build.ModelBuildPropertyVisitor;
import io.ebean.plugin.BeanType;

/**
 * Visitor pattern for visiting a BeanDescriptor and potentially all its bean
 * properties.
 */
public interface BeanVisitor {

  /**
   * Visit a BeanDescriptor and return a PropertyVisitor to use to visit each
   * property on the entity bean (return null to skip visiting this bean).
   */
  ModelBuildPropertyVisitor visitBean(BeanType<?> descriptor);

}
