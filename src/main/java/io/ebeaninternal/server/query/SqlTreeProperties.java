package io.ebeaninternal.server.query;

import io.ebeaninternal.api.ManyWhereJoins;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * The select properties for a node in the SqlTree.
 */
public class SqlTreeProperties {

  /**
   * True if this node of the tree should have read only entity beans.
   */
  private boolean readOnly;

  /**
   * The bean properties in order.
   */
  private final List<STreeProperty> propsList = new ArrayList<>();

  /**
   * Maintain a list of property names to detect embedded bean additions.
   */
  private final LinkedHashSet<String> propNames = new LinkedHashSet<>();

  private boolean allProperties;

  private boolean aggregation;

  private String aggregationPath;

  SqlTreeProperties() {
  }

  boolean containsProperty(String propName) {
    return propNames.contains(propName);
  }

  public void add(STreeProperty[] props) {
    propsList.addAll(Arrays.asList(props));
  }

  public void add(STreeProperty prop) {
    propsList.add(prop);
    propNames.add(prop.getName());
  }

  public STreeProperty[] getProps() {
    return propsList.toArray(new STreeProperty[propsList.size()]);
  }

  boolean isPartialObject() {
    return !allProperties;
  }

  public boolean isReadOnly() {
    return readOnly;
  }

  public void setReadOnly(boolean readOnly) {
    this.readOnly = readOnly;
  }

  void setAllProperties() {
    this.allProperties = true;
  }

  /**
   * Check for an aggregation property and set manyWhereJoin as needed.
   * <p>
   * Return true if a Sql distinct is required.
   * </p>
   */
  boolean requireSqlDistinct(ManyWhereJoins manyWhereJoins) {
    String joinProperty = aggregationJoin();
    if (joinProperty != null) {
      manyWhereJoins.addAggregationJoin(joinProperty);
      return false;
    } else {
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
      for (STreeProperty beanProperty : propsList) {
        if (beanProperty.isAggregation()) {
          aggregation = true;
          aggregationPath = beanProperty.getElPrefix();
          return aggregationPath;
        }
      }
    }
    return null;
  }

  /**
   * Return true if a top level aggregation which means the Id property must be excluded.
   */
  public boolean isAggregationRoot() {
    return aggregation && (aggregationPath == null);
  }
}
