package com.avaje.ebeaninternal.server.query;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import com.avaje.ebeaninternal.api.ManyWhereJoins;
import com.avaje.ebeaninternal.server.deploy.BeanProperty;
import com.avaje.ebeaninternal.server.deploy.TableJoin;

/**
 * The select properties for a node in the SqlTree.
 */
public class SqlTreeProperties {

  private static final TableJoin[] EMPTY_TABLE_JOINS = new TableJoin[0];

  /**
   * True if this node of the tree should have read only entity beans.
   */
  private boolean readOnly;

  private TableJoin[] tableJoins = EMPTY_TABLE_JOINS;

  /**
   * The bean properties in order.
   */
  private final List<BeanProperty> propsList = new ArrayList<BeanProperty>();

  /**
   * Maintain a list of property names to detect embedded bean additions.
   */
  private final LinkedHashSet<String> propNames = new LinkedHashSet<String>();

  private boolean allProperties;

  private boolean aggregation;

  public SqlTreeProperties() {
  }

  public boolean containsProperty(String propName) {
    return propNames.contains(propName);
  }

  public void add(BeanProperty[] props) {
    //noinspection ManualArrayToCollectionCopy
    for (int i = 0; i < props.length; i++) {
      propsList.add(props[i]);
    }
  }

  public void add(BeanProperty prop) {
    propsList.add(prop);
    propNames.add(prop.getName());
  }

  public BeanProperty[] getProps() {
    return propsList.toArray(new BeanProperty[propsList.size()]);
  }

  public boolean isPartialObject() {
    return !allProperties;
  }

  public boolean isReadOnly() {
    return readOnly;
  }

  public void setReadOnly(boolean readOnly) {
    this.readOnly = readOnly;
  }

  public TableJoin[] getTableJoins() {
    return tableJoins;
  }

  public void setTableJoins(TableJoin[] tableJoins) {
    this.tableJoins = tableJoins;
  }

  public void setAllProperties() {
    this.allProperties = true;
  }

  /**
   * Check for an aggregation property and set manyWhereJoin as needed.
   * <p>
   * Return true if a Sql distinct is required.
   * </p>
   */
  public boolean requireSqlDistinct(ManyWhereJoins manyWhereJoins) {
    String joinProperty = aggregationJoin();
    if (joinProperty != null) {
      aggregation = true;
      manyWhereJoins.addAggregationJoin(joinProperty);
      return false;
    } else{
      return manyWhereJoins.requireSqlDistinct();
    }
  }

  /**
   * Return true if this contains an aggregation property.
   */
  public boolean isAggregation() {
    return aggregation;
  }

  /**
   * Return the property to join for aggregation.
   */
  private String aggregationJoin() {
    if (!allProperties) {
      for (BeanProperty beanProperty : propsList) {
        if (beanProperty.isAggregation()) {
          return beanProperty.getElPrefix();
        }
      }
    }
    return null;
  }
}