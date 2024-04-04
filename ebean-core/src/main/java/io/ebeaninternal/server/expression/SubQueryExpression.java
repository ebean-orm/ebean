package io.ebeaninternal.server.expression;

import io.ebean.event.BeanQueryRequest;
import io.ebeaninternal.api.*;
import io.ebeaninternal.api.SpiQuery.Type;
import io.ebeaninternal.server.query.CQuery;

import java.util.List;

/**
 * Sub-Query expression.
 */
final class SubQueryExpression extends AbstractExpression implements UnsupportedDocStoreExpression {

  private final SubQueryOp op;
  private final SpiQuery<?> subQuery;
  private List<Object> bindParams;
  private String sql;

  SubQueryExpression(SubQueryOp op, String propertyName, SpiQuery<?> subQuery) {
    super(propertyName);
    this.op = op;
    this.subQuery = subQuery;
  }

  SubQueryExpression(SubQueryOp op, String propertyName, String sql, List<Object> bindParams) {
    super(propertyName);
    this.op = op;
    this.subQuery = null;
    this.sql = sql;
    this.bindParams = bindParams;
  }

  @Override
  public SpiExpression copy() {
    return subQuery == null ? this : new SubQueryExpression(op, propName, subQuery.copy());
  }

  @Override
  public void simplify() {
    // do nothing
  }

  @Override
  public void prepareExpression(BeanQueryRequest<?> request) {
    CQuery<?> subQuery = compileSubQuery(request);
    this.bindParams = subQuery.predicates().whereExprBindValues();
    this.sql = subQuery.generatedSql().replace('\n', ' ');
  }

  @Override
  public void queryPlanHash(StringBuilder builder) {
    builder.append("SubQuery[").append(propName).append(op.expression)
      .append(" sql:").append(sql)
      .append(" ?:").append(bindParams.size()).append(']');
  }

  /**
   * Compile/build the sub query.
   */
  private CQuery<?> compileSubQuery(BeanQueryRequest<?> queryRequest) {
    SpiEbeanServer ebeanServer = (SpiEbeanServer) queryRequest.database();
    return ebeanServer.compileQuery(Type.SQ_EX, subQuery, queryRequest.transaction());
  }

  @Override
  public void queryBindKey(BindValuesKey key) {
    subQuery.queryBindKey(key);
  }

  @Override
  public void addSql(SpiExpressionRequest request) {
    request.property(propName).append(op.expression).append('(').parse(sql).append(')');
  }

  @Override
  public void addBindValues(SpiExpressionBind request) {
    for (Object bindParam : bindParams) {
      request.addBindValue(bindParam);
    }
  }

  @Override
  public boolean isSameByBind(SpiExpression other) {
    SubQueryExpression that = (SubQueryExpression) other;
    if (this.bindParams.size() != that.bindParams.size()) {
      return false;
    }
    for (int i = 0; i < bindParams.size(); i++) {
      if (!bindParams.get(i).equals(that.bindParams.get(i))) {
        return false;
      }
    }
    return true;
  }
}
