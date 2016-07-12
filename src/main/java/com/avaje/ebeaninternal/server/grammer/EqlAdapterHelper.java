package com.avaje.ebeaninternal.server.grammer;

import com.avaje.ebean.ExpressionList;

import java.math.BigDecimal;

class EqlAdapterHelper {

  private final EqlAdapter owner;

  public EqlAdapterHelper(EqlAdapter owner) {
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


  protected void addExpression(String path, EqlOperator op, String value) {

    switch (op) {
      case EQ:
        peekExprList().eq(path, bind(value));
        break;
      case IEQ:
        peekExprList().ieq(path, bindString(value));
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
        peekExprList().like(path, bindString(value));
        break;
      case CONTAINS:
        peekExprList().contains(path, bindString(value));
        break;
      case STARTS_WITH:
        peekExprList().startsWith(path, bindString(value));
        break;
      case ENDS_WITH:
        peekExprList().endsWith(path, bindString(value));
        break;
      case ILIKE:
        peekExprList().ilike(path, bindString(value));
        break;
      case ICONTAINS:
        peekExprList().icontains(path, bindString(value));
        break;
      case ISTARTS_WITH:
        peekExprList().istartsWith(path, bindString(value));
        break;
      case IENDS_WITH:
        peekExprList().iendsWith(path, bindString(value));
        break;
      default:
        throw new IllegalStateException("Unhandled operator " + op);
    }

  }

  private String bindString(String value) {
    ValueType valueType = getValueType(value);
    switch (valueType) {
      case NAMED_PARAM:
        return NamedParameter.PREFIX + value;
      case STRING:
        return unquote(value);
      default:
        throw new IllegalArgumentException("Only STRING or NAMED PARAMETER argument allowed but got " + valueType);
    }
  }

  private Object bind(String value) {
    ValueType valueType = getValueType(value);
    return getBindValue(valueType, value);
  }

  private ExpressionList peekExprList() {
    return owner.peekExprList();
  }

  private Object getBindValue(ValueType valueType, String value) {
    switch (valueType) {
      case BOOL: return Boolean.parseBoolean(value);
      case NUMBER: return new BigDecimal(value);
      case STRING: return unquote(value);
      case NAMED_PARAM: return new NamedParameter(value.substring(1));
      default:
        throw new IllegalArgumentException("Unhandled valueType "+valueType);
    }
  }

  private String unquote(String value) {
    String raw = value.substring(1, value.length() - 1);
    //raw.replaceAll();
    return raw;
  }
}
