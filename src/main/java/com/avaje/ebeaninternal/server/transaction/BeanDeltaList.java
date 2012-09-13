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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.avaje.ebeaninternal.server.cluster.BinaryMessageList;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;

public class BeanDeltaList {

    private final BeanDescriptor<?> beanDescriptor;
    
    private final List<BeanDelta> deltaBeans = new ArrayList<BeanDelta>();

    public BeanDeltaList(BeanDescriptor<?> beanDescriptor) {
        this.beanDescriptor = beanDescriptor;
    }

    public String toString() {
        return deltaBeans.toString();
    }
    
    public BeanDescriptor<?> getBeanDescriptor() {
        return beanDescriptor;
    }

    public void add(BeanDelta b) {
        deltaBeans.add(b);
    }
    
    public List<BeanDelta> getDeltaBeans() {
        return deltaBeans;
    }

    public void writeBinaryMessage(BinaryMessageList msgList) throws IOException {
        for (int i = 0; i < deltaBeans.size(); i++) {
            deltaBeans.get(i).writeBinaryMessage(msgList);
        }
    }
    
}
