package io.ebeaninternal.server.expression;

import io.ebean.search.TextCommonTerms;

import java.io.IOException;

/**
 * Full text common terms expression.
 */
class TextCommonTermsExpression extends AbstractTextExpression {

  private final String search;

  private final TextCommonTerms options;

  public TextCommonTermsExpression(String search, TextCommonTerms options) {
    super(null);
    this.search = search;
    this.options = options;
  }

  @Override
  public void writeDocQuery(DocQueryContext context) throws IOException {
    context.writeTextCommonTerms(search, options);
  }

}
