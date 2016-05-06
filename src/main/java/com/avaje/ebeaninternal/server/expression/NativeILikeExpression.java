package com.avaje.ebeaninternal.server.expression;

import com.avaje.ebean.LikeType;
import com.avaje.ebeaninternal.api.HashQueryPlanBuilder;
import com.avaje.ebeaninternal.api.SpiExpression;
import com.avaje.ebeaninternal.api.SpiExpressionRequest;
import com.avaje.ebeaninternal.server.el.ElPropertyValue;

import java.io.IOException;

class NativeILikeExpression extends AbstractExpression {

  private final String val;

  NativeILikeExpression(String propertyName, String value) {
    super(propertyName);
    this.val = value;
  }

  @Override
  public void writeDocQuery(DocQueryContext context) throws IOException {
    context.writeLike(propName, val, LikeType.RAW, true);
  }

  @Override
  public void addBindValues(SpiExpressionRequest request) {

    ElPropertyValue prop = getElProp(request);
    if (prop != null && prop.isDbEncrypted()) {
      // bind the key as well as the value
      String encryptKey = prop.getBeanProperty().getEncryptKey().getStringValue();
      request.addBindEncryptKey(encryptKey);
    }
    request.addBindValue(val);
  }

  @Override
  public void addSql(SpiExpressionRequest request) {

    String pname = propName;
    ElPropertyValue prop = getElProp(request);
    if (prop != null && prop.isDbEncrypted()) {
      pname = prop.getBeanProperty().getDecryptProperty(propName);
    }

    request.append(pname).append(" ilike ? ");
  }

  /**
   * Based on caseInsensitive and the property name.
   */
  @Override
  public void queryPlanHash(HashQueryPlanBuilder builder) {
    builder.add(NativeILikeExpression.class).add(propName);
    builder.bind(1);
  }

  @Override
  public int queryBindHash() {
    return val.hashCode();
  }

  @Override
  public boolean isSameByPlan(SpiExpression other) {
    if (!(other instanceof NativeILikeExpression)) {
      return false;
    }

    NativeILikeExpression that = (NativeILikeExpression) other;
    return this.propName.equals(that.propName);
  }

  @Override
  public boolean isSameByBind(SpiExpression other) {
    NativeILikeExpression that = (NativeILikeExpression) other;
    return val.equals(that.val);
  }

}
