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
package com.avaje.ebeaninternal.server.query;

import java.sql.ResultSet;
import java.util.List;

import com.avaje.ebean.RawSql.ColumnMapping;
import com.avaje.ebean.config.dbplatform.SqlLimitResponse;
import com.avaje.ebeaninternal.server.core.OrmQueryRequest;
import com.avaje.ebeaninternal.server.type.DataReader;
import com.avaje.ebeaninternal.server.type.RsetDataReaderIndexed;

public class CQueryPlanRawSql extends CQueryPlan {

    private final int[] rsetIndexPositions;
    
    public CQueryPlanRawSql(OrmQueryRequest<?> request, SqlLimitResponse sqlRes, SqlTree sqlTree, String logWhereSql) {
        
        super(request, sqlRes, sqlTree, true, logWhereSql, null);
        
        this.rsetIndexPositions = createIndexPositions(request, sqlTree);
    }

    public DataReader createDataReader(ResultSet rset){
    	
        return new RsetDataReaderIndexed(rset, rsetIndexPositions, isRowNumberIncluded());
    }
    
    
    private int[] createIndexPositions(OrmQueryRequest<?> request, SqlTree sqlTree) {
        
        List<String> chain = sqlTree.buildSelectExpressionChain();
        ColumnMapping columnMapping = request.getQuery().getRawSql().getColumnMapping();
        
        int[] indexPositions = new int[chain.size()];
        
        for (int i = 0; i < chain.size(); i++) {
            String expr = chain.get(i);
            int indexPos = 1 + columnMapping.getIndexPosition(expr);
            indexPositions[i] = indexPos;
        }

        return indexPositions;
    }
}
