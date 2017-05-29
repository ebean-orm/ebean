package io.ebeaninternal.server.expression;

import io.ebeaninternal.api.HashQueryPlanBuilder;
import io.ebeaninternal.api.ManyWhereJoins;
import io.ebeaninternal.api.SpiExpression;
import io.ebeaninternal.api.SpiExpressionRequest;
import io.ebeaninternal.api.SpiExpressionValidation;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.deploy.id.IdBinder;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

/**
 * Slightly redundant as Query.setId() ultimately also does the same job.
 */
public class IdInExpression extends NonPrepareExpression {

  private final Collection<?> idCollection;

  public IdInExpression(Collection<?> idCollection) {
    this.idCollection = idCollection;
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
    context.writeIds(idCollection);
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

    for (Object id : idCollection) {
      idBinder.addIdInBindValue(request, id);
    }
  }

  /**
   * For use with deleting non attached detail beans during stateless update.
   */
  public void addSqlNoAlias(SpiExpressionRequest request) {

    DefaultExpressionRequest r = (DefaultExpressionRequest) request;
    BeanDescriptor<?> descriptor = r.getBeanDescriptor();
    IdBinder idBinder = descriptor.getIdBinder();
    if (idCollection.size() == 0) {
      request.append("1=0"); // append false for this stage
    } else {
      request.append(descriptor.getIdBinder().getBindIdInSql(null));
      String inClause = idBinder.getIdInValueExpr(idCollection.size());
      request.append(inClause);
    }
  }

  @Override
  public void addSql(SpiExpressionRequest request) {

    DefaultExpressionRequest r = (DefaultExpressionRequest) request;
    BeanDescriptor<?> descriptor = r.getBeanDescriptor();
    IdBinder idBinder = descriptor.getIdBinder();
    if (idCollection.size() == 0) {
      request.append("1=0"); // append false for this stage
    } else {
      request.append(descriptor.getIdBinderInLHSSql());
      String inClause = idBinder.getIdInValueExpr(idCollection.size());
      request.append(inClause);
    }
  }

  /**
   * Incorporates the number of Id values to bind.
   */
  @Override
  public void queryPlanHash(HashQueryPlanBuilder builder) {
    builder.add(IdInExpression.class).add(idCollection.size());
    builder.bind(idCollection.size());
  }

  @Override
  public int queryBindHash() {
    return idCollection.hashCode();
  }

  @Override
  public boolean isSameByPlan(SpiExpression other) {
    if (!(other instanceof IdInExpression)) {
      return false;
    }

    IdInExpression that = (IdInExpression) other;
    return this.idCollection.size() == that.idCollection.size();
  }

  @Override
  public boolean isSameByBind(SpiExpression other) {
    IdInExpression that = (IdInExpression) other;
    if (this.idCollection.size() != that.idCollection.size()) {
      return false;
    }
    Iterator<?> it = that.idCollection.iterator();
    for (Object id1 : idCollection) {
      Object id2 = it.next();
      if (!id1.equals(id2)) {
        return false;
      }
    }
    return true;
  }
}
