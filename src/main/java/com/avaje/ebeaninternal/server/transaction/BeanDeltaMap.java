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
package com.avaje.ebeaninternal.server.transaction;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;

public class BeanDeltaMap {

    private Map<String,BeanDeltaList> deltaMap = new HashMap<String,BeanDeltaList>();

    public BeanDeltaMap() {
    }
    
    public BeanDeltaMap(List<BeanDelta> deltaBeans) {
        if (deltaBeans != null){
            for (int i = 0; i < deltaBeans.size(); i++) {
                BeanDelta deltaBean = deltaBeans.get(i);
                addBeanDelta(deltaBean);
            }
        }
    }
    
    public String toString() {
        return deltaMap.values().toString();
    }
    
    public void addBeanDelta(BeanDelta beanDelta){
        BeanDescriptor<?> d  = beanDelta.getBeanDescriptor();
        BeanDeltaList list = getDeltaBeanList(d);
        list.add(beanDelta);        
    }
    
    public Collection<BeanDeltaList> deltaLists() {
        return deltaMap.values();
    }
    
    private BeanDeltaList getDeltaBeanList(BeanDescriptor<?> d) {
        BeanDeltaList deltaList = deltaMap.get(d.getFullName());
        if (deltaList == null){
            deltaList = new BeanDeltaList(d);
            deltaMap.put(d.getFullName(), deltaList);
        }
        return deltaList;
    }
}
