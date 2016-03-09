package com.avaje.ebeaninternal.server.expression;

import com.avaje.ebean.search.BaseMatch;
import com.avaje.ebean.search.Match;
import com.avaje.ebeaninternal.api.HashQueryPlanBuilder;
import com.avaje.ebeaninternal.api.SpiExpression;
import com.avaje.ebeaninternal.api.SpiExpressionRequest;

import java.io.IOException;

/**
 * Full text MATCH expression.
 */
public class TextMatchExpression extends AbstractExpression {

  private final String search;

  private final Match options;

  public TextMatchExpression(String propertyName, String search, Match options) {
    super(propertyName);
    this.search = search;
    this.options = options;
  }

  @Override
  public void writeDocQuery(DocQueryContext context) throws IOException {
    context.writeMatch(propName, search, options);
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
    builder.add(TextMatchExpression.class).add(propName).add(search);
    if (options != null) {
      builder.add(options.isPhrase());
      builder.add(options.isPhrasePrefix());
      addHash(builder, options);
    }
  }

  @Override
  public int queryBindHash() {
    return search.hashCode();
  }

  @Override
  public boolean isSameByPlan(SpiExpression other) {
    if (!(other instanceof TextMatchExpression)) {
      return false;
    }

    TextMatchExpression that = (TextMatchExpression) other;
    return this.propName.equals(that.propName)
        && this.search.equals(that.search)
        && this.options == null ? that.options == null : options.equals(that.options);
  }

  @Override
  public boolean isSameByBind(SpiExpression other) {
    TextMatchExpression that = (TextMatchExpression) other;
    return search.equals(that.search);
  }

  /**
   * Add the hash to the builder for the base/common options.
   */
  public static void addHash(HashQueryPlanBuilder builder, BaseMatch options) {
    builder.add(options.isAnd());
    builder.add(options.getAnalyzer());
    builder.add(options.getBoost());
    builder.add(options.getCutoffFrequency());
    builder.add(options.getFuzziness());
    builder.add(options.getMaxExpansions());
    builder.add(options.getMinShouldMatch());
    builder.add(options.getPrefixLength());
    builder.add(options.getRewrite());
    builder.add(options.getZeroTerms());
  }
}
