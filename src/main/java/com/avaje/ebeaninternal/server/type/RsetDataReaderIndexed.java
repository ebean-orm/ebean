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
