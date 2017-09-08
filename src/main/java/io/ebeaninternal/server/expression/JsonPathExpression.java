package io.ebeaninternal.server.expression;

import io.ebeaninternal.api.SpiExpression;
import io.ebeaninternal.api.SpiExpressionRequest;

import java.io.IOException;

/**
 * Generally speaking tests the value at a given path in the JSON document.
 * <p>
 * Supports the usual operators (equal to, greater than etc).
 * </p>
 * <p>
 * The value passed in is expected to be a valid JSON type so string, number, boolean.
 * </p>
 */
class JsonPathExpression extends AbstractExpression {

  /**
   * The path in the JSON document in dot notation form.
   */
  protected final String path;

  /**
   * The expression operator.
   */
  protected final Op operator;

  /**
   * The bind value used to compare against the document path value.
   */
  protected final Object value;

  /**
   * For Between this is the upper bind value.
   */
  protected final Object upperValue;

  /**
   * Construct for Operator (not BETWEEN though).
   */
  JsonPathExpression(String propertyName, String path, Op operator, Object value) {
    super(propertyName);
    this.path = path;
    this.operator = operator;
    this.value = value;
    this.upperValue = null;
  }

  /**
   * Construct for BETWEEN expression.
   */
  JsonPathExpression(String propertyName, String path, Object value, Object upperValue) {
    super(propertyName);
    this.path = path;
    this.operator = Op.BETWEEN;
    this.value = value;
    this.upperValue = upperValue;
  }

  @Override
  public void writeDocQuery(DocQueryContext context) throws IOException {
    String fullName = propName + "." + path;
    if (operator == Op.BETWEEN) {
      context.writeRange(fullName, Op.GT_EQ, value, Op.LT_EQ, upperValue);
    } else {
      context.writeSimple(operator, fullName, value);
    }
  }

  @Override
  public void queryPlanHash(StringBuilder builder) {
    builder.append("JsonPath[");
    builder.append(propName).append(" path:").append(path).append(" op:").append(operator);
    if (value != null) {
      builder.append(" ?1");
    }
    if (upperValue != null) {
      builder.append(" ?2");
    }
    builder.append("]");
  }

  @Override
  public int queryBindHash() {
    int hc = (value == null) ? 0 : value.hashCode();
    hc = (upperValue == null) ? hc : hc * 92821 + upperValue.hashCode();
    return hc;
  }

  @Override
  public boolean isSameByBind(SpiExpression other) {
    JsonPathExpression that = (JsonPathExpression) other;
    if (value != null ? !value.equals(that.value) : that.value != null) return false;
    return upperValue != null ? upperValue.equals(that.upperValue) : that.upperValue == null;
  }

  @Override
  public void addSql(SpiExpressionRequest request) {

    // Use DB specific expression handling (Postgres and Oracle supported)
    request.getDbPlatformHandler().json(request, propName, path, operator, value);
  }

  @Override
  public void addBindValues(SpiExpressionRequest request) {

    if (value != null) {
      // value is null for EXISTS/NOT EXISTS
      request.addBindValue(value);
    }
    if (upperValue != null) {
      // upperValue only for BETWEEN operator
      request.addBindValue(upperValue);
    }
  }
}
