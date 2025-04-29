package io.ebeaninternal.server.deploy.id;

import io.ebean.bean.EntityBean;
import io.ebean.core.type.ScalarType;
import io.ebeaninternal.api.SpiExpressionBind;
import io.ebeaninternal.api.SpiExpressionRequest;
import io.ebeaninternal.server.core.DefaultSqlUpdate;
import io.ebeaninternal.server.core.InternString;
import io.ebeaninternal.server.deploy.BeanProperty;
import io.ebeaninternal.server.deploy.DbReadContext;
import io.ebeaninternal.server.deploy.DbSqlContext;
import io.ebeaninternal.server.persist.MultiValueWrapper;
import io.ebeaninternal.server.persist.platform.MultiValueBind;
import io.ebeaninternal.server.bind.DataBind;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Bind an Id where the Id is made of a single property (not embedded).
 */
public final class IdBinderSimple implements IdBinder {

  private final BeanProperty idProperty;
  private final String bindIdSql;
  private final Class<?> expectedType;
  private final MultiValueBind multiValueBind;
  @SuppressWarnings("rawtypes")
  private final ScalarType scalarType;

  public IdBinderSimple(BeanProperty idProperty, MultiValueBind multiValueBind) {
    this.idProperty = idProperty;
    this.scalarType = idProperty.scalarType();
    this.expectedType = idProperty.type();
    bindIdSql = InternString.intern(idProperty.dbColumn() + " = ?");
    this.multiValueBind = multiValueBind;
  }

  @Override
  public void initialise() {
    // do nothing
  }

  @Override
  public String idSelect() {
    return idProperty.name();
  }

  @Override
  public boolean isIdInExpandedForm() {
    return false;
  }

  @Override
  public String orderBy(String pathPrefix, boolean ascending) {
    StringBuilder sb = new StringBuilder();
    if (pathPrefix != null) {
      sb.append(pathPrefix).append('.');
    }
    sb.append(idProperty.name());
    if (!ascending) {
      sb.append(" desc");
    }
    return sb.toString();
  }

  @Override
  public void buildRawSqlSelectChain(String prefix, List<String> selectChain) {
    idProperty.buildRawSqlSelectChain(prefix, selectChain);
  }

  @Override
  public BeanProperty beanProperty() {
    return idProperty;
  }

  @Override
  public BeanProperty findBeanProperty(String dbColumnName) {
    if (dbColumnName.equalsIgnoreCase(idProperty.dbColumn())) {
      return idProperty;
    }
    return null;
  }

  @Override
  public boolean isComplexId() {
    return false;
  }

  @Override
  public String orderBy() {
    return idProperty.name();
  }

  @Override
  public String bindInSql(String baseTableAlias) {
    if (baseTableAlias == null) {
      return idProperty.dbColumn();
    } else {
      return baseTableAlias + "." + idProperty.dbColumn();
    }
  }

  @Override
  public String bindEqSql(String baseTableAlias) {
    if (baseTableAlias == null) {
      return bindIdSql;
    } else {
      return baseTableAlias + "." + bindIdSql;
    }
  }

  @Override
  public Object[] values(EntityBean bean) {
    return new Object[]{idProperty.getValue(bean)};
  }

  @Override
  public Object[] bindValues(Object idValue) {
    return new Object[]{idValue};
  }

  @Override
  public String idInValueExprDelete(int size) {
    return idInValueExpr(false, size);
  }

  @Override
  public String idInValueExpr(boolean not, int size) {
    if (size <= 0) {
      throw new IndexOutOfBoundsException("The size must be at least 1");
    }
    return multiValueBind.getInExpression(not, scalarType, size);
  }

  @Override
  public void addBindValues(DefaultSqlUpdate sqlUpdate, Collection<?> ids) {
    sqlUpdate.setParameter(new MultiValueWrapper(ids));
  }

  @Override
  public void addBindValues(SpiExpressionBind request, Collection<?> values) {
    List<Object> copy = new ArrayList<>(values);
    copy.replaceAll(idValue -> convertSetId(idValue, null));
    request.addBindValue(new MultiValueWrapper(copy));
  }

  @Override
  public Object convertForJson(EntityBean bean) {
    return idProperty.getValue(bean);
  }

  @Override
  public Object convertFromJson(Object value) {
    // handle simple type conversion if required
    return convertId(value);
  }

  @Override
  public void bindId(DefaultSqlUpdate sqlUpdate, Object value) {
    sqlUpdate.setParameter(value);
  }

  @Override
  public void bindId(DataBind dataBind, Object value) throws SQLException {
    if (!value.getClass().equals(expectedType)) {
      value = scalarType.toBeanType(value);
    }
    idProperty.bind(dataBind, value);
  }

  @Override
  public void writeData(DataOutput os, Object value) throws IOException {
    idProperty.writeData(os, value);
  }

  @Override
  public Object readData(DataInput is) throws IOException {
    return idProperty.readData(is);
  }

  @Override
  public void loadIgnore(DbReadContext ctx) {
    idProperty.loadIgnore(ctx);
  }

  @Override
  public Object readSet(DbReadContext ctx, EntityBean bean) throws SQLException {
    Object id = idProperty.read(ctx);
    if (id != null) {
      idProperty.setValue(bean, id);
    }
    return id;
  }

  @Override
  public Object read(DbReadContext ctx) throws SQLException {
    return idProperty.read(ctx);
  }

  @Override
  public void appendSelect(DbSqlContext ctx, boolean subQuery) {
    idProperty.appendSelect(ctx, subQuery);
  }

  @Override
  public String assocExpr(String prefix, String operator) {
    StringBuilder sb = new StringBuilder(25);
    if (prefix != null) {
      sb.append(prefix);
      sb.append('.');
    }
    sb.append(idProperty.name());
    sb.append(operator);
    return sb.toString();
  }

  @Override
  public String assocInExpr(String prefix) {
    StringBuilder sb = new StringBuilder();
    if (prefix != null) {
      sb.append(prefix);
      sb.append('.');
    }
    sb.append(idProperty.name());
    return sb.toString();
  }

  @Override
  public Object convertId(Object idValue) {
    if (!idValue.getClass().equals(expectedType)) {
      return scalarType.toBeanType(idValue);
    }
    return idValue;
  }

  @Override
  public Object convertSetId(Object idValue, EntityBean bean) {
    if (!idValue.getClass().equals(expectedType)) {
      idValue = scalarType.toBeanType(idValue);
    }
    if (bean != null) {
      // not using interception to support unmodifiable entities
      idProperty.setValue(bean, idValue);
    }
    return idValue;
  }

  @Override
  public String cacheKey(Object value) {
    return scalarType.format(value);
  }

  @Override
  public String cacheKeyFromBean(EntityBean bean) {
    final Object value = idProperty.getValue(bean);
    return scalarType.format(value);
  }

  @Override
  public String idNullOr(String prefix, String filterManyExpression) {
    return "(${" + prefix + "}" + idProperty.dbColumn() + " is null or (" + filterManyExpression + "))";
  }
}
