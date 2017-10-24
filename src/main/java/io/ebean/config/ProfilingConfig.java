package io.ebean.config;

/**
 * Configuration for transaction profiling.
 */
public class ProfilingConfig {

  /**
   * When true transaction profiling is enabled.
   */
  private boolean transactionProfiling;

  /**
   * The minimum transaction execution time to be included in profiling.
   */
  private long minimumTransactionMicros;

  /**
   * A specific set of profileIds to include in profiling.
   */
  private int[] includeProfileIds = {};

  /**
   * The number of profiles to write per file.
   */
  private long profilesPerFile = 1000;

  private String directory = "profiling";

  /**
   * Return true if transaction profiling is enabled.
   */
  public boolean isTransactionProfiling() {
    return transactionProfiling;
  }

  /**
   * Set to true to enable transaction profiling.
   */
  public void setTransactionProfiling(boolean transactionProfiling) {
    this.transactionProfiling = transactionProfiling;
  }

  /**
   * Return the minimum transaction execution to be included in profiling.
   */
  public long getMinimumTransactionMicros() {
    return minimumTransactionMicros;
  }

  /**
   * Set the minimum transaction execution to be included in profiling.
   */
  public void setMinimumTransactionMicros(long minimumTransactionMicros) {
    this.minimumTransactionMicros = minimumTransactionMicros;
  }

  /**
   * Return the specific set of profileIds to include in profiling.
   * When not set all transactions with profileIds are included.
   */
  public int[] getIncludeProfileIds() {
    return includeProfileIds;
  }

  /**
   * Set a specific set of profileIds to include in profiling.
   * When not set all transactions with profileIds are included.
   */
  public void setIncludeProfileIds(int[] includeProfileIds) {
    this.includeProfileIds = includeProfileIds;
  }

  /**
   * Return the number of profiles to write to a single file.
   */
  public long getProfilesPerFile() {
    return profilesPerFile;
  }

  /**
   * Set the number of profiles to write to a single file.
   */
  public void setProfilesPerFile(long profilesPerFile) {
    this.profilesPerFile = profilesPerFile;
  }

  /**
   * Return the directory profiling files are put into.
   */
  public String getDirectory() {
    return directory;
  }

  /**
   * Set the directory profiling files are put into.
   */
  public void setDirectory(String directory) {
    this.directory = directory;
  }

  /**
   * Load setting from properties.
   */
  public void loadSettings(PropertiesWrapper p, String name) {
    transactionProfiling = p.getBoolean("profiling.transactionProfiling", transactionProfiling);
    directory = p.get("profiling.directory", directory);
    profilesPerFile = p.getLong("profiling.profilesPerFile", profilesPerFile);
    minimumTransactionMicros = p.getLong("profiling.minimumTransactionMicros", minimumTransactionMicros);

    String includeIds = p.get("profiling.includeProfileIds");
    if (includeIds != null) {
      includeProfileIds = parseIds(includeIds);
    }
  }

  private int[] parseIds(String includeIds) {

    String[] ids = includeIds.split(",");
    int[] vals = new int[ids.length];
    for (int i = 0; i < ids.length; i++) {
      vals[i] = Integer.parseInt(ids[i]);
    }
    return vals;
  }
}
