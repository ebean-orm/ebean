package io.ebeaninternal.server.expression;

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
 * In a collection of Id values.
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

    idBinder.addIdInBindValues(request, idCollection);
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
  public void queryPlanHash(StringBuilder builder) {
    builder.append("IdIn[").append("?").append(idCollection.size()).append("]");
  }

  @Override
  public int queryBindHash() {
    return idCollection.hashCode();
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
