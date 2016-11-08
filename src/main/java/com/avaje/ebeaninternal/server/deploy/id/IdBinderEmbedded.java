package com.avaje.ebeaninternal.server.deploy.id;

import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebeaninternal.api.SpiExpressionRequest;
import com.avaje.ebeaninternal.server.core.DefaultSqlUpdate;
import com.avaje.ebeaninternal.server.deploy.*;
import com.avaje.ebeaninternal.server.query.SplitName;
import com.avaje.ebeaninternal.server.type.DataBind;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Bind an Id that is an Embedded bean.
 */
public final class IdBinderEmbedded implements IdBinder {

  private final BeanPropertyAssocOne<?> embIdProperty;

  private final boolean idInExpandedForm;

  private BeanProperty[] props;

  private BeanDescriptor<?> idDesc;

  private String idInValueSql;

  public IdBinderEmbedded(boolean idInExpandedForm, BeanPropertyAssocOne<?> embIdProperty) {
    this.idInExpandedForm = idInExpandedForm;
    this.embIdProperty = embIdProperty;
  }

  public void initialise() {
    this.idDesc = embIdProperty.getTargetDescriptor();
    this.props = embIdProperty.getProperties();
    this.idInValueSql = idInExpandedForm ? idInExpanded() : idInCompressed();
  }

  public boolean isIdInExpandedForm() {
    return idInExpandedForm;
  }

  private String idInExpanded() {

    StringBuilder sb = new StringBuilder(30);
    sb.append("(");
    for (int i = 0; i < props.length; i++) {
      if (i > 0) {
        sb.append(" and ");
      }
      sb.append(idDesc.getBaseTableAlias());
      sb.append(".");
      sb.append(props[i].getDbColumn());
      sb.append("=?");
    }
    sb.append(")");

    return sb.toString();
  }

  private String idInCompressed() {
    StringBuilder sb = new StringBuilder();
    sb.append("(");
    for (int i = 0; i < props.length; i++) {
      if (i > 0) {
        sb.append(",");
      }
      sb.append("?");
    }
    sb.append(")");

    return sb.toString();
  }

  @Override
  public BeanProperty getBeanProperty() {
    return embIdProperty;
  }

  public String getOrderBy(String pathPrefix, boolean ascending) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < props.length; i++) {
      if (i > 0) {
        sb.append(", ");
      }
      if (pathPrefix != null) {
        sb.append(pathPrefix).append(".");
      }

      sb.append(embIdProperty.getName()).append(".");
      sb.append(props[i].getName());
      if (!ascending) {
        sb.append(" desc");
      }
    }
    return sb.toString();
  }

  public BeanDescriptor<?> getIdBeanDescriptor() {
    return idDesc;
  }

  public String getIdProperty() {
    return embIdProperty.getName();
  }

  public void buildRawSqlSelectChain(String prefix, List<String> selectChain) {

    prefix = SplitName.add(prefix, embIdProperty.getName());

    for (int i = 0; i < props.length; i++) {
      props[i].buildRawSqlSelectChain(prefix, selectChain);
    }
  }

  public BeanProperty findBeanProperty(String dbColumnName) {
    for (int i = 0; i < props.length; i++) {
      if (dbColumnName.equalsIgnoreCase(props[i].getDbColumn())) {
        return props[i];
      }
    }
    return null;
  }

  public boolean isComplexId() {
    return true;
  }

  public String getDefaultOrderBy() {

    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < props.length; i++) {
      if (i > 0) {
        sb.append(",");
      }

      sb.append(embIdProperty.getName());
      sb.append(".");
      sb.append(props[i].getName());
    }

    return sb.toString();
  }

  public BeanProperty[] getProperties() {
    return props;
  }

  public void addIdInBindValue(SpiExpressionRequest request, Object value) {
    for (int i = 0; i < props.length; i++) {
      request.addBindValue(props[i].getValue((EntityBean) value));
    }
  }

  public String getIdInValueExprDelete(int size) {
    if (!idInExpandedForm) {
      return getIdInValueExpr(size);
    }

    StringBuilder sb = new StringBuilder();
    sb.append("(");

    for (int j = 0; j < size; j++) {
      if (j > 0) {
        sb.append(" or ");
      }
      sb.append("(");
      for (int i = 0; i < props.length; i++) {
        if (i > 0) {
          sb.append(" and ");
        }
        sb.append(props[i].getDbColumn());
        sb.append("=?");
      }
      sb.append(")");
    }
    sb.append(") ");
    return sb.toString();
  }

  public String getIdInValueExpr(int size) {

    StringBuilder sb = new StringBuilder();

    if (!idInExpandedForm) {
      sb.append(" in");
    }
    sb.append(" (");
    for (int i = 0; i < size; i++) {
      if (i > 0) {
        if (idInExpandedForm) {
          sb.append(" or ");
        } else {
          sb.append(",");
        }
      }
      sb.append(idInValueSql);
    }
    sb.append(") ");
    return sb.toString();
  }

  public Object[] getIdValues(EntityBean bean) {
    Object val = embIdProperty.getValue(bean);
    Object[] bindvalues = new Object[props.length];
    for (int i = 0; i < props.length; i++) {
      bindvalues[i] = props[i].getValue((EntityBean) val);
    }
    return bindvalues;
  }

  public Object[] getBindValues(Object value) {

    Object[] bindvalues = new Object[props.length];
    for (int i = 0; i < props.length; i++) {
      bindvalues[i] = props[i].getValue((EntityBean) value);
    }
    return bindvalues;
  }

  /**
   * Convert from embedded bean to Map.
   */
  @Override
  public Object getIdForJson(EntityBean bean) {

    EntityBean ebValue = (EntityBean) embIdProperty.getValue(bean);
    Map<String, Object> map = new LinkedHashMap<>();
    for (int i = 0; i < props.length; i++) {
      map.put(props[i].getName(), props[i].getValue(ebValue));
    }
    return map;
  }

  /**
   * Convert back from a Map to embedded bean.
   */
  @SuppressWarnings("unchecked")
  public Object convertIdFromJson(Object value) {

    Map<String, Object> map = (Map<String, Object>) value;

    EntityBean idValue = idDesc.createEntityBean();
    for (int i = 0; i < props.length; i++) {
      Object val = map.get(props[i].getName());
      props[i].setValue(idValue, val);
    }
    return idValue;
  }


  public void bindId(DefaultSqlUpdate sqlUpdate, Object value) {
    for (int i = 0; i < props.length; i++) {
      Object embFieldValue = props[i].getValue((EntityBean) value);
      sqlUpdate.addParameter(embFieldValue);
    }
  }

  public void bindId(DataBind dataBind, Object value) throws SQLException {

    for (int i = 0; i < props.length; i++) {
      Object embFieldValue = props[i].getValue((EntityBean) value);
      props[i].bind(dataBind, embFieldValue);
    }
  }

  public Object readData(DataInput dataInput) throws IOException {

    EntityBean embId = idDesc.createEntityBean();
    boolean notNull = true;

    for (int i = 0; i < props.length; i++) {
      Object value = props[i].readData(dataInput);
      props[i].setValue(embId, value);
      if (value == null) {
        notNull = false;
      }
    }

    if (notNull) {
      return embId;
    } else {
      return null;
    }
  }

  public void writeData(DataOutput dataOutput, Object idValue) throws IOException {
    for (int i = 0; i < props.length; i++) {
      Object embFieldValue = props[i].getValue((EntityBean) idValue);
      props[i].writeData(dataOutput, embFieldValue);
    }
  }

  public void loadIgnore(DbReadContext ctx) {
    for (int i = 0; i < props.length; i++) {
      props[i].loadIgnore(ctx);
    }
  }

  public Object read(DbReadContext ctx) throws SQLException {

    EntityBean embId = idDesc.createEntityBean();
    boolean notNull = true;

    for (int i = 0; i < props.length; i++) {
      Object value = props[i].readSet(ctx, embId);
      if (value == null) {
        notNull = false;
      }
    }

    if (notNull) {
      return embId;
    } else {
      return null;
    }
  }

  public Object readSet(DbReadContext ctx, EntityBean bean) throws SQLException {

    Object embId = read(ctx);
    if (embId != null) {
      embIdProperty.setValue(bean, embId);
      return embId;
    } else {
      return null;
    }
  }

  public void appendSelect(DbSqlContext ctx, boolean subQuery) {
    for (int i = 0; i < props.length; i++) {
      props[i].appendSelect(ctx, subQuery);
    }
  }

  public String getAssocIdInExpr(String prefix) {

    StringBuilder sb = new StringBuilder();
    sb.append("(");
    for (int i = 0; i < props.length; i++) {
      if (i > 0) {
        sb.append(",");
      }
      if (prefix != null) {
        sb.append(prefix);
        sb.append(".");
      }
      sb.append(props[i].getName());
    }
    sb.append(")");
    return sb.toString();
  }

  public String getAssocOneIdExpr(String prefix, String operator) {

    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < props.length; i++) {
      if (i > 0) {
        sb.append(" and ");
      }
      if (prefix != null) {
        sb.append(prefix);
        sb.append(".");
      }

      sb.append(embIdProperty.getName());
      sb.append(".");
      sb.append(props[i].getName());
      sb.append(operator);
    }
    return sb.toString();
  }

  public String getBindIdSql(String baseTableAlias) {

    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < props.length; i++) {
      if (i > 0) {
        sb.append(" and ");
      }
      if (baseTableAlias != null) {
        sb.append(baseTableAlias);
        sb.append(".");
      }
      sb.append(props[i].getDbColumn());
      sb.append(" = ? ");
    }
    return sb.toString();
  }

  public String getBindIdInSql(String baseTableAlias) {

    if (idInExpandedForm) {
      return "";
    }

    StringBuilder sb = new StringBuilder();
    sb.append("(");
    for (int i = 0; i < props.length; i++) {
      if (i > 0) {
        sb.append(",");
      }
      if (baseTableAlias != null) {
        sb.append(baseTableAlias);
        sb.append(".");
      }
      sb.append(props[i].getDbColumn());
    }
    sb.append(")");
    return sb.toString();
  }

  @Override
  public Object convertId(Object idValue) {
    // can not cast/convert if it is embedded
    return idValue;
  }

  public Object convertSetId(Object idValue, EntityBean bean) {

    // can not cast/convert if it is embedded
    if (bean != null) {
      // support PropertyChangeSupport
      embIdProperty.setValueIntercept(bean, idValue);
    }

    return idValue;
  }
}
