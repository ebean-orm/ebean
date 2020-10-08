package io.ebeaninternal.server.expression;

import io.ebean.search.Match;

import java.io.IOException;

/**
 * Full text MATCH expression.
 */
public class TextMatchExpression extends AbstractTextExpression {

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

}
