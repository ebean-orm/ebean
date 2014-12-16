package com.avaje.ebeaninternal.server.deploy.id;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import com.avaje.ebean.bean.EntityBean;

import com.avaje.ebeaninternal.api.SpiExpressionRequest;
import com.avaje.ebeaninternal.server.core.DefaultSqlUpdate;
import com.avaje.ebeaninternal.server.deploy.BeanProperty;
import com.avaje.ebeaninternal.server.deploy.DbReadContext;
import com.avaje.ebeaninternal.server.deploy.DbSqlContext;
import com.avaje.ebeaninternal.server.type.DataBind;

/**
 * For beans with no id properties AKA report type beans.
 */
public final class IdBinderEmpty implements IdBinder {

  private static final String bindIdSql = "";

  public IdBinderEmpty() {

  }

  public void initialise() {

  }

  public boolean isIdInExpandedForm() {
    return false;
  }

  public String getOrderBy(String pathPrefix, boolean ascending) {
    return pathPrefix;
  }

  public void buildSelectExpressionChain(String prefix, List<String> selectChain) {
  }

  public int getPropertyCount() {
    return 0;
  }

  @Override
  public BeanProperty getBeanProperty() {
    return null;
  }

  public String getIdProperty() {
    return null;
  }

  public BeanProperty findBeanProperty(String dbColumnName) {
    return null;
  }

  public boolean isComplexId() {
    return true;
  }

  public String getDefaultOrderBy() {
    // this should never happen?
    return "";
  }

  public String getBindIdSql(String baseTableAlias) {
    return bindIdSql;
  }

  public String getAssocOneIdExpr(String prefix, String operator) {
    return null;
  }

  public String getAssocIdInExpr(String prefix) {
    return null;
  }

  public void addIdInBindValue(SpiExpressionRequest request, Object value) {

  }

  public String getIdInValueExprDelete(int size) {
    return getIdInValueExpr(size);
  }

  public String getIdInValueExpr(int size) {
    return "";
  }

  public String getBindIdInSql(String baseTableAlias) {
    return null;
  }

  public Object[] getIdValues(EntityBean bean) {
    return null;
  }

  public Object[] getBindValues(Object idValue) {
    return new Object[] { idValue };
  }

  public void bindId(DefaultSqlUpdate sqlUpdate, Object value) {

  }

  public void bindId(DataBind dataBind, Object value) throws SQLException {

  }

  public void loadIgnore(DbReadContext ctx) {
  }

  public Object readSet(DbReadContext ctx, EntityBean bean) throws SQLException {
    return null;
  }

  public Object read(DbReadContext ctx) throws SQLException {
    return null;
  }

  public void appendSelect(DbSqlContext ctx, boolean subQuery) {
  }

  public Object convertSetId(Object idValue, EntityBean bean) {
    return idValue;
  }

  public Object readData(DataInput dataOutput) throws IOException {
    return null;
  }

  public void writeData(DataOutput dataOutput, Object idValue) throws IOException {
    
  }
}
