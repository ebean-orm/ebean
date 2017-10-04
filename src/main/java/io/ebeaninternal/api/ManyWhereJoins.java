package io.ebeaninternal.api;

import io.ebeaninternal.server.deploy.BeanProperty;
import io.ebeaninternal.server.deploy.BeanPropertyAssocMany;
import io.ebeaninternal.server.el.ElPropertyDeploy;
import io.ebean.util.SplitName;
import io.ebeaninternal.server.query.SqlJoinType;

import java.io.Serializable;
import java.util.Collection;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Holds the joins needs to support the many where predicates.
 * These joins are independent of any 'fetch' joins on the many.
 */
public class ManyWhereJoins implements Serializable {

  private static final long serialVersionUID = -6490181101871795417L;

  private final TreeMap<String, PropertyJoin> joins = new TreeMap<>();

  private StringBuilder formulaProperties = new StringBuilder();

  private boolean formulaWithJoin;

  private boolean aggregation;

  /**
   * 'Mode' indicating that joins added while this is true are required to be outer joins.
   */
  private boolean requireOuterJoins;

  /**
   * Return the current 'mode' indicating if outer joins are currently required or not.
   */
  public boolean isRequireOuterJoins() {
    return requireOuterJoins;
  }

  /**
   * Set the 'mode' to be that joins added are required to be outer joins.
   * This is set during the evaluation of disjunction predicates.
   */
  public void setRequireOuterJoins(boolean requireOuterJoins) {
    this.requireOuterJoins = requireOuterJoins;
  }

  /**
   * Add a many where join.
   */
  public void add(ElPropertyDeploy elProp) {

    String join = elProp.getElPrefix();
    BeanProperty p = elProp.getBeanProperty();
    if (p instanceof BeanPropertyAssocMany<?>) {
      join = addManyToJoin(join, p.getName());
    }
    if (join != null) {
      addJoin(join);
      if (p != null) {
        String secondaryTableJoinPrefix = p.getSecondaryTableJoinPrefix();
        if (secondaryTableJoinPrefix != null) {
          addJoin(join + "." + secondaryTableJoinPrefix);
        }
      }
      addParentJoins(join);
    }
  }

  /**
   * For 'many' properties we also need to add the name of the
   * many property to get the full logical name of the join.
   */
  private String addManyToJoin(String join, String manyPropName) {
    if (join == null) {
      return manyPropName;
    } else {
      return join + "." + manyPropName;
    }
  }

  private void addParentJoins(String join) {
    String[] split = SplitName.split(join);
    if (split[0] != null) {
      addJoin(split[0]);
      addParentJoins(split[0]);
    }
  }

  private void addJoin(String property) {
    SqlJoinType joinType = (requireOuterJoins) ? SqlJoinType.OUTER : SqlJoinType.INNER;
    joins.put(property, new PropertyJoin(property, joinType));
  }

  /**
   * Return true if this is an aggregation query or if there are no extra many where joins.
   */
  public boolean requireSqlDistinct() {
    return !aggregation && !joins.isEmpty();
  }

  /**
   * Return the set of many where joins.
   */
  public Collection<PropertyJoin> getPropertyJoins() {
    return joins.values();
  }

  /**
   * Return the set of property names for the many where joins.
   */
  public TreeSet<String> getPropertyNames() {

    TreeSet<String> propertyNames = new TreeSet<>();
    for (PropertyJoin join : joins.values()) {
      propertyNames.add(join.getProperty());
    }
    return propertyNames;
  }

  /**
   * In findCount query found a formula property with a join clause so building a select clause
   * specifically for the findCount query.
   */
  public void addFormulaWithJoin(String propertyName) {
    if (formulaWithJoin) {
      formulaProperties.append(",");
    } else {
      formulaProperties = new StringBuilder();
      formulaWithJoin = true;
    }
    formulaProperties.append(propertyName);
  }

  /**
   * Return true if the query select includes a formula with join.
   */
  public boolean isFormulaWithJoin() {
    return formulaWithJoin;
  }

  /**
   * Return the formula properties to build the select clause for a findCount query.
   */
  public String getFormulaProperties() {
    return formulaProperties.toString();
  }

  /**
   * Mark this as part of an aggregation query (so using group by clause).
   */
  public void setAggregation() {
    aggregation = true;
  }

  /**
   * Ensure we have the join required to support the aggregation properties.
   */
  public void addAggregationJoin(String property) {
    this.aggregation = true;
    joins.put(property, new PropertyJoin(property, SqlJoinType.INNER));
  }
}
