package io.ebeaninternal.api;

import io.ebeaninternal.server.core.SpiOrmQueryRequest;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.expression.platform.DbExpressionHandler;

import java.util.List;

/**
 * Request object used for gathering expression sql and bind values.
 */
public interface SpiExpressionRequest {

  /**
   * Return the DB specific handler for JSON and ARRAY expressions.
   */
  DbExpressionHandler getDbPlatformHandler();

  /**
   * Parse the logical property name to the deployment name.
   */
  String parseDeploy(String logicalProp);

  /**
   * Return the bean descriptor for the root type.
   */
  BeanDescriptor<?> getBeanDescriptor();

  /**
   * Return the associated QueryRequest.
   */
  SpiOrmQueryRequest<?> getQueryRequest();

  /**
   * Append to the expression sql.
   */
  SpiExpressionRequest append(String sql);

  /**
   * Add an encryption key to bind to this request.
   */
  void addBindEncryptKey(Object encryptKey);

  /**
   * Add a bind value to this request.
   */
  void addBindValue(Object bindValue);

  /**
   * Return the accumulated expression sql for all expressions in this request.
   */
  String getSql();

  /**
   * Return the ordered list of bind values for all expressions in this request.
   */
  List<Object> getBindValues();

  /**
   * Increments the parameter index and returns that value.
   */
  int nextParameter();

  /**
   * Append a DB Like clause.
   */
  void appendLike(boolean rawLikeExpression);

  /**
   * Escapes a string to use it as exact match in Like clause.
   */
  String escapeLikeString(String value);

  /**
   * Append IN expression taking into account platform and type support for Multi-value.
   */
  void appendInExpression(boolean not, List<Object> bindValues);
}
