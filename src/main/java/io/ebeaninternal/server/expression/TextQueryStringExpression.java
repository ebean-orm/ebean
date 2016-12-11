package io.ebeaninternal.server.expression;

import io.ebean.search.TextQueryString;

import java.io.IOException;

/**
 * Full text query string expression.
 */
class TextQueryStringExpression extends AbstractTextExpression {

  private final String search;

  private final TextQueryString options;

  public TextQueryStringExpression(String search, TextQueryString options) {
    super(null);
    this.search = search;
    this.options = options;
  }

  @Override
  public void writeDocQuery(DocQueryContext context) throws IOException {
    context.writeTextQueryString(search, options);
  }

}
