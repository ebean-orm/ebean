package com.avaje.ebeaninternal.server.el;

import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebean.plugin.ExpressionPath;

/**
 * The expression language object that can get values.
 * <p>
 * This can be used for local sorting and filtering.
 * </p>
 */
public interface ElPropertyValue extends ElPropertyDeploy, ExpressionPath {

  /**
   * Return the Id values for the given bean value.
   */
  Object[] getAssocOneIdValues(EntityBean bean);

  /**
   * Return the Id expression string.
   * <p>
   * Typically used to produce id = ? expression strings.
   * </p>
   */
  String getAssocOneIdExpr(String prefix, String operator);

  /**
   * Return the logical id value expression taking into account embedded id's.
   */
  String getAssocIdInValueExpr(int size);

  /**
   * Return the logical id in expression taking into account embedded id's.
   */
  String getAssocIdInExpr(String prefix);

  /**
   * Return true if this is an ManyToOne or OneToOne associated bean property.
   */
  boolean isAssocId();

  /**
   * Return true if any path of this path contains a Associated One or Many.
   */
  boolean isAssocProperty();

  /**
   * Return true if the property is encrypted via Java.
   */
  boolean isLocalEncrypted();

  /**
   * Return true if the property is encrypted in the DB.
   */
  boolean isDbEncrypted();

  /**
   * Return the value ensuring objects prior to the top scalar property are
   * automatically populated.
   */
  Object elGetReference(EntityBean bean);

}
