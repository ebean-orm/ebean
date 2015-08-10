package com.avaje.ebean.dbmigration.model.visitor;

import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;

/**
 * Visitor pattern for visiting a BeanDescriptor and potentially all its bean
 * properties.
 */
public interface BeanVisitor {

	/**
	 * Visit a BeanDescriptor and return a PropertyVisitor to use to visit each
   * property on the entity bean (return null to skip visiting this bean).
	 */
  BeanPropertyVisitor visitBean(BeanDescriptor<?> descriptor);

}
