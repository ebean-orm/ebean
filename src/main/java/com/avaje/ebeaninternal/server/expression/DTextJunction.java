package com.avaje.ebeaninternal.server.expression;

import com.avaje.ebean.Query;
import com.avaje.ebean.TextExpressionList;
import com.avaje.ebean.TextJunction;
import com.avaje.ebean.search.Match;
import com.avaje.ebeaninternal.api.SpiExpression;
import com.avaje.ebeaninternal.api.SpiTextJunction;

import java.io.IOException;
import java.util.List;

/**
 * Implementation of SpiTextJunction (Must, Must Not or Should group).
 */
class DTextJunction<T> extends JunctionExpression<T> implements SpiTextJunction<T> {

  private final TextJunction.Type type;

  DTextJunction(Query<T> query, TextExpressionList<T> parent, TextJunction.Type type) {
    super(query, parent);
    this.type = type;
  }

  @Override
  public void writeDocQueryJunction(DocQueryContext context) throws IOException {
    context.startBoolGroupList(type);
    List<SpiExpression> list = exprList.internalList();
    for (int i = 0; i < list.size(); i++) {
      list.get(i).writeDocQuery(context);
    }
    context.endBoolGroupList();
  }

  @Override
  public SpiExpression copyForPlanKey() {
    return this;
  }

  @Override
  public TextExpressionList<T> match(String propertyName, String search) {
    return match(propertyName, search, null);
  }

  @Override
  public TextExpressionList<T> match(String propertyName, String search, Match options) {
    return exprList.match(propertyName, search, options);
  }

  @Override
  public TextJunction<T> must() {
    return exprList.must();
  }

  @Override
  public TextJunction<T> should() {
    return exprList.should();
  }

  @Override
  public TextJunction<T> mustNot() {
    return exprList.mustNot();
  }

  @Override
  public TextExpressionList<T> endMust() {
    return endTextJunction();
  }

  @Override
  public TextExpressionList<T> endShould() {
    return endTextJunction();
  }

  @Override
  public TextExpressionList<T> endMustNot() {
    return endTextJunction();
  }

  private TextExpressionList<T> endTextJunction() {
    return exprList.endTextJunction();
  }

}
