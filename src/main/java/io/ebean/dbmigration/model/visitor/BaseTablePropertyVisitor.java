package io.ebean.dbmigration.model.visitor;

import io.ebean.plugin.Property;
import io.ebean.plugin.PropertyAssocMany;
import io.ebean.plugin.PropertyAssocOne;

/**
 * Used to help mark PropertyVisitor methods that need to be implemented
 * to visit base table properties.
 */
public abstract class BaseTablePropertyVisitor implements BeanPropertyVisitor {

  /**
   * Not required in that you can use the visitEmbeddedScalar.
   */
  @Override
  public void visitEmbedded(PropertyAssocOne p) {
  }

  /**
   * Override this method.
   */
  @Override
  public abstract void visitEmbeddedScalar(Property p, PropertyAssocOne embedded);

  /**
   * Not part of base table.
   */
  @Override
  public void visitMany(PropertyAssocMany p) {
  }

  /**
   * Not part of base table.
   */
  @Override
  public void visitOneExported(PropertyAssocOne p) {
  }

  /**
   * Override this method for the foreign key.
   */
  @Override
  public abstract void visitOneImported(PropertyAssocOne p);

  /**
   * Override this method for normal scalar property.
   */
  @Override
  public abstract void visitScalar(Property p);

}
