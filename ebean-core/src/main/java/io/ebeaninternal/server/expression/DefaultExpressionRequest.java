package io.ebeaninternal.server.expression;

import io.ebeaninternal.api.SpiExpressionList;
import io.ebeaninternal.api.SpiExpressionRequest;
import io.ebeaninternal.server.bind.DataBind;
import io.ebeaninternal.server.core.SpiOrmQueryRequest;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.deploy.DeployParser;
import io.ebeaninternal.server.expression.platform.DbExpressionHandler;
import io.ebeaninternal.server.persist.Binder;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public final class DefaultExpressionRequest implements SpiExpressionRequest {

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
    this.beanDescriptor = queryRequest.descriptor();
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
  public DbExpressionHandler platformHandler() {
    return binder.getDbExpressionHandler();
  }

  @Override
  public String parseDeploy(String logicalProp) {
    String s = deployParser.deployWord(logicalProp);
    return s == null ? logicalProp : s;
  }

  /**
   * Append the database platform like clause.
   */
  @Override
  public void appendLike(boolean rawLikeExpression) {
    sql.append(" ");
    sql.append(queryRequest.dbLikeClause(rawLikeExpression));
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
  public BeanDescriptor<?> descriptor() {
    return beanDescriptor;
  }

  @Override
  public SpiOrmQueryRequest<?> queryRequest() {
    return queryRequest;
  }

  @Override
  public StringBuilder buffer() {
    return sql;
  }

  /**
   * Append text the underlying sql expression.
   */
  @Override
  public SpiExpressionRequest append(String expression) {
    sql.append(expression);
    return this;
  }

  @Override
  public SpiExpressionRequest property(String expression) {
    if (deployParser == null) {
      sql.append(expression);
    } else {
      sql.append(deployParser.property(expression));
    }
    return this;
  }

  @Override
  public SpiExpressionRequest parse(String expression) {
    if (deployParser == null) {
      sql.append(expression);
    } else {
      sql.append(deployParser.parse(expression));
    }
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

  public String bindLog() {
    return bindLog == null ? "" : bindLog.toString();
  }

  @Override
  public String sql() {
    return sql.toString();
  }

  @Override
  public List<Object> bindValues() {
    return bindValues;
  }

  @Override
  public void appendInExpression(boolean not, List<Object> bindValues) {
    append(binder.getInExpression(not, bindValues));
  }
}
