package com.avaje.ebeaninternal.server.lib.sql;

import java.sql.Connection;

/**
 * Helper object that can convert between transaction isolation descriptions and values.
 *
 */
public class TransactionIsolation {


    /**
     * return the isolation level for a given string description.
     */
	public static int getLevel(String level) {
	    level = level.toUpperCase();
	    if (level.startsWith("TRANSACTION")){
	    	level = level.substring("TRANSACTION".length());
	    }
	    level = level.replace("_", "");
	    if ("NONE".equalsIgnoreCase(level)){
	        return Connection.TRANSACTION_NONE;
	    }
	    if ("READCOMMITTED".equalsIgnoreCase(level)){
	        return Connection.TRANSACTION_READ_COMMITTED;
	    }
	    if ("READUNCOMMITTED".equalsIgnoreCase(level)){
	        return Connection.TRANSACTION_READ_UNCOMMITTED;
	    }
	    if ("REPEATABLEREAD".equalsIgnoreCase(level)){
	        return Connection.TRANSACTION_REPEATABLE_READ;
	    }
	    if ("SERIALIZABLE".equalsIgnoreCase(level)){
	        return Connection.TRANSACTION_SERIALIZABLE;
	    }

		throw new RuntimeException("Transaction Isolaction level [" + level + "] is not known.");
	}
    
	/**
	 * Return the string description of the transaction isolation level specified.
	 * <p>Returned value is one of NONE, READ_COMMITTED,READ_UNCOMMITTED, 
	 * REPEATABLE_READ or SERIALIZABLE.</p>
	 * 
	 * @param level the transaction isolation level as per java.sql.Connection
	 * @return the level description as a string.
	 */
	public static String getLevelDescription(int level) {
		switch (level) {
			case Connection.TRANSACTION_NONE :
				return "NONE";
			case Connection.TRANSACTION_READ_COMMITTED :
				return "READ_COMMITTED";
			case Connection.TRANSACTION_READ_UNCOMMITTED :
				return "READ_UNCOMMITTED";
			case Connection.TRANSACTION_REPEATABLE_READ :
				return "REPEATABLE_READ";
			case Connection.TRANSACTION_SERIALIZABLE :
				return "SERIALIZABLE";
            case -1 :
                return "NotSet";
			default :
				throw new RuntimeException("Transaction Isolaction level [" + level + "] is not defined.");
		}
	}



}
