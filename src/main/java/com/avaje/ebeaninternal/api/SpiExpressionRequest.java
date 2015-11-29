package com.avaje.ebeaninternal.api;

import java.util.ArrayList;

import com.avaje.ebeaninternal.server.core.JsonExpressionHandler;
import com.avaje.ebeaninternal.server.core.SpiOrmQueryRequest;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;

/**
 * Request object used for gathering expression sql and bind values.
 */
public interface SpiExpressionRequest {

  /**
   * Return the DB specific JSON expression handler.
   */
  JsonExpressionHandler getJsonHandler();

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
	ArrayList<Object> getBindValues();

  /**
   * Increments the parameter index and returns that value.
   */
  int nextParameter();

  /**
   * Append a DB Like clause.
   */
  void appendLike();
}
