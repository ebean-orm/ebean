package io.ebeaninternal.server.grammer;

import io.ebean.Expression;
import io.ebean.ExpressionList;
import io.ebean.FetchConfig;
import io.ebean.LikeType;
import io.ebean.OrderBy;
import io.ebeaninternal.api.SpiQuery;
import io.ebeaninternal.server.grammer.antlr.EQLBaseListener;
import io.ebeaninternal.server.grammer.antlr.EQLLexer;
import io.ebeaninternal.server.grammer.antlr.EQLParser;
import io.ebeaninternal.server.util.ArrayStack;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.ArrayList;
import java.util.List;

class EqlAdapter<T> extends EQLBaseListener {

  private static final OperatorMapping operatorMapping = new OperatorMapping();

  private static final String DISTINCT = "distinct";

  private static final String NULLS = "nulls";

  private static final String ASC = "asc";

  private final SpiQuery<T> query;

  private final EqlAdapterHelper helper;

  private ArrayStack<ExpressionList<T>> textStack;

  private ArrayStack<ExpressionList<T>> whereStack;

  private boolean textMode;

  private List<Object> inValues;

  private String inPropertyName;

  public EqlAdapter(SpiQuery<T> query) {
    this.query = query;
    this.helper = new EqlAdapterHelper(this);
  }

  /**
   * Return the current expression list that expressions should be added to.
   */
  protected ExpressionList<T> peekExprList() {

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

  /**
   * Push the expression list onto the appropriate stack.
   */
  private void pushExprList(ExpressionList<T> list) {
    if (textMode) {
      textStack.push(list);
    } else {
      whereStack.push(list);
    }
  }

  /**
   * End a list of expressions added by 'OR'.
   */
  private void popJunction() {
    if (textMode) {
      textStack.pop();
    } else {
      whereStack.pop();
    }
  }

  @Override
  public void enterSelect_clause(EQLParser.Select_clauseContext ctx) {

    checkChildren(ctx, 4);
    if (DISTINCT.equals(child(ctx, 1))) {
      query.setDistinct(true);
      query.select(child(ctx, 3));
    } else {
      query.select(child(ctx, 2));
    }
  }

  @Override
  public void enterFetch_path(EQLParser.Fetch_pathContext ctx) {

    int childCount = ctx.getChildCount();
    checkChildren(ctx, 2);
    String path = child(ctx, 1);

    int noPropertiesLength = 2;

    FetchConfig fetchConfig = ParseFetchConfig.parse(path);
    if (fetchConfig != null) {
      noPropertiesLength = 3;
      path = child(ctx, 2);
    }
    if (childCount == noPropertiesLength) {
      query.fetch(path, fetchConfig);

    } else {
      String fetchProperties = trimParenthesis(ctx.getChild(noPropertiesLength).getText());
      query.fetch(path, fetchProperties, fetchConfig);
    }
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

  /**
   * Trim leading '(' and trailing ')'
   */
  private String trimParenthesis(String text) {
    text = text.substring(1);
    text = text.substring(0, text.length() - 1);
    return text;
  }

  private String getLeftHandSidePath(ParserRuleContext ctx) {
    TerminalNode pathToken = ctx.getToken(EQLLexer.PATH_VARIABLE, 0);
    return pathToken.getText();
  }

  @Override
  public void enterBetween_expression(EQLParser.Between_expressionContext ctx) {

    checkChildren(ctx, 5);
    String path = getLeftHandSidePath(ctx);
    EqlOperator op = getOperator(ctx);
    if (op != EqlOperator.BETWEEN) {
      throw new IllegalStateException("Expecting BETWEEN operator but got " + op);
    }
    helper.addBetween(path, child(ctx, 2), child(ctx, 4));
  }

  @Override
  public void enterPropertyBetween_expression(EQLParser.PropertyBetween_expressionContext ctx) {
    checkChildren(ctx, 5);
    String rawValue = child(ctx, 0);
    EqlOperator op = getOperator(ctx);
    if (op != EqlOperator.BETWEEN) {
      throw new IllegalStateException("Expecting BETWEEN operator but got " + op);
    }
    helper.addBetweenProperty(rawValue, child(ctx, 2), child(ctx, 4));
  }

  @Override
  public void enterIn_expression(EQLParser.In_expressionContext ctx) {
    this.inValues = new ArrayList<>();
    this.inPropertyName = getLeftHandSidePath(ctx);
  }

  @Override
  public void enterIn_value(EQLParser.In_valueContext ctx) {
    int childCount = ctx.getChildCount();
    for (int i = 0; i < childCount; i++) {
      String text = child(ctx, i);
      if (isValue(text)) {
        inValues.add(helper.bind(text));
      }
    }
  }

  private String child(ParserRuleContext ctx, int position) {
    ParseTree child = ctx.getChild(position);
    return child.getText();
  }

  private boolean isValue(String text) {
    if (text.length() == 1 && (text.equals("(") || text.equals(")") || text.equals(","))) {
      return false;
    }
    return true;
  }

  @Override
  public void exitIn_expression(EQLParser.In_expressionContext ctx) {
    helper.addIn(inPropertyName, inValues);
  }

  @Override
  public void enterIsNull_expression(EQLParser.IsNull_expressionContext ctx) {
    String path = getLeftHandSidePath(ctx);
    peekExprList().isNull(path);
  }

  @Override
  public void enterIsNotNull_expression(EQLParser.IsNotNull_expressionContext ctx) {
    String path = getLeftHandSidePath(ctx);
    peekExprList().isNotNull(path);
  }

  @Override
  public void enterIsEmpty_expression(EQLParser.IsEmpty_expressionContext ctx) {
    String path = getLeftHandSidePath(ctx);
    peekExprList().isEmpty(path);
  }

  @Override
  public void enterIsNotEmpty_expression(EQLParser.IsNotEmpty_expressionContext ctx) {
    String path = getLeftHandSidePath(ctx);
    peekExprList().isNotEmpty(path);
  }

  @Override
  public void enterLike_expression(EQLParser.Like_expressionContext ctx) {
    addExpression(ctx);
  }

  @Override
  public void enterComparison_expression(EQLParser.Comparison_expressionContext ctx) {
    addExpression(ctx);
  }

  private void addExpression(ParserRuleContext ctx) {
    int childCount = ctx.getChildCount();
    if (childCount < 3) {
      throw new IllegalStateException("expecting 3 children for comparison? " + ctx);
    }
    String operator = child(ctx, 1);
    EqlOperator op = operatorMapping.get(operator);
    if (op == null) {
      throw new IllegalStateException("No operator found for " + operator);
    }
    String path = getLeftHandSidePath(ctx);
    String rhs = child(ctx, 2);
    if (path.equals(rhs)) {
      // the 'value operator path' form
      // invert the operator and use LHS as RHS
      op = invert(op);
      rhs = child(ctx, 0);
    }

    // RHS is Path, Literal or Named input parameter
    helper.addExpression(path, op, rhs);
  }

  private EqlOperator invert(EqlOperator op) {
    switch (op) {
      // no change
      case EQ:
        return EqlOperator.EQ;
      case IEQ:
        return EqlOperator.IEQ;
      case NE:
        return EqlOperator.NE;
      // invert
      case LT:
        return EqlOperator.GT;
      case LTE:
        return EqlOperator.GTE;
      case GT:
        return EqlOperator.LT;
      case GTE:
        return EqlOperator.LTE;
      default:
        throw new IllegalStateException("Can not invert operator " + op);
    }
  }


  @Override
  public void enterConditional_term(EQLParser.Conditional_termContext ctx) {
    int childCount = ctx.getChildCount();
    if (childCount > 1) {
      pushExprList(peekExprList().and());
    }
  }

  @Override
  public void exitConditional_term(EQLParser.Conditional_termContext ctx) {
    if (ctx.getChildCount() > 1) {
      popJunction();
    }
  }

  @Override
  public void enterConditional_expression(EQLParser.Conditional_expressionContext ctx) {
    if (ctx.getChildCount() > 1) {
      pushExprList(peekExprList().or());
    }
  }

  @Override
  public void exitConditional_expression(EQLParser.Conditional_expressionContext ctx) {
    if (ctx.getChildCount() > 1) {
      popJunction();
    }
  }

  @Override
  public void enterConditional_factor(EQLParser.Conditional_factorContext ctx) {
    if (ctx.getChildCount() > 1) {
      pushExprList(peekExprList().not());
    }
  }

  @Override
  public void exitConditional_factor(EQLParser.Conditional_factorContext ctx) {
    if (ctx.getChildCount() > 1) {
      popJunction();
    }
  }

  private EqlOperator getOperator(ParserRuleContext ctx) {
    String operator = child(ctx, 1);
    EqlOperator op = operatorMapping.get(operator);
    if (op == null) {
      throw new IllegalStateException("No operator found for " + operator);
    }
    return op;
  }

  /**
   * Check for the minimum number of children.
   */
  private void checkChildren(ParserRuleContext ctx, int min) {
    if (ctx.getChildCount() < min) {
      throw new IllegalStateException("expecting " + min + " children for comparison? " + ctx);
    }
  }

  public Object namedParam(String parameterName) {
    return query.createNamedParameter(parameterName);
  }

  public Expression like(boolean caseInsensitive, LikeType likeType, String property, Object bindValue) {
    return query.getExpressionFactory().like(property, bindValue, caseInsensitive, likeType);
  }

  public Expression ieq(String property, Object bindValue) {
    return query.getExpressionFactory().ieqObject(property, bindValue);
  }
}
