package io.ebeaninternal.server.deploy;

import io.ebean.bean.EntityBean;
import io.ebean.plugin.Property;
import io.ebean.text.StringParser;
import io.ebeaninternal.api.SpiExpressionRequest;
import io.ebeaninternal.server.el.ElPropertyValue;

/**
 * Used to evaluate imported foreign keys so as to avoid unnecessary joins.
 */
public final class BeanFkeyProperty implements ElPropertyValue {

  private final String placeHolder;
  private final String prefix;
  private final String name;
  private final String dbColumn;
  private final boolean containsMany;
  private final int deployOrder;

  public BeanFkeyProperty(String name, String dbColumn, int deployOrder) {
    this(null, name, dbColumn, deployOrder, false);
  }

  private BeanFkeyProperty(String prefix, String name, String dbColumn, int deployOrder, boolean containsMany) {
    this.prefix = prefix;
    this.name = name;
    this.dbColumn = dbColumn;
    this.deployOrder = deployOrder;
    this.containsMany = containsMany;
    this.placeHolder = calcPlaceHolder(prefix, dbColumn);
  }

  @Override
  public String toString() {
    return "prefix:" + prefix + " name:" + name + " dbColumn:" + dbColumn + " ph:" + placeHolder;
  }

  @Override
  public boolean isAggregation() {
    return false;
  }

  @Override
  public int fetchPreference() {
    // return some decently high value
    return 1000;
  }

  private String calcPlaceHolder(String prefix, String dbColumn) {
    if (prefix != null) {
      return "${" + prefix + "}" + dbColumn;
    } else {
      return ROOT_ELPREFIX + dbColumn;
    }
  }

  public BeanFkeyProperty create(String expression, boolean pathContainsMany) {
    int len = expression.length() - name.length() - 1;
    String prefix = expression.substring(0, len);
    return new BeanFkeyProperty(prefix, name, dbColumn, deployOrder, containsMany || pathContainsMany);
  }

  /**
   * Returns false for keys.
   */
  @Override
  public boolean isDbEncrypted() {
    return false;
  }

  /**
   * Returns false for keys.
   */
  @Override
  public boolean isLocalEncrypted() {
    return false;
  }

  @Override
  public Object localEncrypt(Object value) {
    throw new IllegalArgumentException("Should not get here?");
  }

  @Override
  public boolean containsFormulaWithJoin() {
    return false;
  }

  /**
   * Returns false.
   */
  @Override
  public boolean containsMany() {
    return containsMany;
  }

  @Override
  public boolean containsManySince(String sinceProperty) {
    return containsMany();
  }

  @Override
  public String dbColumn() {
    return dbColumn;
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public String elName() {
    return name;
  }

  /**
   * Returns null as not an AssocOne.
   */
  @Override
  public Object[] assocIdValues(EntityBean value) {
    return null;
  }

  /**
   * Returns null as not an AssocOne.
   */
  @Override
  public String assocIdExpression(String prefix, String operator) {
    return null;
  }

  /**
   * Returns null as not an AssocOne.
   */
  @Override
  public String assocIdInExpr(String prefix) {
    return null;
  }

  /**
   * Returns null as not an AssocOne.
   */
  @Override
  public String assocIdInValueExpr(boolean not, int size) {
    return null;
  }

  @Override
  public String assocIsEmpty(SpiExpressionRequest request, String path) {
    throw new RuntimeException("Not Supported or Expected");
  }

  @Override
  public boolean isAssocMany() {
    return false;
  }

  /**
   * Returns false as not an AssocOne.
   */
  @Override
  public boolean isAssocId() {
    return false;
  }

  @Override
  public boolean isAssocProperty() {
    return false;
  }

  @Override
  public String elPlaceholder(boolean encrypted) {
    return placeHolder;
  }

  @Override
  public String elPrefix() {
    return prefix;
  }

  @Override
  public int jdbcType() {
    return 0;
  }

  @Override
  public BeanProperty beanProperty() {
    return null;
  }

  @Override
  public StringParser stringParser() {
    throw new RuntimeException("ElPropertyDeploy only - not implemented");
  }

  @Override
  public Object convert(Object value) {
    throw new RuntimeException("ElPropertyDeploy only - not implemented");
  }

  @Override
  public void pathSet(Object bean, Object value) {
    throw new RuntimeException("ElPropertyDeploy only - not implemented");
  }

  @Override
  public Object pathGet(Object bean) {
    throw new RuntimeException("ElPropertyDeploy only - not implemented");
  }

  @Override
  public Object pathGetNested(Object bean) {
    throw new RuntimeException("ElPropertyDeploy only - not implemented");
  }

  @Override
  public Property property() {
    throw new RuntimeException("ElPropertyDeploy only - not implemented");
  }

}
