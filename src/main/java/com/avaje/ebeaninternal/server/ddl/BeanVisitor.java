package com.avaje.ebeaninternal.server.ddl;

import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.deploy.BeanProperty;

/**
 * Visitor pattern for visiting a BeanDescriptor and potentially all its bean
 * properties.
 */
public interface BeanVisitor {

	/**
	 * Starting.
	 */
	void visitBegin();

	/**
	 * Visit a BeanDescriptor and return true to continue visiting the bean
	 * (return false to skip visiting this bean).
	 */
	boolean visitBean(BeanDescriptor<?> descriptor);

	/**
	 * Visit a property potentially return a specific PropertyVisitor.
	 * <p>
	 * A PropertyVisitor can be returned to more easily process bean properties
	 * by their specific type.
	 * </p>
	 */
	PropertyVisitor visitProperty(BeanProperty p);

	/**
	 * Finished visiting the BeanDescriptor.
	 */
	void visitBeanEnd(BeanDescriptor<?> descriptor);

	/**
	 * Finished all visiting.
	 */
	void visitEnd();

}
