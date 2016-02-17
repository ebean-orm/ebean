package com.avaje.ebeaninternal.server.expression;

import com.avaje.ebean.event.BeanQueryRequest;
import com.avaje.ebeaninternal.api.HashQueryPlanBuilder;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.api.SpiExpression;
import com.avaje.ebeaninternal.api.SpiExpressionRequest;
import com.avaje.ebeaninternal.api.SpiQuery;
import com.avaje.ebeaninternal.server.query.CQuery;

import java.util.List;

/**
 * In expression using a sub query.
 */
class InQueryExpression extends AbstractExpression {

  private static final long serialVersionUID = 666990277309851644L;

  private final boolean not;

  private final SpiQuery<?> subQuery;

  private List<Object> bindParams;

  private String sql;

  InQueryExpression(String propertyName, SpiQuery<?> subQuery, boolean not) {
    super(propertyName);
    this.subQuery = subQuery;
    this.not = not;
  }

  InQueryExpression(String propertyName, boolean not, String sql, List<Object> bindParams) {
    super(propertyName);
    this.subQuery = null;
    this.not = not;
    this.sql = sql;
    this.bindParams = bindParams;
  }

  @Override
  public void prepareExpression(BeanQueryRequest<?> request) {

    CQuery<?> subQuery = compileSubQuery(request);
    this.bindParams = subQuery.getPredicates().getWhereExprBindValues();
    this.sql = subQuery.getGeneratedSql().replace('\n', ' ');
  }

  @Override
  public void queryPlanHash(HashQueryPlanBuilder builder) {
    builder.add(InQueryExpression.class).add(propName).add(not);
    builder.add(sql).add(bindParams.size());
  }

  /**
   * Compile/build the sub query.
   */
  private CQuery<?> compileSubQuery(BeanQueryRequest<?> queryRequest) {

    SpiEbeanServer ebeanServer = (SpiEbeanServer) queryRequest.getEbeanServer();
    return ebeanServer.compileQuery(subQuery, queryRequest.getTransaction());
  }

  @Override
  public int queryBindHash() {
    return subQuery.queryBindHash();
  }

  @Override
  public void addSql(SpiExpressionRequest request) {

    request.append(" (").append(propName).append(")");
    if (not) {
      request.append(" not");
    }
    request.append(" in (");
    request.append(sql);
    request.append(") ");
  }

  @Override
  public void addBindValues(SpiExpressionRequest request) {

    for (int i = 0; i < bindParams.size(); i++) {
      request.addBindValue(bindParams.get(i));
    }
  }

  @Override
  public boolean isSameByPlan(SpiExpression other) {
    if (!(other instanceof InQueryExpression)) {
      return false;
    }

    InQueryExpression that = (InQueryExpression) other;
    return propName.equals(that.propName)
        && sql.equals(that.sql)
        && not == that.not
        && bindParams.size() == that.bindParams.size();
  }

  @Override
  public boolean isSameByBind(SpiExpression other) {
    InQueryExpression that = (InQueryExpression) other;
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
