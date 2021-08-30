package io.ebeaninternal.server.expression;

import io.ebean.search.MultiMatch;

import java.io.IOException;

/**
 * Full text Multi-Match expression.
 */
final class TextMultiMatchExpression extends AbstractTextExpression {

  private final String search;
  private final MultiMatch options;

  TextMultiMatchExpression(String search, MultiMatch options) {
    super(null);
    this.search = search;
    this.options = options;
  }

  @Override
  public void writeDocQuery(DocQueryContext context) throws IOException {
    context.writeMultiMatch(search, options);
  }

}
