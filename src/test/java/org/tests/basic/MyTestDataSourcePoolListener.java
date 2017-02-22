package org.tests.basic;

import org.avaje.datasource.DataSourcePoolListener;

import java.sql.Connection;

public class MyTestDataSourcePoolListener implements DataSourcePoolListener {
  public static int SLEEP_AFTER_BORROW = 0;

  @Override
  public void onAfterBorrowConnection(Connection c) {
    if (SLEEP_AFTER_BORROW > 0) {
      try {
        Thread.sleep(SLEEP_AFTER_BORROW);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }
  }

  @Override
  public void onBeforeReturnConnection(Connection c) {
  }
}
