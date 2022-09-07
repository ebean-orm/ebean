package io.ebean.spring.txn;

import io.ebeaninternal.server.transaction.ExternalJdbcTransaction;
import io.ebeaninternal.server.transaction.TransactionManager;
import org.springframework.jdbc.datasource.ConnectionHolder;

final class SpringJdbcTransaction extends ExternalJdbcTransaction {

  private final ConnectionHolder holder;

  SpringJdbcTransaction(ConnectionHolder holder, TransactionManager manager) {
    super("s" + holder.hashCode(), true, holder.getConnection(), manager);
    this.holder = holder;
  }

  @Override
  public boolean isActive() {
    return holder.isSynchronizedWithTransaction();
  }
}
