package com.avaje.ebeaninternal.server.expression;

import java.util.List;

import com.avaje.ebean.event.BeanQueryRequest;
import com.avaje.ebeaninternal.api.HashQueryPlanBuilder;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.api.SpiExpressionRequest;
import com.avaje.ebeaninternal.api.SpiQuery;
import com.avaje.ebeaninternal.server.query.CQuery;

/**
 * In expression using a sub query.
 */
class InQueryExpression extends AbstractExpression {

  private static final long serialVersionUID = 666990277309851644L;

  private final boolean not;

  private final SpiQuery<?> subQuery;

  private List<Object> bindParams;

  private String sql;

  public InQueryExpression(String propertyName, SpiQuery<?> subQuery, boolean not) {
    super(propertyName);
    this.subQuery = subQuery;
    this.not = not;
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

    request.append(" (").append(getPropertyName()).append(")");
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
}
