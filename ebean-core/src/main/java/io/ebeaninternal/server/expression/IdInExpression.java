package io.ebeaninternal.server.expression;

import io.ebean.event.BeanQueryRequest;
import io.ebeaninternal.api.*;
import io.ebeaninternal.server.core.BindPadding;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.deploy.id.IdBinder;

import java.io.IOException;
import java.util.*;

/**
 * In a collection of ID values.
 */
public final class IdInExpression extends NonPrepareExpression implements IdInCommon {

  private final List<Object> idCollection;
  private boolean multiValueIdSupported;

  public IdInExpression(Collection<?> idCollection) {
    this.idCollection = new ArrayList<>(idCollection);
  }

  @Override
  public Collection<Object> idValues() {
    return idCollection;
  }

  @Override
  public int removeIds(Set<Object> hitIds) {
    idCollection.removeAll(hitIds);
    return idCollection.size();
  }

  @Override
  public void prepareExpression(BeanQueryRequest<?> request) {
    multiValueIdSupported = request.isMultiValueIdSupported();
    if (!multiValueIdSupported && !idCollection.isEmpty() && request.isPadInExpression()) {
      // pad out the ids for better hit ratio on DB query plans
      BindPadding.padIds(idCollection);
    }
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
  public void addBindValues(SpiExpressionBind request) {
    if (idCollection.isEmpty()) {
      return;
    }
    // Bind the ID values including EmbeddedId and multiple ID
    request.descriptor().idBinder().addBindValues(request, idCollection);
  }

  /**
   * For use with deleting non-attached detail beans during stateless update.
   */
  public void addSqlNoAlias(SpiExpressionRequest request) {
    if (idCollection.isEmpty()) {
      request.append(SQL_FALSE); // append false for this stage
    } else {
      final BeanDescriptor<?> descriptor = request.descriptor();
      request.property(descriptor.idBinder().bindInSql(null));
      request.append(descriptor.idBinder().idInValueExpr(false, idCollection.size()));
    }
  }

  @Override
  public void addSql(SpiExpressionRequest request) {
    if (idCollection.isEmpty()) {
      request.append(SQL_FALSE); // append false for this stage
    } else {
      final BeanDescriptor<?> descriptor = request.descriptor();
      final IdBinder idBinder = descriptor.idBinder();
      if (idBinder.isComplexId()) {
        request.parse(descriptor.idBinderInLHSSql());
        request.append(idBinder.idInValueExpr(false, idCollection.size()));
      } else {
        request.property(idBinder.beanProperty().name());
        request.appendInExpression(false, idCollection);
      }
    }
  }

  /**
   * Incorporates the number of ID values to bind.
   */
  @Override
  public void queryPlanHash(StringBuilder builder) {
    builder.append("IdIn[?");
    if (!multiValueIdSupported || idCollection.isEmpty()) {
      // query plan specific to the number of parameters in the IN clause
      builder.append(idCollection.size());
    }
    builder.append(']');
  }

  @Override
  public void queryBindKey(BindValuesKey key) {
    key.add(idCollection.size());
    for (Object elem : idCollection) {
      key.add(elem);
    }
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
