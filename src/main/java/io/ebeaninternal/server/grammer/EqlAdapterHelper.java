package io.ebeaninternal.server.grammer;

import io.ebean.ExpressionList;
import io.ebean.LikeType;

import java.math.BigDecimal;
import java.util.List;

class EqlAdapterHelper {

  private final EqlAdapter<?> owner;

  public EqlAdapterHelper(EqlAdapter<?> owner) {
    this.owner = owner;
  }

  enum ValueType {
    NAMED_PARAM,
    STRING,
    BOOL,
    NUMBER
  }

  private ValueType getValueType(String valueAsText) {

    char firstChar = Character.toLowerCase(valueAsText.charAt(0));
    switch (firstChar) {
      case ':':
        return ValueType.NAMED_PARAM;
      case 't':
        return ValueType.BOOL;
      case 'f':
        return ValueType.BOOL;
      case '\'':
        return ValueType.STRING;
      default:
        if (Character.isDigit(firstChar)) {
          return ValueType.NUMBER;
        }
        throw new IllegalArgumentException("Unexpected first character in value [" + valueAsText + "]");
    }
  }

  protected void addBetweenProperty(String rawValue, String lowProperty, String highProperty) {
    peekExprList().betweenProperties(lowProperty, highProperty, bind(rawValue));
  }

  protected void addBetween(String path, String value1, String value2) {
    peekExprList().between(path, bind(value1), bind(value2));
  }

  protected void addIn(String path, List<Object> inValues) {
    peekExprList().in(path, inValues);
  }

  protected void addExpression(String path, EqlOperator op, String value) {

    switch (op) {
      case EQ:
        peekExprList().eq(path, bind(value));
        break;
      case IEQ:
        peekExprList().add(owner.ieq(path, bind(value)));
        break;
      case NE:
        peekExprList().ne(path, bind(value));
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
      default:
        throw new IllegalStateException("Unhandled operator " + op);
    }

  }

  private void addLike(boolean caseInsensitive, LikeType likeType, String path, Object bindValue) {
    peekExprList().add(owner.like(caseInsensitive, likeType, path, bindValue));
  }

  protected Object bind(String value) {
    ValueType valueType = getValueType(value);
    return getBindValue(valueType, value);
  }

  private ExpressionList<?> peekExprList() {
    return owner.peekExprList();
  }

  private Object getBindValue(ValueType valueType, String value) {
    switch (valueType) {
      case BOOL:
        return Boolean.parseBoolean(value);
      case NUMBER:
        return new BigDecimal(value);
      case STRING:
        return unquote(value);
      case NAMED_PARAM:
        return owner.namedParam(value.substring(1));
      default:
        throw new IllegalArgumentException("Unhandled valueType " + valueType);
    }
  }

  private String unquote(String value) {
    return value.substring(1, value.length() - 1);
  }
}
