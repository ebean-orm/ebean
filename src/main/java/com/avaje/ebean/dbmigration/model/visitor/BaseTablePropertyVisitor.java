package com.avaje.ebean.dbmigration.model.visitor;

import com.avaje.ebeaninternal.server.deploy.BeanProperty;
import com.avaje.ebeaninternal.server.deploy.BeanPropertyAssocMany;
import com.avaje.ebeaninternal.server.deploy.BeanPropertyAssocOne;
import com.avaje.ebeaninternal.server.deploy.BeanPropertyCompound;

/**
 * Used to help mark PropertyVisitor methods that need to be implemented
 * to visit base table properties.
 */
public abstract class BaseTablePropertyVisitor implements BeanPropertyVisitor {

  /**
   * Not required in that you can use the visitEmbeddedScalar.
   */
  public void visitEmbedded(BeanPropertyAssocOne<?> p) {
  }

  /**
   * Override this method.
   */
  public abstract void visitEmbeddedScalar(BeanProperty p, BeanPropertyAssocOne<?> embedded);

  /**
   * Not part of base table.
   */
  public void visitMany(BeanPropertyAssocMany<?> p) {
  }

  /**
   * Not part of base table.
   */
  public void visitOneExported(BeanPropertyAssocOne<?> p) {
  }

  /**
   * Override this method for the foreign key.
   */
  public abstract void visitOneImported(BeanPropertyAssocOne<?> p);

  /**
   * Override this method for normal scalar property.
   */
  public abstract void visitScalar(BeanProperty p);

  /**
   * Not required in that the scalar properties map to the columns.
   */
  public void visitCompound(BeanPropertyCompound p) {
  }

  /**
   * Override this method for scalar property inside a Immutable Compound Value object.
   */
  public abstract void visitCompoundScalar(BeanPropertyCompound compound, BeanProperty p);


}
