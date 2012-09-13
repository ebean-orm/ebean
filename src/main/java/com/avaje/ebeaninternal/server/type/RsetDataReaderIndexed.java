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
package com.avaje.ebeaninternal.server.type;

import java.sql.ResultSet;

public class RsetDataReaderIndexed extends RsetDataReader {

    private final int[] rsetIndexPositions;
    
    public RsetDataReaderIndexed(ResultSet rset, int[] rsetIndexPositions, boolean rowNumberIncluded) {
        super(rset);
        if (!rowNumberIncluded){
        	this.rsetIndexPositions = rsetIndexPositions;
        } else {
        	this.rsetIndexPositions = new int[rsetIndexPositions.length+1];
        	for (int i = 0; i < rsetIndexPositions.length; i++) {
        		// increment all the column indexes by 1
        		this.rsetIndexPositions[i+1] = rsetIndexPositions[i]+1;
            }
        }
    }

    @Override
    protected int pos() {
        int i = pos++;
        return rsetIndexPositions[i];
    }

}
