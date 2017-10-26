package io.ebeaninternal.server.transaction;

/**
 * Profiling information for a single transaction that has completed.
 */
public class TransactionProfile {

  private long startTime;
  /**
   * The profileId of the transaction (On @Transactional explicitly or can be automatically set by enhancement).
   */
  private int profileId;

  /**
   * The total execution time of the transaction (for filtering out small/short transactions).
   */
  private long totalMicros;

  /**
   * The binary encoding of the transaction profiling events.
   */
  private String data;

  private Summary summary;

  /**
   * Create with profileId, total micros and encoded profile data.
   */
  public TransactionProfile(long startTime, int profileId) {
    this.startTime = startTime;
    this.profileId = profileId;
    this.summary = new Summary();
  }

  /**
   * Construct for JSON tools.
   */
  public TransactionProfile(){
  }

  /**
   * Return the transaction start time.
   */
  public long getStartTime() {
    return startTime;
  }

  /**
   * Return the transaction profileId.
   */
  public int getProfileId() {
    return profileId;
  }

  /**
   * Return the total transaction execution time in micros.
   */
  public long getTotalMicros() {
    return totalMicros;
  }

  /**
   * Return the profiling data in encoded form.
   */
  public String getData() {
    return data;
  }

  /**
   * Set start time (for JSON tools).
   */
  public void setStartTime(long startTime) {
    this.startTime = startTime;
  }

  /**
   * Set profileId (for JSON tools).
   */
  public void setProfileId(int profileId) {
    this.profileId = profileId;
  }

  /**
   * Set total micros (for JSON tools).
   */
  public void setTotalMicros(long totalMicros) {
    this.totalMicros = totalMicros;
  }

  /**
   * Set raw data (for JSON tools).
   */
  public void setData(String data) {
    this.data = data;
  }

  public Summary getSummary() {
    return summary;
  }

  public void setSummary(Summary summary) {
    this.summary = summary;
  }

  public static class Summary {
    public long queryMicros;
    public long queryCount;
    public long queryBeans;
    public long queryMax;

    public long persistMicros;
    public long persistCount;
    public long persistBeans;
    public long persistOneCount;

    public long commitMicros;

    void addPersist(long micros, int beanCount) {
      persistMicros += micros;
      persistBeans += beanCount;
      persistCount++;

      if (beanCount == 1) {
        persistOneCount++;
      }
    }

    void addQuery(long micros, int beanCount) {
      queryMax = Math.max(queryMax, micros);
      queryMicros += micros;
      queryBeans += beanCount;
      queryCount++;
    }
  }
}
