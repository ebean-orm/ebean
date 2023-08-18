package io.ebeaninternal.server.expression;

import io.ebean.LikeType;
import io.ebeaninternal.api.BindValuesKey;
import io.ebeaninternal.api.SpiExpression;
import io.ebeaninternal.api.SpiExpressionRequest;
import io.ebeaninternal.server.el.ElPropertyValue;

import java.io.IOException;

final class NativeILikeExpression extends AbstractExpression {

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
      String encryptKey = prop.beanProperty().encryptKey().getStringValue();
      request.addBindEncryptKey(encryptKey);
    }
    request.addBindValue(val);
  }

  @Override
  public void addSql(SpiExpressionRequest request) {
    String pname = propName;
    ElPropertyValue prop = getElProp(request);
    if (prop != null && prop.isDbEncrypted()) {
      pname = prop.beanProperty().decryptProperty(propName);
    }
    request.property(pname).append(" ilike ?");
  }

  /**
   * Based on caseInsensitive and the property name.
   */
  @Override
  public void queryPlanHash(StringBuilder builder) {
    builder.append("NativeILike[").append(propName).append(']');
  }

  @Override
  public void queryBindKey(BindValuesKey key) {
    key.add(val);
  }

  @Override
  public boolean isSameByBind(SpiExpression other) {
    NativeILikeExpression that = (NativeILikeExpression) other;
    return val.equals(that.val);
  }

}
