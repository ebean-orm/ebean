package io.ebeaninternal.server.transaction;

import io.ebean.ProfileLocation;

/**
 * Default transaction profiling event collection.
 */
public final class DefaultProfileStream implements ProfileStream {

  private final long startNanos;
  private final StringBuilder buffer;
  private final TransactionProfile profile;
  private final TransactionProfile.Summary summary;

  DefaultProfileStream(ProfileLocation location, boolean verbose) {
    this.startNanos = System.nanoTime();
    this.profile = new TransactionProfile(System.currentTimeMillis(), location);
    this.summary = profile.getSummary();
    this.buffer = (verbose) ? new StringBuilder(200) : null;
  }

  /**
   * Return the time offset from the beginning of the transaction.
   */
  @Override
  public long offset() {
    return ((System.nanoTime() - startNanos) / 1_000L);
  }

  private long exeMicros(long offset) {
    return offset() - offset;
  }

  /**
   * Add a query execution event.
   */
  @Override
  public void addQueryEvent(String event, long offset, String beanName, int beanCount, String queryId) {
    long micros = exeMicros(offset);
    summary.addQuery(micros, beanCount);
    if (buffer != null) {
      add(micros, event, offset, beanName, beanCount, queryId);
    }
  }

  /**
   * Add a persist event.
   */
  @Override
  public void addPersistEvent(String event, long offset, String beanName, int beanCount) {
    long micros = exeMicros(offset);
    summary.addPersist(micros, beanCount);
    if (buffer != null) {
      add(micros, event, offset, beanName, beanCount, "");
    }
  }

  /**
   * Add the commit/rollback event.
   */
  @Override
  public void addEvent(String event, long offset) {
    long micros = exeMicros(offset);
    summary.commitMicros = micros;
    if (buffer != null) {
      buffer.append(event).append(',');
      buffer.append(offset).append(',');
      buffer.append(micros).append(';');
    }
  }

  private void add(long micros, String event, long offset, String beanName, int beanCount, String queryId) {
    buffer.append(event).append(',');
    buffer.append(offset).append(',');
    buffer.append(micros).append(',');
    buffer.append(beanName).append(',');
    buffer.append(beanCount).append(',');
    buffer.append(queryId).append(";");
  }

  /**
   * End the transaction profiling.
   */
  @Override
  public void end(TransactionManager manager) {
    profile.setTotalMicros(offset());
    if (buffer != null) {
      profile.setData(buffer.toString());
    }
    manager.profileCollect(profile);
  }

}
