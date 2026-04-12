package io.ebeaninternal.server.deploy.id;

import io.ebean.bean.EntityBean;
import io.ebeaninternal.api.SpiExpressionBind;
import io.ebeaninternal.api.SpiExpressionRequest;
import io.ebeaninternal.server.bind.DataBind;
import io.ebeaninternal.server.core.DefaultSqlUpdate;
import io.ebeaninternal.server.deploy.BeanProperty;
import io.ebeaninternal.server.deploy.DbReadContext;
import io.ebeaninternal.server.deploy.DbSqlContext;

import java.io.DataInput;
import java.io.DataOutput;
import java.util.Collection;
import java.util.List;

/**
 * For beans with no id properties AKA report type beans.
 */
final class IdBinderEmpty implements IdBinder {

  private static final String bindIdSql = "";

  public IdBinderEmpty() {
  }

  @Override
  public void initialise() {
  }

  @Override
  public String idSelect() {
    return "";
  }

  @Override
  public boolean isIdInExpandedForm() {
    return false;
  }

  @Override
  public String orderBy(String pathPrefix, boolean ascending) {
    return pathPrefix;
  }

  @Override
  public void buildRawSqlSelectChain(String prefix, List<String> selectChain) {
  }

  @Override
  public BeanProperty beanProperty() {
    return null;
  }

  @Override
  public BeanProperty findBeanProperty(String dbColumnName) {
    return null;
  }

  @Override
  public boolean isComplexId() {
    return true;
  }

  @Override
  public String orderBy() {
    // this should never happen?
    return "";
  }

  @Override
  public String bindEqSql(String baseTableAlias) {
    return bindIdSql;
  }

  @Override
  public String assocExpr(String prefix, String operator) {
    return null;
  }

  @Override
  public String assocInExpr(String prefix) {
    return null;
  }

  @Override
  public String idInValueExprDelete(int size) {
    return idInValueExpr(false, size);
  }

  @Override
  public String idInValueExpr(boolean not, int size) {
    return "";
  }

  @Override
  public String bindInSql(String baseTableAlias) {
    return null;
  }

  @Override
  public Object[] values(EntityBean bean) {
    return null;
  }

  @Override
  public Object[] bindValues(Object idValue) {
    return new Object[]{idValue};
  }

  @Override
  public Object convertForJson(EntityBean bean) {
    return null;
  }

  @Override
  public Object convertFromJson(Object value) {
    return value;
  }

  @Override
  public void bindId(DefaultSqlUpdate sqlUpdate, Object value) {
  }

  @Override
  public void bindId(DataBind dataBind, Object value) {
  }

  @Override
  public void addBindValues(DefaultSqlUpdate sqlUpdate, Collection<?> ids) {
  }

  @Override
  public void addBindValues(SpiExpressionBind request, Collection<?> ids) {
  }

  @Override
  public void loadIgnore(DbReadContext ctx) {
  }

  @Override
  public Object readSet(DbReadContext ctx, EntityBean bean) {
    return null;
  }

  @Override
  public Object read(DbReadContext ctx) {
    return null;
  }

  @Override
  public void appendSelect(DbSqlContext ctx, boolean subQuery) {
  }

  @Override
  public Object convertSetId(Object idValue, EntityBean bean) {
    return idValue;
  }

  @Override
  public Object convertId(Object idValue) {
    return idValue;
  }

  @Override
  public Object readData(DataInput dataOutput) {
    return null;
  }

  @Override
  public void writeData(DataOutput dataOutput, Object idValue) {
  }

  @Override
  public String cacheKey(Object bean) {
    return null;
  }

  @Override
  public String cacheKeyFromBean(EntityBean bean) {
    return null;
  }
}
