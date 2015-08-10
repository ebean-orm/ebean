package com.avaje.ebean.dbmigration.model.visitor;

import com.avaje.ebeaninternal.server.deploy.BeanProperty;
import com.avaje.ebeaninternal.server.deploy.BeanPropertyAssocMany;
import com.avaje.ebeaninternal.server.deploy.BeanPropertyAssocOne;
import com.avaje.ebeaninternal.server.deploy.BeanPropertyCompound;

/**
 * Used to visit a BeanProperty given the type of bean property it is.
 */
public interface BeanPropertyVisitor {

  /**
   * Completed visiting all the properties on the bean.
   */
  void visitEnd();

  /**
   * Visit a OneToMany or ManyToMany property.
   */
  void visitMany(BeanPropertyAssocMany<?> p);

  /**
   * Visit the imported side of a OneToOne property.
   */
  void visitOneImported(BeanPropertyAssocOne<?> p);

  /**
   * Visit the exported side of a OneToOne property.
   */
  void visitOneExported(BeanPropertyAssocOne<?> p);

  /**
   * Visit an embedded property.
   */
  void visitEmbedded(BeanPropertyAssocOne<?> p);

  /**
   * Visit the scalar property of an embedded bean.
   */
  void visitEmbeddedScalar(BeanProperty p, BeanPropertyAssocOne<?> embedded);

  /**
   * Visit a scalar property.
   */
  void visitScalar(BeanProperty p);

  /**
   * Visit a compound value object.
   */
  void visitCompound(BeanPropertyCompound p);

  /**
   * Visit the scalar value inside a compound value object.
   */
  void visitCompoundScalar(BeanPropertyCompound compound, BeanProperty p);

}
