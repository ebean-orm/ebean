package io.ebeaninternal.server.grammer;

import io.ebean.ExpressionFactory;
import io.ebean.ExpressionList;
import io.ebean.FetchConfig;
import io.ebean.OrderBy;
import io.ebeaninternal.api.SpiQuery;
import io.ebeaninternal.server.grammer.antlr.EQLParser;
import io.ebeaninternal.server.util.ArrayStack;
import org.antlr.v4.runtime.tree.ParseTree;

class EqlAdapter<T> extends EqlWhereListener<T> {

  private static final String DISTINCT = "distinct";

  private static final String NULLS = "nulls";

  private static final String ASC = "asc";

  private final SpiQuery<T> query;

  private final ExpressionFactory expressionFactory;

  EqlAdapter(SpiQuery<T> query) {
    this.query = query;
    this.expressionFactory = query.getExpressionFactory();
  }

  @Override
  ExpressionFactory expressionFactory() {
    return expressionFactory;
  }

  @Override
  Object namedParam(String parameterName) {
    return query.createNamedParameter(parameterName);
  }

  @Override
  Object positionParam(String paramPosition) {
    return query.createNamedParameter(paramPosition);
  }

  /**
   * Return the current expression list that expressions should be added to.
   */
  @Override
  ExpressionList<T> peekExprList() {

    if (textMode) {
      // return the current text expression list
      return _peekText();
    }

    if (whereStack == null) {
      whereStack = new ArrayStack<>();
      whereStack.push(query.where());
    }
    // return the current expression list
    return whereStack.peek();
  }

  private ExpressionList<T> _peekText() {
    if (textStack == null) {
      textStack = new ArrayStack<>();
      // empty so push on the queries base expression list
      textStack.push(query.text());
    }
    // return the current expression list
    return textStack.peek();
  }

  @Override
  public void enterSelect_clause(EQLParser.Select_clauseContext ctx) {

    // with or without surrounding ( and )
    int childCount = ctx.getChildCount();
    String clause = trimParenthesis(child(ctx, childCount - 1));
    if (DISTINCT.equals(child(ctx, 1))) {
      query.setDistinct(true);
    }
    query.select(clause);
  }

  @Override
  public void enterFetch_path(EQLParser.Fetch_pathContext ctx) {

    int childCount = ctx.getChildCount();
    checkChildren(ctx, 2);

    String maybePath = child(ctx, 1);
    FetchConfig fetchConfig = ParseFetchConfig.parse(maybePath);

    int propsIndex = 2;

    String path;
    if (fetchConfig == null) {
      path = trimQuotes(maybePath);
    } else {
      propsIndex = 3;
      path = trimQuotes(child(ctx, 2));
    }

    if (childCount == propsIndex) {
      query.fetch(path, fetchConfig);

    } else {
      String properties = trimParenthesis(ctx.getChild(propsIndex).getText());
      query.fetch(path, properties, fetchConfig);
    }
  }

  /**
   * Trim leading '(' and trailing ')'
   */
  private String trimParenthesis(String text) {
    if (text.charAt(0) == '(') {
      return text.substring(1, text.length() - 1);
    }
    return text;
  }

  private String trimQuotes(String path) {
    if (path.charAt(0) == '\'' || path.charAt(0) == '`') {
      return path.substring(1, path.length() - 1);
    }
    return path;
  }

  @Override
  public void enterOrderby_property(EQLParser.Orderby_propertyContext ctx) {

    int childCount = ctx.getChildCount();

    String path = child(ctx, 0);
    boolean asc = true;
    String nulls = null;
    String nullsFirstLast = null;

    if (childCount == 3) {
      asc = child(ctx, 1).startsWith(ASC);
      nullsFirstLast = ctx.getChild(2).getChild(1).getText();
      nulls = NULLS;

    } else if (childCount == 2) {
      String firstChild = child(ctx, 1);
      if (firstChild.startsWith(NULLS)) {
        nullsFirstLast = ctx.getChild(1).getChild(1).getText();
        nulls = NULLS;
      } else {
        asc = firstChild.startsWith(ASC);
      }
    }

    query.orderBy().add(new OrderBy.Property(path, asc, nulls, nullsFirstLast));
  }

  @Override
  public void enterLimit_clause(EQLParser.Limit_clauseContext ctx) {

    try {
      String limitValue = child(ctx, 1);
      query.setMaxRows(Integer.parseInt(limitValue));

      int childCount = ctx.getChildCount();
      if (childCount == 3) {
        ParseTree offsetTree = ctx.getChild(2);
        String offsetValue = offsetTree.getChild(1).getText();
        query.setFirstRow(Integer.parseInt(offsetValue));
      }
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("Error parsing limit or offset parameter - not an integer", e);
    }
  }

}
