package com.avaje.ebeaninternal.util;

import java.util.ArrayList;

import com.avaje.ebeaninternal.api.SpiExpressionRequest;
import com.avaje.ebeaninternal.server.core.JsonExpressionHandler;
import com.avaje.ebeaninternal.server.core.SpiOrmQueryRequest;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.deploy.DeployParser;
import com.avaje.ebeaninternal.server.persist.Binder;

public class DefaultExpressionRequest implements SpiExpressionRequest {

  private final SpiOrmQueryRequest<?> queryRequest;

  private final BeanDescriptor<?> beanDescriptor;

  private final StringBuilder sb = new StringBuilder();

  private final ArrayList<Object> bindValues = new ArrayList<Object>();

  private final DeployParser deployParser;

  private final Binder binder;

  private int paramIndex;

  public DefaultExpressionRequest(SpiOrmQueryRequest<?> queryRequest, DeployParser deployParser, Binder binder) {
    this.queryRequest = queryRequest;
    this.beanDescriptor = queryRequest.getBeanDescriptor();
    this.deployParser = deployParser;
    this.binder = binder;
  }

  public DefaultExpressionRequest(BeanDescriptor<?> beanDescriptor) {
    this.beanDescriptor = beanDescriptor;
    this.queryRequest = null;
    this.deployParser = null;
    this.binder = null;
  }

  public JsonExpressionHandler getJsonHander() {
    return binder.getJsonExpressionHandler();
  }

  public String parseDeploy(String logicalProp) {

    String s = deployParser.getDeployWord(logicalProp);
    return s == null ? logicalProp : s;
  }

  /**
   * Append the database platform like clause.
   */
  @Override
  public void appendLike() {
    sb.append(" ");
    sb.append(queryRequest.getDBLikeClause());
    sb.append(" ");
  }

  /**
   * Increments the parameter index and returns that value.
   */
  public int nextParameter() {
    return ++paramIndex;
  }

  public BeanDescriptor<?> getBeanDescriptor() {
    return beanDescriptor;
  }

  public SpiOrmQueryRequest<?> getQueryRequest() {
    return queryRequest;
  }

  public SpiExpressionRequest append(String sql) {
    sb.append(sql);
    return this;
  }

  public void addBindValue(Object bindValue) {
    bindValues.add(bindValue);
  }

  public String getSql() {
    return sb.toString();
  }

  public ArrayList<Object> getBindValues() {
    return bindValues;
  }

}
