package com.avaje.ebeaninternal.server.grammer;

import com.avaje.ebean.Expression;
import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.LikeType;
import com.avaje.ebeaninternal.api.SpiQuery;
import com.avaje.ebeaninternal.server.grammer.antlr.EQLBaseListener;
import com.avaje.ebeaninternal.server.grammer.antlr.EQLLexer;
import com.avaje.ebeaninternal.server.grammer.antlr.EQLParser;
import com.avaje.ebeaninternal.server.util.ArrayStack;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.ArrayList;
import java.util.List;

class EqlAdapter<T> extends EQLBaseListener {

  private static final OperatorMapping operatorMapping = new OperatorMapping();

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
      whereStack = new ArrayStack<ExpressionList<T>>();
      whereStack.push(query.where());
    }
    // return the current expression list
    return whereStack.peek();
  }

  private ExpressionList<T> _peekText() {
    if (textStack == null) {
      textStack = new ArrayStack<ExpressionList<T>>();
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


  private String getLeftHandSidePath(ParserRuleContext ctx) {
    TerminalNode pathToken = ctx.getToken(EQLLexer.PATH_VARIABLE, 0);
    return pathToken.getText();
  }

  @Override
  public void enterBetween_expression(EQLParser.Between_expressionContext ctx) {

  }

  @Override
  public void enterPropertyBetween_expression(EQLParser.PropertyBetween_expressionContext ctx) {

  }

  @Override
  public void enterIn_expression(EQLParser.In_expressionContext ctx) {
    this.inValues = new ArrayList<Object>();
    this.inPropertyName = getLeftHandSidePath(ctx);
  }

  @Override
  public void enterIn_value(EQLParser.In_valueContext ctx) {
    int childCount = ctx.getChildCount();
    for (int i = 0; i < childCount; i++) {
      ParseTree child = ctx.getChild(i);
      String text = child.getText();
      if (!text.equals("(") && !text.equals(")")) {
        inValues.add(helper.bind(text));
      }
    }
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
    String path = getLeftHandSidePath(ctx);
    String operator = ctx.getChild(1).getText();
    EqlOperator op = operatorMapping.get(operator);
    if (op == null) {
      throw new IllegalStateException("No operator found for " + operator);
    }

    // RHS is Path, Literal or Named input parameter
    ParseTree rhs = ctx.getChild(2);
    helper.addExpression(path, op, rhs.getText());
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
