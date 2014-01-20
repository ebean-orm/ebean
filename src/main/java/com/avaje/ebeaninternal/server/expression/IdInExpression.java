package com.avaje.ebeaninternal.server.expression;

import java.util.List;

import com.avaje.ebean.event.BeanQueryRequest;
import com.avaje.ebeaninternal.api.HashQueryPlanBuilder;
import com.avaje.ebeaninternal.api.ManyWhereJoins;
import com.avaje.ebeaninternal.api.SpiExpression;
import com.avaje.ebeaninternal.api.SpiExpressionRequest;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.deploy.id.IdBinder;
import com.avaje.ebeaninternal.util.DefaultExpressionRequest;

/**
 * Slightly redundant as Query.setId() ultimately also does the same job.
 */
public class IdInExpression implements SpiExpression {

  private static final long serialVersionUID = 1L;

  private final List<?> idList;

  public IdInExpression(List<?> idList) {
    this.idList = idList;
  }

  public void containsMany(BeanDescriptor<?> desc, ManyWhereJoins manyWhereJoin) {
  }

  public void addBindValues(SpiExpressionRequest request) {

    // Bind the Id values including EmbeddedId and multiple Id

    DefaultExpressionRequest r = (DefaultExpressionRequest) request;
    BeanDescriptor<?> descriptor = r.getBeanDescriptor();
    IdBinder idBinder = descriptor.getIdBinder();

    for (int i = 0; i < idList.size(); i++) {
      idBinder.addIdInBindValue(request, idList.get(i));
    }
  }

  /**
   * For use with deleting non attached detail beans during stateless update.
   */
  public void addSqlNoAlias(SpiExpressionRequest request) {

    DefaultExpressionRequest r = (DefaultExpressionRequest) request;
    BeanDescriptor<?> descriptor = r.getBeanDescriptor();
    IdBinder idBinder = descriptor.getIdBinder();

    request.append(descriptor.getIdBinder().getBindIdInSql(null));
    String inClause = idBinder.getIdInValueExpr(idList.size());
    request.append(inClause);
  }

  public void addSql(SpiExpressionRequest request) {

    DefaultExpressionRequest r = (DefaultExpressionRequest) request;
    BeanDescriptor<?> descriptor = r.getBeanDescriptor();
    IdBinder idBinder = descriptor.getIdBinder();

    request.append(descriptor.getIdBinderInLHSSql());
    String inClause = idBinder.getIdInValueExpr(idList.size());
    request.append(inClause);
  }

  /**
   * Incorporates the number of Id values to bind.
   */
  public void queryAutoFetchHash(HashQueryPlanBuilder builder) {
    builder.add(IdInExpression.class).add(idList.size());
    builder.bind(idList.size());
  }

  public void queryPlanHash(BeanQueryRequest<?> request, HashQueryPlanBuilder builder) {
    queryAutoFetchHash(builder);
  }

  public int queryBindHash() {
    return idList.hashCode();
  }

}
