package io.ebeaninternal.server.el;

import io.ebean.plugin.ExpressionPath;
import io.ebeaninternal.api.SpiExpressionRequest;

/**
 * The expression language object that can get values.
 * <p>
 * This can be used for local sorting and filtering.
 * </p>
 */
public interface ElPropertyValue extends ElPropertyDeploy, ExpressionPath {

  /**
   * Return the logical id value expression taking into account embedded id's.
   */
  String getAssocIdInValueExpr(boolean not, int size);

  /**
   * Return the logical id in expression taking into account embedded id's.
   */
  String getAssocIdInExpr(String prefix);

  /**
   * Return the logical where clause to support "Is empty".
   */
  String getAssocIsEmpty(SpiExpressionRequest request, String path);

  /**
   * Return true if this is an ManyToOne or OneToOne associated bean property.
   */
  @Override
  boolean isAssocId();

  /**
   * Return true if the property is a OneToMany or ManyToMany associated bean property.
   */
  boolean isAssocMany();

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
   * Encrypt the input value return the encrypted value.
   */
  Object localEncrypt(Object value);

  /**
   * Return the value ensuring objects prior to the top scalar property are
   * automatically populated.
   */
  Object pathGetNested(Object bean);

}
