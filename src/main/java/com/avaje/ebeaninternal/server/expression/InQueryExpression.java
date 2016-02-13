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

  private transient CQuery<?> compiledSubQuery;

  public InQueryExpression(String propertyName, SpiQuery<?> subQuery, boolean not) {
    super(propertyName);
    this.subQuery = subQuery;
    this.not = not;
  }

  @Override
  public void prepareExpression(BeanQueryRequest<?> request) {
    compiledSubQuery = compileSubQuery(request);
  }

  @Override
  public void queryPlanHash(HashQueryPlanBuilder builder) {
    builder.add(InQueryExpression.class).add(propName).add(not);
    subQuery.queryAutoTuneHash(builder);
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

    String subSelect = compiledSubQuery.getGeneratedSql();
    subSelect = subSelect.replace('\n', ' ');

    String propertyName = getPropertyName();
    request.append(" (").append(propertyName).append(")");
    if (not) {
      request.append(" not");
    }
    request.append(" in (");
    request.append(subSelect);
    request.append(") ");
  }

  @Override
  public void addBindValues(SpiExpressionRequest request) {

    List<Object> bindParams = compiledSubQuery.getPredicates().getWhereExprBindValues();

    if (bindParams == null) {
      return;
    }

    for (int i = 0; i < bindParams.size(); i++) {
      request.addBindValue(bindParams.get(i));
    }
  }
}
