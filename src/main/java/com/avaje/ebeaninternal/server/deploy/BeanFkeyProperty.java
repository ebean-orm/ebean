package com.avaje.ebeaninternal.server.deploy;

import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebean.text.StringFormatter;
import com.avaje.ebean.text.StringParser;
import com.avaje.ebeaninternal.server.el.ElPropertyValue;

/**
 * Used to evaluate imported foreign keys so as to avoid unnecessary joins.
 */
public final class BeanFkeyProperty implements ElPropertyValue {

  private final String placeHolder;
  private final String prefix;
  private final String name;
  private final String dbColumn;
  private final boolean containsMany;

  private int deployOrder;

  public BeanFkeyProperty(String prefix, String name, String dbColumn, int deployOrder) {
    this(prefix, name, dbColumn, deployOrder, false);
  }
  
  private BeanFkeyProperty(String prefix, String name, String dbColumn, int deployOrder, boolean containsMany) {
    this.prefix = prefix;
    this.name = name;
    this.dbColumn = dbColumn;
    this.deployOrder = deployOrder;
    this.containsMany = containsMany;
    this.placeHolder = calcPlaceHolder(prefix, dbColumn);
  }

  public int getDeployOrder() {
    return deployOrder;
  }

  private String calcPlaceHolder(String prefix, String dbColumn) {
    if (prefix != null) {
      return "${" + prefix + "}" + dbColumn;
    } else {
      return ROOT_ELPREFIX + dbColumn;
    }
  }

  public BeanFkeyProperty create(String expression, boolean containsMany) {
    int len = expression.length() - name.length() - 1;
    String prefix = expression.substring(0, len);

    return new BeanFkeyProperty(prefix, name, dbColumn, deployOrder, containsMany);
  }

  /**
   * Returns false for keys.
   */
  public boolean isDbEncrypted() {
    return false;
  }

  /**
   * Returns false for keys.
   */
  public boolean isLocalEncrypted() {
    return false;
  }

  /**
   * Only usable as ElPropertyDeploy.
   */
  public boolean isDeployOnly() {
    return true;
  }

  @Override
  public boolean containsFormulaWithJoin() {
    return false;
  }

  /**
   * Returns false.
   */
  public boolean containsMany() {
    return containsMany;
  }

  public boolean containsManySince(String sinceProperty) {
    return containsMany();
  }

  public String getDbColumn() {
    return dbColumn;
  }

  public String getName() {
    return name;
  }

  public String getElName() {
    return name;
  }

  /**
   * Returns null as not an AssocOne.
   */
  public Object[] getAssocOneIdValues(EntityBean value) {
    return null;
  }

  /**
   * Returns null as not an AssocOne.
   */
  public String getAssocOneIdExpr(String prefix, String operator) {
    return null;
  }

  /**
   * Returns null as not an AssocOne.
   */
  public String getAssocIdInExpr(String prefix) {
    return null;
  }

  /**
   * Returns null as not an AssocOne.
   */
  public String getAssocIdInValueExpr(int size) {
    return null;
  }

  /**
   * Returns false as not an AssocOne.
   */
  public boolean isAssocId() {
    return false;
  }

  public boolean isAssocProperty() {
    return false;
  }

  public String getElPlaceholder(boolean encrypted) {
    return placeHolder;
  }

  public String getElPrefix() {
    return prefix;
  }

  public boolean isDateTimeCapable() {
    return false;
  }

  public int getJdbcType() {
    return 0;
  }

  public BeanProperty getBeanProperty() {
    return null;
  }

  public Object parseDateTime(long systemTimeMillis) {
    throw new RuntimeException("ElPropertyDeploy only - not implemented");
  }

  public StringFormatter getStringFormatter() {
    throw new RuntimeException("ElPropertyDeploy only - not implemented");
  }

  public StringParser getStringParser() {
    throw new RuntimeException("ElPropertyDeploy only - not implemented");
  }

  public void elSetReference(EntityBean bean) {
    throw new RuntimeException("ElPropertyDeploy only - not implemented");
  }

  public Object elConvertType(Object value) {
    throw new RuntimeException("ElPropertyDeploy only - not implemented");
  }

  public void elSetValue(EntityBean bean, Object value, boolean populate) {
    throw new RuntimeException("ElPropertyDeploy only - not implemented");
  }

  public Object elGetValue(EntityBean bean) {
    throw new RuntimeException("ElPropertyDeploy only - not implemented");
  }

  public Object elGetReference(EntityBean bean) {
    throw new RuntimeException("ElPropertyDeploy only - not implemented");
  }

  public String getDeployProperty() {
    throw new RuntimeException("ElPropertyDeploy only - not implemented");
  }

}
