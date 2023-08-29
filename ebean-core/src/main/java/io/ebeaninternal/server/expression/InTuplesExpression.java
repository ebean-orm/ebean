package io.ebeaninternal.server.expression;

import io.ebean.InTuples;
import io.ebean.service.SpiInTuples;
import io.ebeaninternal.api.BindValuesKey;
import io.ebeaninternal.api.NaturalKeyQueryData;
import io.ebeaninternal.api.SpiExpression;
import io.ebeaninternal.api.SpiExpressionRequest;

import java.util.List;

import static java.util.Objects.requireNonNull;

final class InTuplesExpression extends AbstractExpression {

  private final boolean not;
  private final String[] properties;
  private final List<Object[]> entries;

  InTuplesExpression(InTuples pairs, boolean not) {
    super("");
    SpiInTuples inTuples = (SpiInTuples) pairs;
    this.properties = inTuples.properties();
    // the entries might be modified on cache hit.
    this.entries = inTuples.entries();
    this.not = not;
  }

  @Override
  public boolean naturalKey(NaturalKeyQueryData<?> data) {
    return false;
  }

  @Override
  public void addBindValues(SpiExpressionRequest request) {
    for (Object[] entry : entries) {
      for (Object value : entry) {
        requireNonNull(value);
        request.addBindValue(value);
      }
    }
  }

  @Override
  public void addSql(SpiExpressionRequest request) {
    if (entries.isEmpty()) {
      request.append(not ? SQL_TRUE : SQL_FALSE);
      return;
    }
    request.append('(');
    for (int i = 0; i < properties.length; i++) {
      if (i > 0) {
        request.append(',');
      }
      request.property(properties[i]);
    }
    request.append(") in (");
    addSqlBinding(request);
    request.append(')');
  }

  private void addSqlBinding(SpiExpressionRequest request) {
    final String eb = entryBinding();
    for (int i = 0; i < entries.size(); i++) {
      if (i > 0) {
        request.append(',');
      }
      request.append(eb);
    }
  }

  private String entryBinding() {
    StringBuilder sb = new StringBuilder();
    sb.append('(');
    for (int i = 0; i < properties.length; i++) {
      if (i > 0) {
        sb.append(',');
      }
      sb.append('?');
    }
    return sb.append(')').toString();
  }

  /**
   * Based on the number of values in the in clause.
   */
  @Override
  public void queryPlanHash(StringBuilder builder) {
    if (not) {
      builder.append("Not");
    }
    builder.append("InTuple[");
    for (String property : properties) {
      builder.append(property).append('-');
    }
    builder.append(entries.size()).append(']');
  }

  @Override
  public void queryBindKey(BindValuesKey key) {
    key.add(entries.size());
    for (Object[] entry : entries) {
      for (Object value : entry) {
        key.add(value);
      }
    }
  }

  @Override
  public boolean isSameByBind(SpiExpression other) {
    InTuplesExpression that = (InTuplesExpression) other;
    return this.entries.size() == that.entries.size() && entries.equals(that.entries);
  }
}
