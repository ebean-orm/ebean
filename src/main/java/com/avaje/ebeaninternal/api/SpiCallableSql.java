package com.avaje.ebeaninternal.api;

import com.avaje.ebean.CallableSql;

public interface SpiCallableSql extends CallableSql {

	public BindParams getBindParams();
	
	public TransactionEventTable getTransactionEventTable();
}
