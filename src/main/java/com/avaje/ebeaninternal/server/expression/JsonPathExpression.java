package com.avaje.ebeaninternal.server.expression;

import com.avaje.ebean.event.BeanQueryRequest;
import com.avaje.ebeaninternal.api.HashQueryPlanBuilder;
import com.avaje.ebeaninternal.api.SpiExpressionRequest;

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

  JsonPathExpression(String propertyName, String path, Op operator, Object value) {
    super(propertyName);
    this.path = path;
    this.operator = operator;
    this.value = value;
  }

  @Override
  public void queryAutoTuneHash(HashQueryPlanBuilder builder) {
    builder.add(JsonPathExpression.class).add(propName).add(path).add(operator);
  }

  @Override
  public void queryPlanHash(BeanQueryRequest<?> request, HashQueryPlanBuilder builder) {
    queryAutoTuneHash(builder);
  }

  @Override
  public int queryBindHash() {
    return (value == null) ? 0 : value.hashCode();
  }

  @Override
  public void addSql(SpiExpressionRequest request) {

    // Use DB specific expression handling (Postgres and Oracle supported)
    request.getJsonHander().addSql(request, propName, path, operator, value);
  }

  @Override
  public void addBindValues(SpiExpressionRequest request) {

    if (value != null) {
      request.addBindValue(value);
    }
  }
}
