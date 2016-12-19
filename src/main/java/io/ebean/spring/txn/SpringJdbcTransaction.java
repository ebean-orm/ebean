/**
 * Copyright (C) 2009 the original author or authors
 * <p>
 * This file is part of Ebean.
 * <p>
 * Ebean is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 * <p>
 * Ebean is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public License
 * along with Ebean; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA
 */

package io.ebean.spring.txn;

import org.springframework.jdbc.datasource.ConnectionHolder;

import io.ebeaninternal.server.transaction.ExternalJdbcTransaction;
import io.ebeaninternal.server.transaction.TransactionManager;

public class SpringJdbcTransaction extends ExternalJdbcTransaction {

  private final ConnectionHolder holder;

  public SpringJdbcTransaction(ConnectionHolder holder, TransactionManager manager) {
    super("s" + holder.hashCode(), true, holder.getConnection(), manager);
    this.holder = holder;
  }

  @Override
  public boolean isActive() {
    return holder.isSynchronizedWithTransaction();
  }

  public ConnectionHolder getConnectionHolder() {
    return holder;
  }
}
