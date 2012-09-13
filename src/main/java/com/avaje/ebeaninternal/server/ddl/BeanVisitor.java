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
	public void visitBegin();

	/**
	 * Visit a BeanDescriptor and return true to continue visiting the bean
	 * (return false to skip visiting this bean).
	 */
	public boolean visitBean(BeanDescriptor<?> descriptor);

	/**
	 * Visit a property potentially return a specific PropertyVisitor.
	 * <p>
	 * A PropertyVisitor can be returned to more easily process bean properties
	 * by their specific type.
	 * </p>
	 */
	public PropertyVisitor visitProperty(BeanProperty p);

	/**
	 * Finished visiting the BeanDescriptor.
	 */
	public void visitBeanEnd(BeanDescriptor<?> descriptor);

	/**
	 * Finished all visiting.
	 */
	public void visitEnd();

}
