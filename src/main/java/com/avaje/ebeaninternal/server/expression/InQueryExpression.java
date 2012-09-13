package com.avaje.ebeaninternal.server.expression;

import java.util.List;

import com.avaje.ebean.event.BeanQueryRequest;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.api.SpiExpressionRequest;
import com.avaje.ebeaninternal.api.SpiQuery;
import com.avaje.ebeaninternal.server.query.CQuery;

/**
 * In expression using a sub query.
 * 
 * @authors Mario and Rob
 */
class InQueryExpression extends AbstractExpression {

  private static final long serialVersionUID = 666990277309851644L;

  private final SpiQuery<?> subQuery;

  private transient CQuery<?> compiledSubQuery;

  public InQueryExpression(FilterExprPath pathPrefix, String propertyName, SpiQuery<?> subQuery) {
    super(pathPrefix, propertyName);
    this.subQuery = subQuery;
  }

  public int queryAutoFetchHash() {
    int hc = InQueryExpression.class.getName().hashCode();
    hc = hc * 31 + propName.hashCode();
    hc = hc * 31 + subQuery.queryAutofetchHash();
    return hc;
  }

  public int queryPlanHash(BeanQueryRequest<?> request) {

    // queryPlanHash executes prior to addSql() or addBindValues()
    // ... so compiledQuery will exist
    compiledSubQuery = compileSubQuery(request);

    int hc = InQueryExpression.class.getName().hashCode();
    hc = hc * 31 + propName.hashCode();
    hc = hc * 31 + subQuery.queryPlanHash(request);
    return hc;
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

    String propertyName = getPropertyName();
    request.append(" (");
    request.append(propertyName);
    request.append(") in (");
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
}
