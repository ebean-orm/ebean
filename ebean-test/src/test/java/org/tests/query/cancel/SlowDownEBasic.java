package org.tests.query.cancel;

import io.ebean.DB;
import io.ebean.Transaction;
import org.h2.api.Trigger;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Class to artificially slow down selects on 'e_basic' table
 */
public class SlowDownEBasic implements Trigger {

  private static int wait;

  private static boolean triggerInstalled;

  @Override
  public void init(final Connection conn, final String schemaName, final String triggerName, final String tableName,
      final boolean before, final int type) {
  }

  @Override
  public void fire(final Connection conn, final Object[] oldRow, final Object[] newRow) {
    try {
      Thread.sleep(wait);
    } catch (InterruptedException e) {
      // nop
    }
  }

  @Override
  public void close() {
  }

  @Override
  public void remove() {
  }


  public static void setSelectWaitMillis(final int wait) throws SQLException {
    SlowDownEBasic.wait = wait;
    if (triggerInstalled) {
      return;
    }
    triggerInstalled = true;
    try (Transaction txn = DB.beginTransaction(); Statement stmt = txn.connection().createStatement()) {

      stmt.execute("CREATE TRIGGER SLOW_DOWN_E_BASIC BEFORE SELECT ON e_basic " + "CALL \""
          + SlowDownEBasic.class.getName() + "\"");
      txn.commit();
    }
  }
}
