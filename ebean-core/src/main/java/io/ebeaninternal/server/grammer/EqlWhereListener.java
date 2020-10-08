package io.ebeaninternal.server.grammer;

import io.ebean.Expression;
import io.ebean.ExpressionFactory;
import io.ebean.ExpressionList;
import io.ebean.LikeType;
import io.ebeaninternal.server.grammer.antlr.EQLBaseListener;
import io.ebeaninternal.server.grammer.antlr.EQLLexer;
import io.ebeaninternal.server.grammer.antlr.EQLParser;
import io.ebeaninternal.server.util.ArrayStack;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

abstract class EqlWhereListener<T> extends EQLBaseListener {

  private static final OperatorMapping operatorMapping = new OperatorMapping();

  ArrayStack<ExpressionList<T>> textStack;

  ArrayStack<ExpressionList<T>> whereStack;

  boolean textMode;

  private boolean inWithEmpty;

  private List<Object> inValues;

  private String inPropertyName;

  /**
   * Return the current expression list that expressions should be added to.
   */
  abstract ExpressionList<T> peekExprList();

  abstract ExpressionFactory expressionFactory();

  abstract Object namedParam(String paramName);

  abstract Object positionParam(String paramPosition);

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
  public void enterInrange_expression(EQLParser.Inrange_expressionContext ctx) {
    checkChildren(ctx, 5);
    String path = getLeftHandSidePath(ctx);
    EqlOperator op = getOperator(ctx);
    if (op != EqlOperator.INRANGE) {
      throw new IllegalStateException("Expecting INRANGE operator but got " + op);
    }
    addInRange(path, child(ctx, 2), child(ctx, 4));
  }

  @Override
  public void enterBetween_expression(EQLParser.Between_expressionContext ctx) {

    checkChildren(ctx, 5);
    String path = getLeftHandSidePath(ctx);
    EqlOperator op = getOperator(ctx);
    if (op != EqlOperator.BETWEEN) {
      throw new IllegalStateException("Expecting BETWEEN operator but got " + op);
    }
    addBetween(path, child(ctx, 2), child(ctx, 4));
  }

  @Override
  public void enterPropertyBetween_expression(EQLParser.PropertyBetween_expressionContext ctx) {
    checkChildren(ctx, 5);
    String rawValue = child(ctx, 0);
    EqlOperator op = getOperator(ctx);
    if (op != EqlOperator.BETWEEN) {
      throw new IllegalStateException("Expecting BETWEEN operator but got " + op);
    }
    addBetweenProperty(rawValue, child(ctx, 2), child(ctx, 4));
  }

  @Override
  public void enterInOrEmpty_expression(EQLParser.InOrEmpty_expressionContext ctx) {
    this.inWithEmpty = true;
    this.inValues = new ArrayList<>();
    this.inPropertyName = getLeftHandSidePath(ctx);
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
      if (text.startsWith("?")) {
        inValues = toList(getBindValue(EqlValueType.POS_PARAM, text));
      } else {
        if (inWithEmpty) {
          throw new IllegalArgumentException("Sorry, can only use inOrEmpty with positioned parameters");
        }
        if (isValue(text)) {
          inValues.add(bind(text));
        }
      }
    }
  }

  @SuppressWarnings("unchecked")
  private List<Object> toList(Object value) {
    if (value == null) return null;
    if (value instanceof List) {
      return (List<Object>)value;
    }
    if (value instanceof Set) {
      return new ArrayList<>((Set)value);
    }
    throw new IllegalArgumentException("Expected List of Set but got " + value);
  }

  @Override
  public void exitIn_expression(EQLParser.In_expressionContext ctx) {
    peekExprList().in(inPropertyName, inValues);
  }

  @Override
  public void exitInOrEmpty_expression(EQLParser.InOrEmpty_expressionContext ctx) {
    inWithEmpty = false;
    peekExprList().inOrEmpty(inPropertyName, inValues);
  }

  String child(ParserRuleContext ctx, int position) {
    ParseTree child = ctx.getChild(position);
    return child.getText();
  }

  private boolean isValue(String text) {
    return text.length() != 1 || (!text.equals("(") && !text.equals(")") && !text.equals(","));
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
    addExpression(path, op, rhs);
  }

  private EqlOperator invert(EqlOperator op) {
    switch (op) {
      // no change
      case EQ:
        return EqlOperator.EQ;
      case IEQ:
        return EqlOperator.IEQ;
      case INE:
        return EqlOperator.INE;
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
      case EQORNULL:
        return EqlOperator.EQORNULL;
      case GTORNULL:
        return EqlOperator.LEORNULL;
      case LTORNULL:
        return EqlOperator.GEORNULL;
      case GEORNULL:
        return EqlOperator.LTORNULL;
      case LEORNULL:
        return EqlOperator.GTORNULL;
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
  void checkChildren(ParserRuleContext ctx, int min) {
    if (ctx.getChildCount() < min) {
      throw new IllegalStateException("expecting " + min + " children for comparison but got " + ctx.getChildCount());
    }
  }

  private Expression like(boolean caseInsensitive, LikeType likeType, String property, Object bindValue) {
    return expressionFactory().like(property, bindValue, caseInsensitive, likeType);
  }

  private Expression ieq(String property, Object bindValue) {
    return expressionFactory().ieqObject(property, bindValue);
  }

  private Expression ine(String property, Object bindValue) {
    return expressionFactory().ineObject(property, bindValue);
  }

  private EqlValueType getValueType(String valueAsText) {

    char firstChar = Character.toLowerCase(valueAsText.charAt(0));
    switch (firstChar) {
      case '?':
        return EqlValueType.POS_PARAM;
      case ':':
        return EqlValueType.NAMED_PARAM;
      case 't':
        return EqlValueType.BOOL;
      case 'f':
        return EqlValueType.BOOL;
      case '\'':
        return EqlValueType.STRING;
      default:
        if (Character.isDigit(firstChar)) {
          return EqlValueType.NUMBER;
        }
        throw new IllegalArgumentException("Unexpected first character in value [" + valueAsText + "]");
    }
  }

  private void addBetweenProperty(String rawValue, String lowProperty, String highProperty) {
    peekExprList().betweenProperties(lowProperty, highProperty, bind(rawValue));
  }

  private void addBetween(String path, String value1, String value2) {
    peekExprList().between(path, bind(value1), bind(value2));
  }

  private void addInRange(String path, String value1, String value2) {
    peekExprList().inRange(path, bind(value1), bind(value2));
  }

  private void addExpression(String path, EqlOperator op, String value) {
    switch (op) {
      case EQ:
        peekExprList().eq(path, bind(value));
        break;
      case IEQ:
        peekExprList().add(ieq(path, bind(value)));
        break;
      case NE:
        peekExprList().ne(path, bind(value));
        break;
      case INE:
        peekExprList().add(ine(path, bind(value)));
        break;
      case GT:
        peekExprList().gt(path, bind(value));
        break;
      case LT:
        peekExprList().lt(path, bind(value));
        break;
      case GTE:
        peekExprList().ge(path, bind(value));
        break;
      case LTE:
        peekExprList().le(path, bind(value));
        break;
      case LIKE:
        addLike(false, LikeType.RAW, path, bind(value));
        break;
      case CONTAINS:
        addLike(false, LikeType.CONTAINS, path, bind(value));
        break;
      case STARTS_WITH:
        addLike(false, LikeType.STARTS_WITH, path, bind(value));
        break;
      case ENDS_WITH:
        addLike(false, LikeType.ENDS_WITH, path, bind(value));
        break;
      case ILIKE:
        addLike(true, LikeType.RAW, path, bind(value));
        break;
      case ICONTAINS:
        addLike(true, LikeType.CONTAINS, path, bind(value));
        break;
      case ISTARTS_WITH:
        addLike(true, LikeType.STARTS_WITH, path, bind(value));
        break;
      case IENDS_WITH:
        addLike(true, LikeType.ENDS_WITH, path, bind(value));
        break;
      case EQORNULL:
        peekExprList().eqOrNull(path, bind(value));
        break;
      case GTORNULL:
        peekExprList().gtOrNull(path, bind(value));
        break;
      case LTORNULL:
        peekExprList().ltOrNull(path, bind(value));
        break;
      case GEORNULL:
        peekExprList().geOrNull(path, bind(value));
        break;
      case LEORNULL:
        peekExprList().leOrNull(path, bind(value));
        break;

      default:
        throw new IllegalStateException("Unhandled operator " + op);
    }
  }

  private void addLike(boolean caseInsensitive, LikeType likeType, String path, Object bindValue) {
    peekExprList().add(like(caseInsensitive, likeType, path, bindValue));
  }

  private Object bind(String value) {
    return getBindValue(getValueType(value), value);
  }

  private Object getBindValue(EqlValueType valueType, String value) {
    switch (valueType) {
      case BOOL:
        return Boolean.parseBoolean(value);
      case NUMBER:
        return new BigDecimal(value);
      case STRING:
        return unquote(value);
      case POS_PARAM:
        return positionParam(value);
      case NAMED_PARAM:
        return namedParam(value.substring(1));
      default:
        throw new IllegalArgumentException("Unhandled valueType " + valueType);
    }
  }

  private String unquote(String value) {
    return value.substring(1, value.length() - 1);
  }
}
