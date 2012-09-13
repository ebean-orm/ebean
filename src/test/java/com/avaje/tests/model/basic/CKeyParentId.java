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
package com.avaje.tests.model.basic;

import javax.persistence.Embeddable;

@Embeddable
public class CKeyParentId {

	Integer oneKey;
	String twoKey;
	
	public CKeyParentId() {
		
	}
	public CKeyParentId(Integer oneKey, String twoKey){
		this.oneKey = oneKey;
		this.twoKey = twoKey;
	}
	
	public Integer getOneKey() {
		return oneKey;
	}
	public void setOneKey(Integer oneKey) {
		this.oneKey = oneKey;
	}
	public String getTwoKey() {
		return twoKey;
	}
	public void setTwoKey(String twoKey) {
		this.twoKey = twoKey;
	}

	@Override
    public boolean equals(Object o) {
        if (this == o){
            return true;
        }
        if (!(o instanceof CKeyParentId)){
            return false;
        }

        CKeyParentId otherKey = (CKeyParentId) o;
        return otherKey.hashCode() == hashCode();
    }

    @Override
    public int hashCode(){
        int hc = getClass().getName().hashCode();
        hc = 31 * hc + oneKey;
        hc = 31 * hc + twoKey.hashCode();
        return hc;
    }
}
