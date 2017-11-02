package io.ebeaninternal.server.deploy.id;

import io.ebean.bean.EntityBean;
import io.ebeaninternal.api.SpiExpressionRequest;
import io.ebeaninternal.server.core.DefaultSqlUpdate;
import io.ebeaninternal.server.deploy.BeanProperty;
import io.ebeaninternal.server.deploy.DbReadContext;
import io.ebeaninternal.server.deploy.DbSqlContext;
import io.ebeaninternal.server.type.DataBind;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

/**
 * For beans with no id properties AKA report type beans.
 */
public final class IdBinderEmpty implements IdBinder {

  private static final String bindIdSql = "";

  public IdBinderEmpty() {

  }

  @Override
  public void initialise() {

  }

  @Override
  public boolean isIdInExpandedForm() {
    return false;
  }

  @Override
  public String getOrderBy(String pathPrefix, boolean ascending) {
    return pathPrefix;
  }

  @Override
  public void buildRawSqlSelectChain(String prefix, List<String> selectChain) {
  }

  @Override
  public BeanProperty getBeanProperty() {
    return null;
  }

  @Override
  public String getIdProperty() {
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
  public String getDefaultOrderBy() {
    // this should never happen?
    return "";
  }

  @Override
  public String getBindIdSql(String baseTableAlias) {
    return bindIdSql;
  }

  @Override
  public String getAssocOneIdExpr(String prefix, String operator) {
    return null;
  }

  @Override
  public String getAssocIdInExpr(String prefix) {
    return null;
  }

  @Override
  public String getIdInValueExprDelete(int size) {
    return getIdInValueExpr(false, size);
  }

  @Override
  public String getIdInValueExpr(boolean not, int size) {
    return "";
  }

  @Override
  public String getBindIdInSql(String baseTableAlias) {
    return null;
  }

  @Override
  public Object[] getIdValues(EntityBean bean) {
    return null;
  }

  @Override
  public Object[] getBindValues(Object idValue) {
    return new Object[]{idValue};
  }

  @Override
  public Object getIdForJson(EntityBean bean) {
    return null;
  }

  @Override
  public Object convertIdFromJson(Object value) {
    return value;
  }

  @Override
  public void bindId(DefaultSqlUpdate sqlUpdate, Object value) {

  }

  @Override
  public void bindId(DataBind dataBind, Object value) throws SQLException {

  }

  @Override
  public void addIdInBindValues(DefaultSqlUpdate sqlUpdate, Collection<?> ids) {

  }

  @Override
  public void addIdInBindValues(SpiExpressionRequest request, Collection<?> ids) {

  }

  @Override
  public void loadIgnore(DbReadContext ctx) {
  }

  @Override
  public Object readSet(DbReadContext ctx, EntityBean bean) throws SQLException {
    return null;
  }

  @Override
  public Object read(DbReadContext ctx) throws SQLException {
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
  public Object readData(DataInput dataOutput) throws IOException {
    return null;
  }

  @Override
  public void writeData(DataOutput dataOutput, Object idValue) throws IOException {

  }
}
