package com.avaje.ebeaninternal.server.expression;

import java.io.IOException;
import java.util.List;

import com.avaje.ebean.event.BeanQueryRequest;
import com.avaje.ebeaninternal.api.HashQueryPlanBuilder;
import com.avaje.ebeaninternal.api.ManyWhereJoins;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.api.SpiExpression;
import com.avaje.ebeaninternal.api.SpiExpressionRequest;
import com.avaje.ebeaninternal.api.SpiExpressionValidation;
import com.avaje.ebeaninternal.api.SpiQuery;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.query.CQuery;

class ExistsQueryExpression implements SpiExpression, UnsupportedDocStoreExpression {

  protected final boolean not;

  protected final SpiQuery<?> subQuery;

  protected List<Object> bindParams;

  protected String sql;

  public ExistsQueryExpression(SpiQuery<?> subQuery, boolean not) {
    this.subQuery = subQuery;
    this.not = not;
  }

  ExistsQueryExpression(boolean not, String sql , List<Object> bindParams) {
    this.not = not;
    this.sql = sql;
    this.bindParams = bindParams;
    this.subQuery = null;
  }

  @Override
  public void writeDocQuery(DocQueryContext context) throws IOException {
    throw new IllegalStateException("Not supported");
  }

  @Override
  public Object getIdEqualTo(String idName) {
    // always return null for this expression
    return null;
  }

  @Override
  public void prepareExpression(BeanQueryRequest<?> request) {

    CQuery<?> subQuery = compileSubQuery(request);
    this.bindParams = subQuery.getPredicates().getWhereExprBindValues();
    this.sql = subQuery.getGeneratedSql().replace('\n', ' ');
  }

  @Override
  public SpiExpression copyForPlanKey() {
    return this;
  }

  /**
   * Compile/build the sub query.
   */
  protected CQuery<?> compileSubQuery(BeanQueryRequest<?> queryRequest) {
    SpiEbeanServer ebeanServer = (SpiEbeanServer) queryRequest.getEbeanServer();
    return ebeanServer.compileQuery(subQuery, queryRequest.getTransaction());
  }

  @Override
  public void queryPlanHash(HashQueryPlanBuilder builder) {
    builder.add(ExistsQueryExpression.class).add(not);
    builder.add(sql).add(bindParams.size());
  }

  @Override
  public int queryBindHash() {
    return subQuery.queryBindHash();
  }

  @Override
  public void addSql(SpiExpressionRequest request) {

    if (not) {
      request.append(" not");
    }
    request.append(" exists (");
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
    if (!(other instanceof ExistsQueryExpression)) {
      return false;
    }

    ExistsQueryExpression that = (ExistsQueryExpression) other;
    return this.sql.equals(that.sql)
        && this.not == that.not
        && this.bindParams.size() == that.bindParams.size();
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
  public String nestedPath(BeanDescriptor<?> desc) {
    return null;
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
