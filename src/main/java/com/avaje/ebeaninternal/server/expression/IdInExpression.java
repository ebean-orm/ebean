package com.avaje.ebeaninternal.server.expression;

import com.avaje.ebeaninternal.api.*;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.deploy.id.IdBinder;

import java.io.IOException;
import java.util.List;

/**
 * Slightly redundant as Query.setId() ultimately also does the same job.
 */
public class IdInExpression extends NonPrepareExpression {

  private final List<?> idList;

  public IdInExpression(List<?> idList) {
    this.idList = idList;
  }

  @Override
  public String nestedPath(BeanDescriptor<?> desc) {
    return null;
  }

  @Override
  public void containsMany(BeanDescriptor<?> desc, ManyWhereJoins manyWhereJoin) {
  }

  @Override
  public void writeDocQuery(DocQueryContext context) throws IOException {
    context.writeIds(idList);
  }

  @Override
  public void validate(SpiExpressionValidation validation) {
    // always valid
  }

  @Override
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

  @Override
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
  @Override
  public void queryPlanHash(HashQueryPlanBuilder builder) {
    builder.add(IdInExpression.class).add(idList.size());
    builder.bind(idList.size());
  }

  @Override
  public int queryBindHash() {
    return idList.hashCode();
  }

  @Override
  public boolean isSameByPlan(SpiExpression other) {
    if (!(other instanceof IdInExpression)) {
      return false;
    }

    IdInExpression that = (IdInExpression) other;
    return this.idList.size() == that.idList.size();
  }

  @Override
  public boolean isSameByBind(SpiExpression other) {
    IdInExpression that = (IdInExpression) other;
    if (this.idList.size() != that.idList.size()) {
      return false;
    }
    for (int i = 0; i < idList.size(); i++) {
      if (!idList.get(i).equals(that.idList.get(i))) {
        return false;
      }
    }
    return true;
  }
}
