package com.avaje.ebeaninternal.server.ldap.expression;

import com.avaje.ebean.event.BeanQueryRequest;
import com.avaje.ebeaninternal.api.SpiExpressionRequest;
import com.avaje.ebeaninternal.server.el.ElPropertyValue;
import com.avaje.ebeaninternal.server.type.ScalarType;

class LdSimpleExpression extends LdAbstractExpression {

  private static final long serialVersionUID = 4091359751840929075L;

  enum Op {
    EQ {
      public String toString() {
        return "=";
      }
    },
    NOT_EQ {
      public String toString() {
        return "<>";
      }
    },
    LT {
      public String toString() {
        return "<";
      }
    },
    LT_EQ {
      public String toString() {
        return "<=";
      }
    },
    GT {
      public String toString() {
        return ">";
      }
    },
    GT_EQ {
      public String toString() {
        return ">=";
      }
    }
  }

  private final Op type;

  private final Object value;

  public LdSimpleExpression(String propertyName, Op type, Object value) {
    super(propertyName);
    this.type = type;
    this.value = value;
  }

  public String getPropertyName() {
    return propertyName;
  }

  public void addBindValues(SpiExpressionRequest request) {

    ElPropertyValue prop = getElProp(request);
    if (prop != null) {
      if (prop.isAssocId()) {
        Object[] ids = prop.getAssocOneIdValues(value);
        if (ids != null) {
          for (int i = 0; i < ids.length; i++) {
            request.addBindValue(ids[i]);
          }
        }
        return;
      }
      ScalarType<?> scalarType = prop.getBeanProperty().getScalarType();
      Object v = scalarType.toJdbcType(value);
      request.addBindValue(v);
    } else {
      request.addBindValue(value);
    }
  }

  public void addSql(SpiExpressionRequest request) {
    ElPropertyValue prop = getElProp(request);
    if (prop != null) {
      if (prop.isAssocId()) {
        String rawExpr = prop.getAssocOneIdExpr(propertyName, type.toString());
        String parsed = request.parseDeploy(rawExpr);
        request.append(parsed);
        return;
      }
    }
    String parsed = request.parseDeploy(propertyName);

    request.append("(").append(parsed).append("").append(type.toString()).append(nextParam(request)).append(")");
  }

  /**
   * Based on the type and propertyName.
   */
  public int queryAutoFetchHash() {
    int hc = LdSimpleExpression.class.getName().hashCode();
    hc = hc * 31 + propertyName.hashCode();
    hc = hc * 31 + type.name().hashCode();
    return hc;
  }

  public int queryPlanHash(BeanQueryRequest<?> request) {
    return queryAutoFetchHash();
  }

  public int queryBindHash() {
    return value.hashCode();
  }

}
