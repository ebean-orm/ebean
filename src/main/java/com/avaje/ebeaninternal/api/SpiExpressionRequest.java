package com.avaje.ebeaninternal.api;

import java.util.ArrayList;

import com.avaje.ebeaninternal.server.core.SpiOrmQueryRequest;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;

/**
 * Request object used for gathering expression sql and bind values.
 */
public interface SpiExpressionRequest {

  /**
   * Parse the logical property name to the deployment name.
   */
  public String parseDeploy(String logicalProp);

	/**
	 * Return the bean descriptor for the root type.
	 */
	public BeanDescriptor<?> getBeanDescriptor();
	
	/**
	 * Return the associated QueryRequest.
	 */
	public SpiOrmQueryRequest<?> getQueryRequest();
	
	/**
	 * Append to the expression sql.
	 */
	public SpiExpressionRequest append(String sql);
	
	/**
	 * Add a bind value to this request.
	 */
	public void addBindValue(Object bindValue);
	
	/**
	 * Return the accumulated expression sql for all expressions in this request.
	 */
	public String getSql();
	
	/**
	 * Return the ordered list of bind values for all expressions in this request.
	 */
	public ArrayList<Object> getBindValues();

  /**
   * Increments the parameter index and returns that value.
   */
  public int nextParameter();

  /**
   * Append a DB Like clause.
   */
  public void appendLike();
}
