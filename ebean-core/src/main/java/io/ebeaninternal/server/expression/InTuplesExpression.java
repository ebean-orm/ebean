package io.ebeaninternal.server.expression;

import io.ebean.InTuples;
import io.ebean.annotation.Platform;
import io.ebean.event.BeanQueryRequest;
import io.ebean.service.SpiInTuples;
import io.ebeaninternal.api.BindValuesKey;
import io.ebeaninternal.api.NaturalKeyQueryData;
import io.ebeaninternal.api.SpiExpression;
import io.ebeaninternal.api.SpiExpressionRequest;

import java.util.List;
import java.util.UUID;

import static java.util.Objects.requireNonNull;

final class InTuplesExpression extends AbstractExpression {

  private final boolean not;
  private final String[] properties;
  private final List<Object[]> entries;
  private InLiterals[] literals;
  private boolean literalMode;

  InTuplesExpression(InTuples pairs, boolean not) {
    super("");
    SpiInTuples inTuples = (SpiInTuples) pairs;
    this.properties = inTuples.properties();
    // the entries might be modified on cache hit.
    this.entries = inTuples.entries();
    this.not = not;
  }

  @Override
  public void prepareExpression(BeanQueryRequest<?> request) {
    if (entries.size() > 50) {
      // check if this should go into literal mode
      final int maxInBinding = request.database().pluginApi().databasePlatform().maxInBinding();
      final int threshold = literalThreshold(maxInBinding, properties.length);
      if (entries.size() > threshold) {
        literals = initLiterals(request.database().platform());
        literalMode = literals != null;
      }
    }
  }

  private InLiterals[] initLiterals(Platform platform) {
    try {
      Object[] firstData = entries.get(0);
      final InLiterals[] literals = new InLiterals[firstData.length];
      for (int i = 0; i < firstData.length; i++) {
        literals[i] = InLiterals.of(firstData[i], platform);
      }
      return literals;
    } catch (UnsupportedOperationException e) {
      // one of the value types is not supported as a SQL literal
      // so stick to binding only
      return null;
    }
  }

  /**
   * Return the threshold in number of entries before literal mode is used.
   */
  static int literalThreshold(int maxInBinding, int propertyCount) {
    if (maxInBinding == 0) {
      return 5000 / propertyCount;
    }
    return (maxInBinding / propertyCount) - 200;
  }

  @Override
  public boolean naturalKey(NaturalKeyQueryData<?> data) {
    return false;
  }

  @Override
  public void writeDocQuery(DocQueryContext context) {
    throw new RuntimeException("Not supported with document query");
  }

  @Override
  public void addBindValues(SpiExpressionRequest request) {
    if (literalMode) {
      return;
    }
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
    request.append("(");
    for (int i = 0; i < properties.length; i++) {
      if (i > 0) {
        request.append(",");
      }
      request.property(properties[i]);
    }
    request.append(") in (");
    if (literalMode) {
      addSqlLiterals(request);
    } else {
      addSqlBinding(request);
    }
    request.append(")");
  }

  private void addSqlLiterals(SpiExpressionRequest request) {
    final var buffer = request.buffer();
    for (int i = 0; i < entries.size(); i++) {
      if (i > 0) {
        buffer.append(',');
      }
      buffer.append('(');
      Object[] values = entries.get(i);
      for (int v = 0; v < values.length; v++) {
        if (v > 0) {
          buffer.append(',');
        }
        literals[v].append(buffer, values[v]);
      }
      buffer.append(')');
    }
  }

  private void addSqlBinding(SpiExpressionRequest request) {
    final String eb = entryBinding();
    for (int i = 0; i < entries.size(); i++) {
      if (i > 0) {
        request.append(",");
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
    if (literalMode) {
      builder.delete(0, builder.length());
      builder.append("$NoCache/").append(UUID.randomUUID()).append('/');
      return;
    }
    if (not) {
      builder.append("Not");
    }
    builder.append("InTuple[");
    for (String property : properties) {
      builder.append(property).append("-");
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
