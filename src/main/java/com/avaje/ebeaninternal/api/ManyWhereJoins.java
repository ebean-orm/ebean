/**
 * Copyright (C) 2009 Authors
 * 
 * This file is part of Ebean.
 * 
 * Ebean is free software; you can redistribute it and/or modify it 
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *  
 * Ebean is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Ebean; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA  
 */
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
    
}
