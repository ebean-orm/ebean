package io.ebeaninternal.server.persist;

import io.ebeaninternal.api.SpiProfileTransactionEvent;
import io.ebeaninternal.api.SpiTransaction;
import org.junit.jupiter.api.Test;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BatchedPstmtTest {

  @Test
  void executeBatch_profilesBeforeClearingBatch() throws SQLException {
    var pstmt = mock(PreparedStatement.class);
    var transaction = mock(SpiTransaction.class);
    var postExecute = mock(BatchPostExecute.class);

    when(pstmt.executeBatch()).thenReturn(new int[]{1});
    when(transaction.profileOffset()).thenReturn(42L);
    doAnswer(invocation -> {
      var event = invocation.getArgument(0, SpiProfileTransactionEvent.class);
      event.profile();
      return null;
    }).when(transaction).profileEvent(any());

    var batched = new BatchedPstmt(pstmt, false, "insert into foo values (?)", transaction);
    batched.add(postExecute);

    batched.executeBatch(false);

    verify(postExecute).checkRowCount(1);
    verify(postExecute).postExecute();
    verify(postExecute).addTimingBatch(anyLong(), eq(1));
    verify(postExecute).profile(42L, 1);
    assertThat(batched.isEmpty()).isTrue();
  }
}
