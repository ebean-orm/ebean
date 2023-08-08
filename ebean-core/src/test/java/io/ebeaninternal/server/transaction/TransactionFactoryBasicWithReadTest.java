package io.ebeaninternal.server.transaction;

import io.ebeaninternal.api.SpiTransaction;
import io.ebeaninternal.api.SpiTxnLogger;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;

import java.sql.Connection;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class TransactionFactoryBasicWithReadTest {

  @Test
  void createReadOnlyTransaction_expect_usesMasterDataSource() throws SQLException {
    var connection = mock(Connection.class);
    var tm = mockTransactionManager();
    var dataSource = mockDataSource(connection);
    var dss = mock(DataSourceSupplier.class);
    when(dss.dataSource()).thenReturn(dataSource);

    var txnFactory = new TransactionFactoryBasicWithRead(tm, dss);

    // act
    SpiTransaction txn = txnFactory.createReadOnlyTransaction(null, true);

    assertThat(txn).isNotNull();
    verify(dataSource).getConnection(); // used the 'master' dataSource
  }

  @Test
  void createReadOnlyTransaction_expect_usesReadOnlyDataSource() throws SQLException {
    var tm = mockTransactionManager();
    var connection = mock(Connection.class);
    var readOnlyDataSource = mockDataSource(connection);

    var dss = mock(DataSourceSupplier.class);
    when(dss.readOnlyDataSource()).thenReturn(readOnlyDataSource);
    var txnFactory = new TransactionFactoryBasicWithRead(tm, dss);

    // act
    SpiTransaction txn = txnFactory.createReadOnlyTransaction(null, false);

    assertThat(txn).isNotNull();
    verify(readOnlyDataSource).getConnection(); // used the 'read only' dataSource
  }

  private static DataSource mockDataSource(Connection dsConnection) throws SQLException {
    var readOnlyDataSource = mock(DataSource.class);
    when(readOnlyDataSource.getConnection()).thenReturn(dsConnection);
    return readOnlyDataSource;
  }

  private static TransactionManager mockTransactionManager() {
    var tm = mock(TransactionManager.class);
    when(tm.loggerReadOnly()).thenReturn(mock(SpiTxnLogger.class));
    return tm;
  }
}
