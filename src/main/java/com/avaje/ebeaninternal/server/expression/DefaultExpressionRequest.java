package com.avaje.ebeaninternal.server.expression;

import com.avaje.ebeaninternal.api.SpiExpressionList;
import com.avaje.ebeaninternal.api.SpiExpressionRequest;
import com.avaje.ebeaninternal.server.core.DbExpressionHandler;
import com.avaje.ebeaninternal.server.core.SpiOrmQueryRequest;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.deploy.DeployParser;
import com.avaje.ebeaninternal.server.persist.Binder;
import com.avaje.ebeaninternal.server.type.DataBind;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DefaultExpressionRequest implements SpiExpressionRequest {

  private final SpiOrmQueryRequest<?> queryRequest;

  private final BeanDescriptor<?> beanDescriptor;

  private final StringBuilder sql = new StringBuilder();

  private final List<Object> bindValues = new ArrayList<>();

  private final DeployParser deployParser;

  private final Binder binder;

  private final SpiExpressionList<?> expressionList;

  private int paramIndex;

  private StringBuilder bindLog;

  public DefaultExpressionRequest(SpiOrmQueryRequest<?> queryRequest, DeployParser deployParser, Binder binder, SpiExpressionList<?> expressionList) {
    this.queryRequest = queryRequest;
    this.beanDescriptor = queryRequest.getBeanDescriptor();
    this.deployParser = deployParser;
    this.binder = binder;
    this.expressionList = expressionList;
    // immediately build the list of bind values (callback style)
    expressionList.addBindValues(this);
  }

  public DefaultExpressionRequest(BeanDescriptor<?> beanDescriptor) {
    this.beanDescriptor = beanDescriptor;
    this.queryRequest = null;
    this.deployParser = null;
    this.binder = null;
    this.expressionList = null;
  }

  /**
   * Build sql for the underlying expression list.
   */
  public String buildSql() {
    expressionList.addSql(this);
    return sql.toString();
  }

  /**
   * Bind the values from the underlying expression list.
   */
  public void bind(DataBind dataBind) throws SQLException {
    for (int i = 0; i < bindValues.size(); i++) {
      Object bindValue = bindValues.get(i);
      binder.bindObject(dataBind, bindValue);
    }
    if (bindLog != null) {
      dataBind.append(bindLog.toString());
    }
  }

  @Override
  public DbExpressionHandler getDbPlatformHandler() {
    return binder.getDbExpressionHandler();
  }

  @Override
  public String parseDeploy(String logicalProp) {

    String s = deployParser.getDeployWord(logicalProp);
    return s == null ? logicalProp : s;
  }

  /**
   * Append the database platform like clause.
   */
  @Override
  public void appendLike() {
    sql.append(" ");
    sql.append(queryRequest.getDBLikeClause());
    sql.append(" ");
  }

  /**
   * Increments the parameter index and returns that value.
   */
  @Override
  public int nextParameter() {
    return ++paramIndex;
  }

  @Override
  public BeanDescriptor<?> getBeanDescriptor() {
    return beanDescriptor;
  }

  @Override
  public SpiOrmQueryRequest<?> getQueryRequest() {
    return queryRequest;
  }

  /**
   * Append text the underlying sql expression.
   */
  @Override
  public SpiExpressionRequest append(String sqlExpression) {
    sql.append(sqlExpression);
    return this;
  }

  @Override
  public void addBindEncryptKey(Object bindValue) {
    bindValues.add(bindValue);
    bindLog("****");
  }

  @Override
  public void addBindValue(Object bindValue) {
    bindValues.add(bindValue);
    bindLog(bindValue);
  }

  private void bindLog(Object val) {
    if (bindLog == null) {
      bindLog = new StringBuilder();
    } else {
      bindLog.append(",");
    }
    bindLog.append(val);
  }

  public String getBindLog() {
    return bindLog == null ? "" : bindLog.toString();
  }

  @Override
  public String getSql() {
    return sql.toString();
  }

  @Override
  public List<Object> getBindValues() {
    return bindValues;
  }

}
