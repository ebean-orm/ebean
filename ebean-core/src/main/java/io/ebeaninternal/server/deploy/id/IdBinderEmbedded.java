package io.ebeaninternal.server.deploy.id;

import io.ebean.bean.EntityBean;
import io.ebean.util.SplitName;
import io.ebeaninternal.api.SpiExpressionBind;
import io.ebeaninternal.api.SpiExpressionRequest;
import io.ebeaninternal.server.core.DefaultSqlUpdate;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.deploy.BeanProperty;
import io.ebeaninternal.server.deploy.BeanPropertyAssocOne;
import io.ebeaninternal.server.deploy.DbReadContext;
import io.ebeaninternal.server.deploy.DbSqlContext;
import io.ebeaninternal.server.bind.DataBind;

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
  private final boolean idClass;
  private BeanProperty[] props;
  private BeanDescriptor<?> idDesc;
  private String idInValueSql;

  public IdBinderEmbedded(boolean idInExpandedForm, BeanPropertyAssocOne<?> embIdProperty) {
    this.idInExpandedForm = idInExpandedForm;
    this.embIdProperty = embIdProperty;
    this.idClass = "_$IdClass$".equals(embIdProperty.name());
  }

  @Override
  public void initialise() {
    this.idDesc = embIdProperty.targetDescriptor();
    this.props = embIdProperty.properties();
    this.idInValueSql = idInExpandedForm ? idInExpanded() : idInCompressed();
  }

  @Override
  public String idNullOr(String prefix, String filterManyExpression) {
    StringBuilder sb = new StringBuilder(100);
    sb.append("((");
    for (int i = 0; i < props.length; i++) {
      if (i > 0) {
        sb.append(" and ");
      }
      sb.append("${").append(prefix).append('}').append(props[i].dbColumn()).append(" is null");
    }
    sb.append(") or (").append(filterManyExpression).append("))");
    return sb.toString();
  }

  @Override
  public String idSelect() {
    return embIdProperty.name();
  }

  @Override
  public boolean isIdInExpandedForm() {
    return idInExpandedForm;
  }

  private String idInExpanded() {
    final StringBuilder sb = new StringBuilder(30);
    sb.append('(');
    for (int i = 0; i < props.length; i++) {
      if (i > 0) {
        sb.append(" and ");
      }
      sb.append(idDesc.baseTableAlias()).append('.').append(props[i].dbColumn()).append("=?");
    }
    sb.append(')');
    return sb.toString();
  }

  private String idInCompressed() {
    final StringBuilder sb = new StringBuilder(props.length * 2 + 2).append('(');
    for (int i = 0; i < props.length; i++) {
      if (i > 0) {
        sb.append(',');
      }
      sb.append('?');
    }
    sb.append(')');
    return sb.toString();
  }

  @Override
  public BeanProperty beanProperty() {
    return embIdProperty;
  }

  public BeanDescriptor<?> descriptor() {
    return idDesc;
  }

  @Override
  public void buildRawSqlSelectChain(String prefix, List<String> selectChain) {
    if (!idClass) {
      prefix = SplitName.add(prefix, embIdProperty.name());
    }
    for (BeanProperty prop : props) {
      prop.buildRawSqlSelectChain(prefix, selectChain);
    }
  }

  @Override
  public BeanProperty findBeanProperty(String dbColumnName) {
    for (BeanProperty prop : props) {
      if (dbColumnName.equalsIgnoreCase(prop.dbColumn())) {
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
  public int size() {
    return props.length;
  }

  @Override
  public String orderBy() {
    return orderBy(null, true);
  }

  @Override
  public String orderBy(String pathPrefix, boolean ascending) {
    final StringBuilder sb = new StringBuilder(100);
    for (int i = 0; i < props.length; i++) {
      if (i > 0) {
        sb.append(", ");
      }
      if (pathPrefix != null) {
        sb.append(pathPrefix).append('.');
      }
      if (!idClass) {
        sb.append(embIdProperty.name()).append('.');
      }
      sb.append(props[i].name());
      if (!ascending) {
        sb.append(" desc");
      }
    }
    return sb.toString();
  }

  public BeanProperty[] properties() {
    return props;
  }

  @Override
  public String idInValueExprDelete(int size) {
    if (size <= 0) {
      throw new IndexOutOfBoundsException("The size must be at least 1");
    }
    if (!idInExpandedForm) {
      return idInValueExpr(false, size);
    }
    final StringBuilder sb = new StringBuilder(80);
    sb.append('(');
    for (int j = 0; j < size; j++) {
      if (j > 0) {
        sb.append(" or ");
      }
      sb.append('(');
      for (int i = 0; i < props.length; i++) {
        if (i > 0) {
          sb.append(" and ");
        }
        sb.append(props[i].dbColumn()).append("=?");
      }
      sb.append(')');
    }
    sb.append(") ");
    return sb.toString();
  }

  @Override
  public String idInValueExpr(boolean not, int size) {
    if (size <= 0) {
      throw new IndexOutOfBoundsException("The size must be at least 1");
    }
    final StringBuilder sb = new StringBuilder(80);
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
          sb.append(", ");
        }
      }
      sb.append(idInValueSql);
    }
    sb.append(") ");
    return sb.toString();
  }

  @Override
  public Object[] values(EntityBean bean) {
    return bindValues(embIdProperty.getValue(bean));
  }

  @Override
  public Object[] bindValues(Object value) {
    final EntityBean bean = (EntityBean) value;
    final Object[] bindValues = new Object[props.length];
    for (int i = 0; i < props.length; i++) {
      bindValues[i] = props[i].getValue(bean);
    }
    return bindValues;
  }

  /**
   * Convert from embedded bean to Map.
   */
  @Override
  public Object convertForJson(EntityBean bean) {
    final EntityBean ebValue = (EntityBean) embIdProperty.getValue(bean);
    final Map<String, Object> map = new LinkedHashMap<>();
    for (BeanProperty prop : props) {
      map.put(prop.name(), prop.getValue(ebValue));
    }
    return map;
  }

  /**
   * Convert back from a Map to embedded bean.
   */
  @Override
  @SuppressWarnings("unchecked")
  public Object convertFromJson(Object value) {
    final Map<String, Object> map = (Map<String, Object>) value;
    final EntityBean idValue = idDesc.createEntityBean();
    for (BeanProperty prop : props) {
      prop.setValue(idValue, map.get(prop.name()));
    }
    return idValue;
  }

  @Override
  public void bindId(DefaultSqlUpdate sqlUpdate, Object value) {
    final EntityBean bean = (EntityBean) value;
    for (BeanProperty prop : props) {
      sqlUpdate.setParameter(prop.getValue(bean));
    }
  }

  @Override
  public void bindId(DataBind dataBind, Object value) throws SQLException {
    final EntityBean bean = (EntityBean) value;
    for (BeanProperty prop : props) {
      prop.bind(dataBind, prop.getValue(bean));
    }
  }

  @Override
  public void addBindValues(DefaultSqlUpdate sqlUpdate, Collection<?> values) {
    for (Object value : values) {
      bindId(sqlUpdate, value);
    }
  }

  @Override
  public void addBindValues(SpiExpressionBind request, Collection<?> values) {
    for (Object value : values) {
      final EntityBean bean = (EntityBean) value;
      for (BeanProperty prop : props) {
        request.addBindValue(prop.getValue(bean));
      }
    }
  }

  @Override
  public Object readData(DataInput dataInput) throws IOException {
    final EntityBean embId = idDesc.createEntityBean();
    boolean notNull = true;
    for (BeanProperty prop : props) {
      final Object value = prop.readData(dataInput);
      prop.setValue(embId, value);
      if (value == null) {
        notNull = false;
      }
    }
    return notNull ? embId : null;
  }

  @Override
  public void writeData(DataOutput dataOutput, Object idValue) throws IOException {
    final EntityBean bean = (EntityBean) idValue;
    for (BeanProperty prop : props) {
      prop.writeData(dataOutput, prop.getValue(bean));
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
    final EntityBean embId = idDesc.createEntityBean();
    boolean nullValue = true;
    for (BeanProperty prop : props) {
      final Object value = prop.read(ctx);
      if (value != null) {
        prop.setValue(embId, value);
        nullValue = false;
      }
    }
    return nullValue ? null : embId;
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
  public String assocInExpr(String prefix) {
    final StringBuilder sb = new StringBuilder(80);
    sb.append('(');
    for (int i = 0; i < props.length; i++) {
      if (i > 0) {
        sb.append(", ");
      }
      if (prefix != null) {
        sb.append(prefix).append('.');
      }
      sb.append(props[i].name());
    }
    sb.append(')');
    return sb.toString();
  }

  @Override
  public String assocExpr(String prefix, String operator) {
    final StringBuilder sb = new StringBuilder(100);
    for (int i = 0; i < props.length; i++) {
      if (i > 0) {
        sb.append(" and ");
      }
      if (prefix != null) {
        sb.append(prefix).append('.');
      }
      if (!idClass) {
        sb.append(embIdProperty.name()).append('.');
      }
      sb.append(props[i].name()).append(operator);
    }
    return sb.toString();
  }

  @Override
  public String bindEqSql(String baseTableAlias) {
    final StringBuilder sb = new StringBuilder(80);
    for (int i = 0; i < props.length; i++) {
      if (i > 0) {
        sb.append(" and ");
      }
      if (baseTableAlias != null) {
        sb.append(baseTableAlias).append('.');
      }
      sb.append(props[i].dbColumn()).append("=?");
    }
    return sb.toString();
  }

  @Override
  public String bindInSql(String baseTableAlias) {
    if (idInExpandedForm) {
      return "";
    }
    final StringBuilder sb = new StringBuilder(80);
    sb.append('(');
    for (int i = 0; i < props.length; i++) {
      if (i > 0) {
        sb.append(',');
      }
      if (baseTableAlias != null) {
        sb.append(baseTableAlias).append('.');
      }
      sb.append(props[i].dbColumn());
    }
    sb.append(')');
    return sb.toString();
  }

  @Override
  public Object convertId(Object idValue) {
    if (idValue instanceof String) {
      final EntityBean embId = idDesc.createEntityBean();
      final String[] rawValues = ((String)idValue).split("\\|");
      for (int i = 0; i < props.length; i++) {
        props[i].setValue(embId, props[i].parse(rawValues[i]));
      }
      return embId;
    }
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

  @Override
  public String cacheKey(Object value) {
    final EntityBean bean = (EntityBean)value;
    final StringBuilder sb = new StringBuilder(80);
    for (BeanProperty prop : props) {
      Object val = prop.getValue(bean);
      if (val != null) {
        sb.append(prop.format(val));
      }
      sb.append('|');
    }
    return sb.toString();
  }

  @Override
  public String cacheKeyFromBean(EntityBean bean) {
    return cacheKey(embIdProperty.getValue(bean));
  }
}
