package io.ebeaninternal.server.deploy.id;

import io.ebean.bean.EntityBean;
import io.ebean.util.SplitName;
import io.ebeaninternal.api.SpiExpressionRequest;
import io.ebeaninternal.server.core.DefaultSqlUpdate;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.deploy.BeanProperty;
import io.ebeaninternal.server.deploy.BeanPropertyAssocOne;
import io.ebeaninternal.server.deploy.DbReadContext;
import io.ebeaninternal.server.deploy.DbSqlContext;
import io.ebeaninternal.server.type.DataBind;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
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

  @Override
  public void initialise() {
    this.idDesc = embIdProperty.getTargetDescriptor();
    this.props = embIdProperty.getProperties();
    this.idInValueSql = idInExpandedForm ? idInExpanded() : idInCompressed();
  }

  @Override
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

  @Override
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

  @Override
  public String getIdProperty() {
    return embIdProperty.getName();
  }

  @Override
  public void buildRawSqlSelectChain(String prefix, List<String> selectChain) {

    prefix = SplitName.add(prefix, embIdProperty.getName());

    for (BeanProperty prop : props) {
      prop.buildRawSqlSelectChain(prefix, selectChain);
    }
  }

  @Override
  public BeanProperty findBeanProperty(String dbColumnName) {
    for (BeanProperty prop : props) {
      if (dbColumnName.equalsIgnoreCase(prop.getDbColumn())) {
        return prop;
      }
    }
    return null;
  }

  @Override
  public boolean isComplexId() {
    return true;
  }

  @Override
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

  @Override
  public String getIdInValueExprDelete(int size) {
    if (size <= 0) {
      throw new IndexOutOfBoundsException("The size must be at least 1");
    }
    if (!idInExpandedForm) {
      return getIdInValueExpr(false, size);
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

  @Override
  public String getIdInValueExpr(boolean not, int size) {
    if (size <= 0) {
      throw new IndexOutOfBoundsException("The size must be at least 1");
    }
    StringBuilder sb = new StringBuilder();
    if (not) {
      sb.append(" not");
    }
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

  @Override
  public Object[] getIdValues(EntityBean bean) {
    Object val = embIdProperty.getValue(bean);
    Object[] bindvalues = new Object[props.length];
    for (int i = 0; i < props.length; i++) {
      bindvalues[i] = props[i].getValue((EntityBean) val);
    }
    return bindvalues;
  }

  @Override
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
    for (BeanProperty prop : props) {
      map.put(prop.getName(), prop.getValue(ebValue));
    }
    return map;
  }

  /**
   * Convert back from a Map to embedded bean.
   */
  @Override
  @SuppressWarnings("unchecked")
  public Object convertIdFromJson(Object value) {

    Map<String, Object> map = (Map<String, Object>) value;

    EntityBean idValue = idDesc.createEntityBean();
    for (BeanProperty prop : props) {
      Object val = map.get(prop.getName());
      prop.setValue(idValue, val);
    }
    return idValue;
  }


  @Override
  public void bindId(DefaultSqlUpdate sqlUpdate, Object value) {
    for (BeanProperty prop : props) {
      Object embFieldValue = prop.getValue((EntityBean) value);
      sqlUpdate.setNextParameter(embFieldValue);
    }
  }

  @Override
  public void bindId(DataBind dataBind, Object value) throws SQLException {

    for (BeanProperty prop : props) {
      Object embFieldValue = prop.getValue((EntityBean) value);
      prop.bind(dataBind, embFieldValue);
    }
  }

  @Override
  public void addIdInBindValues(DefaultSqlUpdate sqlUpdate, Collection<?> values) {
    for (Object value : values) {
      bindId(sqlUpdate, value);
    }
  }

  @Override
  public void addIdInBindValues(SpiExpressionRequest request, Collection<?> values) {
    for (Object value : values) {
      for (BeanProperty prop : props) {
        request.addBindValue(prop.getValue((EntityBean) value));
      }
    }
  }

  @Override
  public Object readData(DataInput dataInput) throws IOException {

    EntityBean embId = idDesc.createEntityBean();
    boolean notNull = true;

    for (BeanProperty prop : props) {
      Object value = prop.readData(dataInput);
      prop.setValue(embId, value);
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

  @Override
  public void writeData(DataOutput dataOutput, Object idValue) throws IOException {
    for (BeanProperty prop : props) {
      Object embFieldValue = prop.getValue((EntityBean) idValue);
      prop.writeData(dataOutput, embFieldValue);
    }
  }

  @Override
  public void loadIgnore(DbReadContext ctx) {
    for (BeanProperty prop : props) {
      prop.loadIgnore(ctx);
    }
  }

  @Override
  public Object read(DbReadContext ctx) throws SQLException {

    EntityBean embId = idDesc.createEntityBean();
    boolean notNull = true;

    for (BeanProperty prop : props) {
      Object value = prop.readSet(ctx, embId);
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

  @Override
  public Object readSet(DbReadContext ctx, EntityBean bean) throws SQLException {

    Object embId = read(ctx);
    if (embId != null) {
      embIdProperty.setValue(bean, embId);
      return embId;
    } else {
      return null;
    }
  }

  @Override
  public void appendSelect(DbSqlContext ctx, boolean subQuery) {
    for (BeanProperty prop : props) {
      prop.appendSelect(ctx, subQuery);
    }
  }

  @Override
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

  @Override
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

  @Override
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

  @Override
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

  @Override
  public Object convertSetId(Object idValue, EntityBean bean) {

    // can not cast/convert if it is embedded
    if (bean != null) {
      // support PropertyChangeSupport
      embIdProperty.setValueIntercept(bean, idValue);
    }

    return idValue;
  }
}
