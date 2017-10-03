package io.ebean.dbmigration.model.visitor;

import io.ebean.plugin.Property;
import io.ebean.plugin.PropertyAssocMany;
import io.ebean.plugin.PropertyAssocOne;

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
  void visitMany(PropertyAssocMany p);

  /**
   * Visit the imported side of a OneToOne property.
   */
  void visitOneImported(PropertyAssocOne p);

  /**
   * Visit the exported side of a OneToOne property.
   */
  void visitOneExported(PropertyAssocOne p);

  /**
   * Visit an embedded property.
   */
  void visitEmbedded(PropertyAssocOne p);

  /**
   * Visit the scalar property of an embedded bean.
   */
  void visitEmbeddedScalar(Property p, PropertyAssocOne embedded);

  /**
   * Visit a scalar property.
   */
  void visitScalar(Property p);

}
