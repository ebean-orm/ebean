package io.ebeaninternal.server.expression;

import io.ebean.LikeType;
import io.ebean.QueryDsl;
import io.ebeaninternal.api.SpiExpression;
import io.ebeaninternal.api.SpiExpressionRequest;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.el.ElPropertyValue;

import java.io.IOException;

class LikeExpression extends AbstractValueExpression {

  private final boolean caseInsensitive;

  private final LikeType type;

  LikeExpression(String propertyName, Object value, boolean caseInsensitive, LikeType type) {
    super(propertyName, value);
    this.caseInsensitive = caseInsensitive;
    this.type = type;
  }

  @Override
  public void writeDocQuery(DocQueryContext context) throws IOException {
    context.writeLike(propName, strValue(), type, caseInsensitive);
  }

  @Override
  public void addBindValues(SpiExpressionRequest request) {

    ElPropertyValue prop = getElProp(request);
    if (prop != null && prop.isDbEncrypted()) {
      // bind the key as well as the value
      String encryptKey = prop.getBeanProperty().getEncryptKey().getStringValue();
      request.addBindEncryptKey(encryptKey);
    }
    String bindValue = getValue(strValue(), caseInsensitive, type, request);
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
    if (type == LikeType.EQUAL_TO) {
      request.append(" = ? ");
    } else {
      // append db platform like clause
      request.appendLike(type == LikeType.RAW);
    }
  }

  /**
   * Based on caseInsensitive and the property name.
   */
  @Override
  public void queryPlanHash(StringBuilder builder) {
    if (caseInsensitive){
      builder.append("I");
    }
    builder.append("Like[").append(type).append(" ").append(propName).append("]");
  }

  @Override
  public int queryBindHash() {
    return strValue().hashCode();
  }

  @Override
  public boolean isSameByBind(SpiExpression other) {
    LikeExpression that = (LikeExpression) other;
    return strValue().equals(that.strValue());
  }

  private static String getValue(String value, boolean caseInsensitive, LikeType type, SpiExpressionRequest request) {
    if (caseInsensitive) {
      value = value.toLowerCase();
    }
    if (type == LikeType.RAW) {
      return value;
    }
    value = request.escapeLikeString(value);
    switch (type) {
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

  @Override
  public <F extends QueryDsl<?,F>> void visitDsl(BeanDescriptor<?> desc, QueryDsl<?, F> target) {
    if (caseInsensitive) {
      switch (type) {
      case CONTAINS:
        target.icontains(propName, strValue());
        return;
      case STARTS_WITH:
        target.istartsWith(propName, strValue());
        return;
      case ENDS_WITH:
        target.iendsWith(propName, strValue());
        return;
      case EQUAL_TO:
        target.ieq(propName, strValue());
        return;
      case RAW:
        target.ilike(propName, strValue());
        return;
      }
    } else {
      switch (type) {
      case CONTAINS:
        target.contains(propName, strValue());
        return;
      case STARTS_WITH:
        target.startsWith(propName, strValue());
        return;
      case ENDS_WITH:
        target.endsWith(propName, strValue());
        return;
      case EQUAL_TO:
        target.eq(propName, strValue());
        return;
      case RAW:
        target.like(propName, strValue());
        return;
      }
    }
    throw new UnsupportedOperationException(type + " not supported");
  }
}
