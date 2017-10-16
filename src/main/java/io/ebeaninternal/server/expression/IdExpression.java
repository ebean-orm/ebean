package io.ebeaninternal.server.expression;

import io.ebeaninternal.api.ManyWhereJoins;
import io.ebeaninternal.api.SpiExpression;
import io.ebeaninternal.api.SpiExpressionRequest;
import io.ebeaninternal.api.SpiExpressionValidation;
import io.ebeaninternal.server.deploy.BeanDescriptor;

import java.io.IOException;

/**
 * Slightly redundant as Query.setId() ultimately also does the same job.
 */
class IdExpression extends NonPrepareExpression implements SpiExpression {

  private final Object value;

  IdExpression(Object value) {
    this.value = value;
  }

  @Override
  public void writeDocQuery(DocQueryContext context) throws IOException {
    context.writeId(value);
  }

  @Override
  public String nestedPath(BeanDescriptor<?> desc) {
    return null;
  }

  /**
   * Always returns false.
   */
  @Override
  public void containsMany(BeanDescriptor<?> desc, ManyWhereJoins manyWhereJoin) {

  }

  @Override
  public void validate(SpiExpressionValidation validation) {
    // always valid
  }

  @Override
  public void addBindValues(SpiExpressionRequest request) {

    // 'flatten' EmbeddedId and multiple Id cases
    // into an array of the underlying scalar field values
    DefaultExpressionRequest r = (DefaultExpressionRequest) request;
    Object[] bindIdValues = r.getBeanDescriptor().getBindIdValues(value);
    for (Object bindIdValue : bindIdValues) {
      request.addBindValue(bindIdValue);
    }
  }

  @Override
  public void addSql(SpiExpressionRequest request) {

    DefaultExpressionRequest r = (DefaultExpressionRequest) request;
    String idSql = r.getBeanDescriptor().getIdBinderIdSql(null);

    request.append(idSql).append(" ");
  }

  /**
   * No properties so this is just a unique static number.
   */
  @Override
  public void queryPlanHash(StringBuilder builder) {
    builder.append("Id[]");
  }

  @Override
  public int queryBindHash() {
    return value.hashCode();
  }

  @Override
  public boolean isSameByBind(SpiExpression other) {
    IdExpression that = (IdExpression) other;
    return value.equals(that.value);
  }
}
