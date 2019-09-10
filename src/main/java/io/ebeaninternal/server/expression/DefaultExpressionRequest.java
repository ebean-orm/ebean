package io.ebeaninternal.server.expression;

import io.ebeaninternal.api.SpiExpressionList;
import io.ebeaninternal.api.SpiExpressionRequest;
import io.ebeaninternal.server.core.SpiOrmQueryRequest;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.deploy.DeployParser;
import io.ebeaninternal.server.expression.platform.DbExpressionHandler;
import io.ebeaninternal.server.persist.Binder;
import io.ebeaninternal.server.type.DataBind;

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

  private final boolean enableBindLog;

  private StringBuilder bindLog;

  public DefaultExpressionRequest(SpiOrmQueryRequest<?> queryRequest, DeployParser deployParser, Binder binder, SpiExpressionList<?> expressionList) {
    this.queryRequest = queryRequest;
    this.beanDescriptor = queryRequest.getBeanDescriptor();
    this.deployParser = deployParser;
    this.binder = binder;
    this.expressionList = expressionList;
    this.enableBindLog = binder.isEnableBindLog();
    // immediately build the list of bind values (callback style)
    expressionList.addBindValues(this);
  }

  public DefaultExpressionRequest(BeanDescriptor<?> beanDescriptor) {
    this.beanDescriptor = beanDescriptor;
    this.queryRequest = null;
    this.deployParser = null;
    this.binder = null;
    this.expressionList = null;
    this.enableBindLog = true;
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
    for (Object bindValue : bindValues) {
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
  public void appendLike(boolean rawLikeExpression) {
    sql.append(" ");
    sql.append(queryRequest.getDBLikeClause(rawLikeExpression));
    sql.append(" ");
  }

  @Override
  public String escapeLikeString(String value) {
    return queryRequest.escapeLikeString(value);
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
    if (enableBindLog) {
      if (bindLog == null) {
        bindLog = new StringBuilder();
      } else {
        bindLog.append(",");
      }
      bindLog.append(val);
    }
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

  @Override
  public void appendInExpression(boolean not, List<Object> bindValues) {
    append(binder.getInExpression(not, bindValues));
  }
}
