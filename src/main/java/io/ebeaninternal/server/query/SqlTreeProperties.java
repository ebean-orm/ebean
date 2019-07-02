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
   * The bean properties in order.
   */
  private final List<STreeProperty> propsList = new ArrayList<>();

  /**
   * Maintain a list of property names to detect embedded bean additions.
   */
  private final LinkedHashSet<String> propNames = new LinkedHashSet<>();

  private boolean allProperties;

  private boolean aggregationManyToOne;

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
    if (prop.isAggregation()) {
      if (!aggregation) {
        aggregation = true;
        aggregationPath = prop.getElPrefix();
      }
      if (prop.isAggregationManyToOne()) {
        aggregationManyToOne = true;
      }
    }
  }

  public STreeProperty[] getProps() {
    return propsList.toArray(new STreeProperty[0]);
  }

  boolean isPartialObject() {
    return !allProperties;
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
   * Return true if this is an aggregation formula on a ManyToOne.
   */
  boolean isAggregationManyToOne() {
    return aggregationManyToOne;
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
    return aggregationPath;
  }

  /**
   * Return true if a top level aggregation which means the Id property must be excluded.
   */
  boolean isAggregationRoot() {
    return aggregation && (aggregationPath == null);
  }
}
