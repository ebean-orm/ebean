package io.ebeaninternal.api;

import io.ebean.CallableSql;

public interface SpiCallableSql extends CallableSql {

  BindParams bindParams();

  TransactionEventTable transactionEventTable();
}
