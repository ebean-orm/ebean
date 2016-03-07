package com.avaje.ebeaninternal.server.expression;

import com.avaje.ebean.LikeType;
import com.avaje.ebeaninternal.api.HashQueryPlanBuilder;
import com.avaje.ebeaninternal.api.SpiExpression;
import com.avaje.ebeaninternal.api.SpiExpressionRequest;
import com.avaje.ebeaninternal.server.el.ElPropertyValue;

import java.io.IOException;

class LikeExpression extends AbstractExpression {

  private static final long serialVersionUID = -5398151809111172380L;

  private final String val;

  private final boolean caseInsensitive;

  private final LikeType type;

  LikeExpression(String propertyName, String value, boolean caseInsensitive, LikeType type) {
    super(propertyName);
    this.caseInsensitive = caseInsensitive;
    this.type = type;
    this.val = value;
  }

  @Override
  public void writeDocQuery(DocQueryContext context) throws IOException {
    context.writeLike(propName, val, type, caseInsensitive);
  }

  @Override
  public void addBindValues(SpiExpressionRequest request) {

    ElPropertyValue prop = getElProp(request);
    if (prop != null && prop.isDbEncrypted()) {
      // bind the key as well as the value
      String encryptKey = prop.getBeanProperty().getEncryptKey().getStringValue();
      request.addBindEncryptKey(encryptKey);
    }

    String bindValue = getValue(val, caseInsensitive, type);
    request.addBindValue(bindValue);
  }

  @Override
  public void addSql(SpiExpressionRequest request) {

    String pname = propName;
    ElPropertyValue prop = getElProp(request);
    if (prop != null && prop.isDbEncrypted()) {
      pname = prop.getBeanProperty().getDecryptProperty(propName);
    }
    if (caseInsensitive) {
      request.append("lower(").append(pname).append(")");
    } else {
      request.append(pname);
    }
    if (type.equals(LikeType.EQUAL_TO)) {
      request.append(" = ? ");
    } else {
      // append db platform like clause
      request.appendLike();
    }
  }

  /**
   * Based on caseInsensitive and the property name.
   */
  @Override
  public void queryPlanHash(HashQueryPlanBuilder builder) {
    builder.add(LikeExpression.class).add(caseInsensitive).add(propName);
    builder.bind(1);
  }

  @Override
  public int queryBindHash() {
    return val.hashCode();
  }

  @Override
  public boolean isSameByPlan(SpiExpression other) {
    if (!(other instanceof LikeExpression)) {
      return false;
    }

    LikeExpression that = (LikeExpression) other;
    return this.propName.equals(that.propName)
        && this.caseInsensitive == that.caseInsensitive
        && this.type == that.type;
  }

  @Override
  public boolean isSameByBind(SpiExpression other) {
    LikeExpression that = (LikeExpression) other;
    return val.equals(that.val);
  }

  private static String getValue(String value, boolean caseInsensitive, LikeType type) {
    if (caseInsensitive) {
      value = value.toLowerCase();
    }
    switch (type) {
      case RAW:
        return value;
      case STARTS_WITH:
        return value + "%";
      case ENDS_WITH:
        return "%" + value;
      case CONTAINS:
        return "%" + value + "%";
      case EQUAL_TO:
        return value;

      default:
        throw new RuntimeException("LikeType " + type + " missed?");
    }
  }

}
