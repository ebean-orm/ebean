package com.avaje.ebeaninternal.server.deploy.id;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import com.avaje.ebean.bean.EntityBean;

import com.avaje.ebeaninternal.api.SpiExpressionRequest;
import com.avaje.ebeaninternal.server.core.DefaultSqlUpdate;
import com.avaje.ebeaninternal.server.core.InternString;
import com.avaje.ebeaninternal.server.deploy.BeanProperty;
import com.avaje.ebeaninternal.server.deploy.DbReadContext;
import com.avaje.ebeaninternal.server.deploy.DbSqlContext;
import com.avaje.ebeaninternal.server.type.DataBind;
import com.avaje.ebeaninternal.server.type.ScalarType;

/**
 * Bind an Id where the Id is made of a single property (not embedded).
 */
public final class IdBinderSimple implements IdBinder {

	private final BeanProperty idProperty;

	private final String bindIdSql;
	
	private final Class<?> expectedType;
	
	@SuppressWarnings("rawtypes")
    private final ScalarType scalarType;
	
	public IdBinderSimple(BeanProperty idProperty) {
		this.idProperty = idProperty;
		this.scalarType = idProperty.getScalarType();
		this.expectedType = idProperty.getPropertyType();
		bindIdSql = InternString.intern(idProperty.getDbColumn()+" = ? ");
	}
	
	public void initialise(){
		// do nothing
	}

  public boolean isIdInExpandedForm() {
    return false;
  }

  public String getOrderBy(String pathPrefix, boolean ascending) {

    StringBuilder sb = new StringBuilder();
    if (pathPrefix != null) {
      sb.append(pathPrefix).append(".");
    }
    sb.append(idProperty.getName());
    if (!ascending) {
      sb.append(" desc");
    }
    return sb.toString();
  }
    
  public void buildSelectExpressionChain(String prefix, List<String> selectChain) {
    
    idProperty.buildSelectExpressionChain(prefix, selectChain);
  }

  /**
   * Returns 1.
   */
  public int getPropertyCount() {
    return 1;
  }
  
	@Override
  public BeanProperty getBeanProperty() {
    return idProperty;
  }

  public String getIdProperty() {
		return idProperty.getName();
	}

	public BeanProperty findBeanProperty(String dbColumnName) {
		if (dbColumnName.equalsIgnoreCase(idProperty.getDbColumn())){
			return idProperty;
		}
		return null;
	}

	public boolean isComplexId(){
		return false;
	}
	
	public String getDefaultOrderBy() {
		return idProperty.getName();
	}

  public String getBindIdInSql(String baseTableAlias) {
    if (baseTableAlias == null) {
      return idProperty.getDbColumn();
    } else {
      return baseTableAlias + "." + idProperty.getDbColumn();
    }
  }

  public String getBindIdSql(String baseTableAlias) {
    if (baseTableAlias == null) {
      return bindIdSql;
    } else {
      return baseTableAlias + "." + bindIdSql;
    }
  }

  public Object[] getIdValues(EntityBean bean) {
    return new Object[] { idProperty.getValue(bean) };
  }

  public Object[] getBindValues(Object idValue) {
    return new Object[] { idValue };
  }

  public String getIdInValueExprDelete(int size) {
    return getIdInValueExpr(size);
  }

  public String getIdInValueExpr(int size) {
    StringBuilder sb = new StringBuilder(2 * size + 10);
    sb.append(" in");
    sb.append(" (?");
    for (int i = 1; i < size; i++) {
      sb.append(",?");
    }
    sb.append(") ");
    return sb.toString();
  }

  public void addIdInBindValue(SpiExpressionRequest request, Object value) {
    value = convertSetId(value, null);
    request.addBindValue(value);
  }

  public void bindId(DefaultSqlUpdate sqlUpdate, Object value) {
    sqlUpdate.addParameter(value);
  }

  public void bindId(DataBind dataBind, Object value) throws SQLException {
    if (!value.getClass().equals(expectedType)) {
      value = scalarType.toBeanType(value);
    }
    idProperty.bind(dataBind, value);
  }

  public void writeData(DataOutput os, Object value) throws IOException {
    idProperty.writeData(os, value);
  }

  public Object readData(DataInput is) throws IOException {
    return idProperty.readData(is);
  }

  public void loadIgnore(DbReadContext ctx) {
    idProperty.loadIgnore(ctx);
  }

  public Object readSet(DbReadContext ctx, EntityBean bean) throws SQLException {
    Object id = idProperty.read(ctx);
    if (id != null) {
      idProperty.setValue(bean, id);
    }
    return id;
  }

  public Object read(DbReadContext ctx) throws SQLException {
    return idProperty.read(ctx);
  }

  public void appendSelect(DbSqlContext ctx, boolean subQuery) {
    idProperty.appendSelect(ctx, subQuery);
  }

  public String getAssocOneIdExpr(String prefix, String operator) {

    StringBuilder sb = new StringBuilder();
    if (prefix != null) {
      sb.append(prefix);
      sb.append(".");
    }
    sb.append(idProperty.getName());
    sb.append(operator);
    return sb.toString();
  }

  public String getAssocIdInExpr(String prefix) {

    StringBuilder sb = new StringBuilder();
    if (prefix != null) {
      sb.append(prefix);
      sb.append(".");
    }
    sb.append(idProperty.getName());
    return sb.toString();
  }

  public Object convertSetId(Object idValue, EntityBean bean) {

    if (!idValue.getClass().equals(expectedType)) {
      idValue = scalarType.toBeanType(idValue);
    }

    if (bean != null) {
      // support PropertyChangeSupport
      idProperty.setValueIntercept(bean, idValue);
    }

    return idValue;
  }
}
