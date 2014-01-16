package com.avaje.ebeaninternal.api;

import java.io.Serializable;
import java.util.Set;
import java.util.TreeSet;

import com.avaje.ebeaninternal.server.deploy.BeanProperty;
import com.avaje.ebeaninternal.server.deploy.BeanPropertyAssocMany;
import com.avaje.ebeaninternal.server.el.ElPropertyDeploy;
import com.avaje.ebeaninternal.server.query.SplitName;

/**
 * Holds the joins needs to support the many where predicates.
 * These joins are independent of any 'fetch' joins on the many.
 */
public class ManyWhereJoins implements Serializable {

    private static final long serialVersionUID = -6490181101871795417L;
    
    private final TreeSet<String> joins = new TreeSet<String>();

    private StringBuilder formulaProperties = new StringBuilder();
    
    private boolean formulaWithJoin;

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
            joins.add(join);
            String secondaryTableJoinPrefix = p.getSecondaryTableJoinPrefix();
            if (secondaryTableJoinPrefix != null) {
                joins.add(join+"."+secondaryTableJoinPrefix);
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
            joins.add(split[0]);
            addParentJoins(split[0]);
        }
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
    public Set<String> getJoins() {
        return joins;
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
