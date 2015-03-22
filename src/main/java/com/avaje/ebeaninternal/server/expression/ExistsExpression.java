package com.avaje.ebeaninternal.server.expression;

import java.util.List;

import com.avaje.ebean.event.BeanQueryRequest;
import com.avaje.ebeaninternal.api.HashQueryPlanBuilder;
import com.avaje.ebeaninternal.api.ManyWhereJoins;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.api.SpiExpression;
import com.avaje.ebeaninternal.api.SpiExpressionRequest;
import com.avaje.ebeaninternal.api.SpiQuery;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.query.CQuery;

public class ExistsExpression implements SpiExpression {

  private static final long serialVersionUID = 666990277309851644L;

  private final boolean not;
  private final SpiQuery<?> subQuery;

  private transient CQuery<?> compiledSubQuery;

  public ExistsExpression(SpiQuery<?> subQuery, boolean not) {
    this.subQuery = subQuery;
    this.not = not;
  }

  public void queryAutoFetchHash(HashQueryPlanBuilder builder) {
    builder.add(ExistsExpression.class).add(not);

    subQuery.queryAutofetchHash(builder);
  }

  public void queryPlanHash(BeanQueryRequest<?> request, HashQueryPlanBuilder builder) {

    // queryPlanHash executes prior to addSql() or addBindValues()
    // ... so compiledQuery will exist
    compiledSubQuery = compileSubQuery(request);

    queryAutoFetchHash(builder);
  }

  /**
   * Compile/build the sub query.
   */
  private CQuery<?> compileSubQuery(BeanQueryRequest<?> queryRequest) {

    SpiEbeanServer ebeanServer = (SpiEbeanServer) queryRequest.getEbeanServer();
    return ebeanServer.compileQuery(subQuery, queryRequest.getTransaction());
  }

  public int queryBindHash() {
    return subQuery.queryBindHash();
  }

  public void addSql(SpiExpressionRequest request) {

    String subSelect = compiledSubQuery.getGeneratedSql();
    subSelect = subSelect.replace('\n', ' ');

    if (not) {
      request.append(" not");
    }
    request.append(" exists (");
    request.append(subSelect);
    request.append(") ");
  }

  public void addBindValues(SpiExpressionRequest request) {

    List<Object> bindParams = compiledSubQuery.getPredicates().getWhereExprBindValues();

    if (bindParams == null) {
      return;
    }

    for (int i = 0; i < bindParams.size(); i++) {
      request.addBindValue(bindParams.get(i));
    }
  }

  @Override
  public void containsMany(BeanDescriptor<?> desc, ManyWhereJoins whereManyJoins) {
    // Nothing to do for exists expression
  }
}
