package io.ebeaninternal.dbmigration.model.visitor;

import io.ebeaninternal.server.deploy.BeanProperty;
import io.ebeaninternal.server.deploy.BeanPropertyAssocMany;
import io.ebeaninternal.server.deploy.BeanPropertyAssocOne;

/**
 * Used to help mark PropertyVisitor methods that need to be implemented
 * to visit base table properties.
 */
public abstract class BaseTablePropertyVisitor implements BeanPropertyVisitor {

  /**
   * Not required in that you can use the visitEmbeddedScalar.
   */
  @Override
  public void visitEmbedded(BeanPropertyAssocOne<?> p) {
  }

  /**
   * Override this method.
   */
  @Override
  public abstract void visitEmbeddedScalar(BeanProperty p, BeanPropertyAssocOne<?> embedded);

  /**
   * Not part of base table.
   */
  @Override
  public void visitMany(BeanPropertyAssocMany<?> p) {
  }

  /**
   * Not part of base table.
   */
  @Override
  public void visitOneExported(BeanPropertyAssocOne<?> p) {
  }

  /**
   * Override this method for the foreign key.
   */
  @Override
  public abstract void visitOneImported(BeanPropertyAssocOne<?> p);

  /**
   * Override this method for normal scalar property.
   */
  @Override
  public abstract void visitScalar(BeanProperty p);

}
