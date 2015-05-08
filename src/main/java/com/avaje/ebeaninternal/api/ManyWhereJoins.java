package com.avaje.ebeaninternal.api;

import java.io.Serializable;
import java.util.Collection;
import java.util.TreeMap;
import java.util.TreeSet;

import com.avaje.ebeaninternal.server.deploy.BeanProperty;
import com.avaje.ebeaninternal.server.deploy.BeanPropertyAssocMany;
import com.avaje.ebeaninternal.server.el.ElPropertyDeploy;
import com.avaje.ebeaninternal.server.query.SplitName;
import com.avaje.ebeaninternal.server.query.SqlJoinType;

/**
 * Holds the joins needs to support the many where predicates.
 * These joins are independent of any 'fetch' joins on the many.
 */
public class ManyWhereJoins implements Serializable {

    private static final long serialVersionUID = -6490181101871795417L;
    
    private final TreeMap<String,PropertyJoin> joins = new TreeMap<String,PropertyJoin>();

    private StringBuilder formulaProperties = new StringBuilder();
    
    private boolean formulaWithJoin;

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
        if (p instanceof BeanPropertyAssocMany<?>){
            join = addManyToJoin(join, p.getName());          
        }
        if (join != null){
            addJoin(join);
            if (p != null) {
              String secondaryTableJoinPrefix = p.getSecondaryTableJoinPrefix();
              if (secondaryTableJoinPrefix != null) {
                addJoin(join+"."+secondaryTableJoinPrefix);
              }
            }
            addParentJoins(join);
        }
    }
    
    /**
     * For 'many' properties we also need to add the name of the 
     * many property to get the full logical name of the join.
     */
    private String addManyToJoin(String join, String manyPropName){
        if (join == null){
            return manyPropName;
        } else {
            return join+"."+manyPropName;
        }
    }
    
    private void addParentJoins(String join) {
        String[] split = SplitName.split(join);
        if (split[0] != null){
            addJoin(split[0]);
            addParentJoins(split[0]);
        }
    }

    private void addJoin(String property) {
      SqlJoinType joinType = (requireOuterJoins) ? SqlJoinType.OUTER: SqlJoinType.INNER; 
      joins.put(property, new PropertyJoin(property, joinType));
    }
    
    /**
     * Return true if there are no extra many where joins.
     */
    public boolean isEmpty() {
        return joins.isEmpty();
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
      
      TreeSet<String> propertyNames = new TreeSet<String>();
      for (PropertyJoin join : joins.values()) {
        propertyNames.add(join.getProperty());
      }
      return propertyNames;
    }

    /**
     * In findRowCount query found a formula property with a join clause so building a select clause
     * specifically for the findRowCount query.
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
    
    public boolean isHasMany() {
      return formulaWithJoin || !joins.isEmpty();
    }
    
    /**
     * Return true if the findRowCount query just needs the id property in the select clause.
     */
    public boolean isSelectId() {
      return !formulaWithJoin;
    }
    
    /**
     * Return the formula properties to build the select clause for a findRowCount query.
     */
    public String getFormulaProperties() {
      return formulaProperties.toString();
    }

}
