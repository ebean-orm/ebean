package com.avaje.ebeaninternal.server.ddl;

import com.avaje.ebeaninternal.server.deploy.BeanProperty;
import com.avaje.ebeaninternal.server.deploy.BeanPropertyAssocMany;
import com.avaje.ebeaninternal.server.deploy.BeanPropertyAssocOne;
import com.avaje.ebeaninternal.server.deploy.BeanPropertyCompound;

/**
 * Used to visit a BeanProperty given the type of bean property it is.
 */
public interface PropertyVisitor {

	/**
	 * Visit a OneToMany or ManyToMany property.
	 */
	public void visitMany(BeanPropertyAssocMany<?> p);

	/**
	 * Visit the imported side of a OneToOne property.
	 */
	public void visitOneImported(BeanPropertyAssocOne<?> p);

	/**
	 * Visit the exported side of a OneToOne property.
	 */
	public void visitOneExported(BeanPropertyAssocOne<?> p);
	
	/**
	 * Visit an embedded property.
	 */
	public void visitEmbedded(BeanPropertyAssocOne<?> p);

	/**
	 * Visit the scalar property of an embedded bean.
	 */
	public void visitEmbeddedScalar(BeanProperty p, BeanPropertyAssocOne<?> embedded);

	/**
	 * Visit a scalar property.
	 */
	public void visitScalar(BeanProperty p);

	/**
	 * Visit a compound value object.
	 */
    public void visitCompound(BeanPropertyCompound p);

    /**
     * Visit the scalar value inside a compound value object.
     */
    public void visitCompoundScalar(BeanPropertyCompound compound, BeanProperty p);

}
