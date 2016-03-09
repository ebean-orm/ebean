package com.avaje.ebeaninternal.server.expression;

import com.avaje.ebean.search.MultiMatch;
import com.avaje.ebeaninternal.api.HashQueryPlanBuilder;
import com.avaje.ebeaninternal.api.SpiExpression;
import com.avaje.ebeaninternal.api.SpiExpressionRequest;

import java.io.IOException;

/**
 * Full text Multi-Match expression.
 */
public class TextMultiMatchExpression extends AbstractExpression {

  private final String search;

  private final MultiMatch options;

  public TextMultiMatchExpression(String search, MultiMatch options) {
    super(null);
    this.search = search;
    this.options = options;
  }

  @Override
  public void writeDocQuery(DocQueryContext context) throws IOException {
    context.writeMultiMatch(search, options);
  }

  @Override
  public void addSql(SpiExpressionRequest request) {
    throw new IllegalStateException("Not implemented - DocStore/Elastic only");
  }

  @Override
  public void addBindValues(SpiExpressionRequest request) {
    throw new IllegalStateException("Not implemented - DocStore/Elastic only");
  }

  /**
   * Based on the type and propertyName.
   */
  @Override
  public void queryPlanHash(HashQueryPlanBuilder builder) {
    builder.add(TextMultiMatchExpression.class).add(search);
    builder.add(options.getType());
    builder.add(options.getTieBreaker());
    String[] fields = options.getFields();
    builder.add(fields.length);
    for (int i = 0; i < fields.length; i++) {
      builder.add(fields[i]);
    }
    TextMatchExpression.addHash(builder, options);
  }

  @Override
  public int queryBindHash() {
    return search.hashCode();
  }

  @Override
  public boolean isSameByPlan(SpiExpression other) {
    if (!(other instanceof TextMultiMatchExpression)) {
      return false;
    }

    TextMultiMatchExpression that = (TextMultiMatchExpression) other;
    return this.search.equals(that.search)
        && this.options == null ? that.options == null : options.equals(that.options);
  }

  @Override
  public boolean isSameByBind(SpiExpression other) {
    TextMultiMatchExpression that = (TextMultiMatchExpression) other;
    return search.equals(that.search);
  }
}
