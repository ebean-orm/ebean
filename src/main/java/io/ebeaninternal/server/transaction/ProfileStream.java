package io.ebeaninternal.server.transaction;

import io.ebeaninternal.api.TxnProfileEventCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * A binary encoding of the transaction profiling events.
 */
public class ProfileStream implements TxnProfileEventCodes {

  private static final Logger logger = LoggerFactory.getLogger(ProfileStream.class);

  private final int profId;
  private final long startNanos;
  private final DataOutputStream out;
  private final ByteArrayOutputStream profileBuffer;

  public ProfileStream(int profId) {
    this.profId = profId;
    this.startNanos = System.nanoTime();
    this.profileBuffer = new ByteArrayOutputStream(200);
    this.out = new DataOutputStream(profileBuffer);
    try {
      out.writeLong(System.currentTimeMillis());
      out.writeInt(profId);
    } catch (IOException e) {
      throw new RuntimeException("Unexpected error starting transaction profiling", e);
    }
  }

  /**
   * Return the time offset from the beginning of the transaction.
   */
  public int offset() {
    // int max of 2,147,483,648 as micros = 35 minutes
    // use 10_000 to get 100th of millis to max at 357 minutes (almost 6 hours)
    // not micros so we can reasonably use int rather than long
    return (int)((System.nanoTime() - startNanos) / 10_000L);
  }

  /**
   * Add the commit/rollback event.
   */
  public void addEvent(byte event, int startOffset) {
    try {
      out.writeByte(event);
      out.writeInt(startOffset);
      out.writeInt(offset() - startOffset);
    } catch (IOException e) {
      logger.error("Error writing event to transaction profiling", e);
    }
  }

  /**
   * Add a query execution event.
   */
  public void addQueryEvent(byte event, int offset, short beanTypeId, int beanCount, short queryId) {
    add(event, offset, beanTypeId, beanCount, queryId);
  }

  /**
   * Add a persist event.
   */
  public void addEvent(byte event, int offset, short beanTypeId, int beanCount) {
    add(event, offset, beanTypeId, beanCount, (short)0);
  }

  private void add(byte event, int offset, short beanTypeId, int beanCount, short queryId) {
    try {
      out.writeByte(event);
      out.writeInt(offset);
      out.writeInt(offset() - offset);
      out.writeShort(beanTypeId);
      out.writeInt(beanCount);
      out.writeShort(queryId);
    } catch (IOException e) {
      logger.error("Error writing event to transaction profiling", e);
    }
  }

  /**
   * End the transaction profiling.
   */
  public void end(TransactionManager manager) {
    try {
      long totalMicros = ((System.nanoTime() - startNanos) / 1_000L);
      out.writeByte(EVT_END);
      out.writeLong(totalMicros);
      out.flush();
      out.close();
      manager.profileCollect(new TransactionProfile(profId, totalMicros, profileBuffer.toByteArray()));
    } catch (IOException e) {
      logger.error("Error flushing and collecting profiling", e);
    }
  }

}
