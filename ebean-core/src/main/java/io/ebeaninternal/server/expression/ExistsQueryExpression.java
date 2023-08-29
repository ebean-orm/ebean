package io.ebeaninternal.server.expression;

import io.ebean.event.BeanQueryRequest;
import io.ebeaninternal.api.*;
import io.ebeaninternal.api.SpiQuery.Type;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.query.CQuery;

import java.util.List;

final class ExistsQueryExpression implements SpiExpression, UnsupportedDocStoreExpression {

  private final boolean not;
  private final SpiQuery<?> subQuery;
  private List<Object> bindParams;
  private String sql;

  ExistsQueryExpression(SpiQuery<?> subQuery, boolean not) {
    this.subQuery = subQuery;
    this.not = not;
  }

  ExistsQueryExpression(boolean not, String sql, List<Object> bindParams) {
    this.not = not;
    this.sql = sql;
    this.bindParams = bindParams;
    this.subQuery = null;
  }

  @Override
  public SpiExpression copy() {
    return subQuery == null ? this : new ExistsQueryExpression(subQuery.copy(), not);
  }

  @Override
  public void prefixProperty(String path) {
    // do nothing
  }

  @Override
  public boolean naturalKey(NaturalKeyQueryData<?> data) {
    // can't use naturalKey cache
    return false;
  }

  @Override
  public void simplify() {
    // do nothing
  }

  @Override
  public Object getIdEqualTo(String idName) {
    // always return null for this expression
    return null;
  }

  @Override
  public void prepareExpression(BeanQueryRequest<?> request) {
    CQuery<?> subQuery = compileSubQuery(request);
    this.bindParams = subQuery.predicates().whereExprBindValues();
    this.sql = subQuery.generatedSql().replace('\n', ' ');
  }

  @Override
  public SpiExpression copyForPlanKey() {
    return this;
  }

  /**
   * Compile/build the sub query.
   */
  CQuery<?> compileSubQuery(BeanQueryRequest<?> queryRequest) {
    SpiEbeanServer ebeanServer = (SpiEbeanServer) queryRequest.database();
    return ebeanServer.compileQuery(Type.SQ_EXISTS, subQuery, queryRequest.transaction());
  }

  @Override
  public void queryPlanHash(StringBuilder builder) {
    builder.append("ExistsQuery[").append(" not:").append(not);
    builder.append(" sql:").append(sql).append(" ?:").append(bindParams.size()).append(']');
  }

  @Override
  public void queryBindKey(BindValuesKey key) {
    subQuery.queryBindKey(key);
  }

  @Override
  public void addSql(SpiExpressionRequest request) {
    if (not) {
      request.append("not ");
    }
    request.append("exists (").parse(sql).append(')');
  }

  @Override
  public void addBindValues(SpiExpressionRequest request) {
    for (Object bindParam : bindParams) {
      request.addBindValue(bindParam);
    }
  }

  @Override
  public boolean isSameByBind(SpiExpression other) {
    ExistsQueryExpression that = (ExistsQueryExpression) other;
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

  @Override
  public void containsMany(BeanDescriptor<?> desc, ManyWhereJoins whereManyJoins) {
    // Nothing to do for exists expression
  }

  @Override
  public void validate(SpiExpressionValidation validation) {
    // Nothing to do for exists expression
  }
}
