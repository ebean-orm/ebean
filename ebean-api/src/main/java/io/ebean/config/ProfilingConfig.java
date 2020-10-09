package io.ebean.config;

/**
 * Configuration for transaction profiling.
 */
public class ProfilingConfig {

  /**
   * When true transaction profiling is enabled.
   */
  private boolean enabled;

  /**
   * Set true for verbose mode.
   */
  private boolean verbose;

  /**
   * The minimum transaction execution time to be included in profiling.
   */
  private long minimumMicros;

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
  public boolean isEnabled() {
    return enabled;
  }

  /**
   * Set to true to enable transaction profiling.
   */
  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  /**
   * Return true if verbose mode is used.
   */
  public boolean isVerbose() {
    return verbose;
  }

  /**
   * Set to true to use verbose mode.
   */
  public void setVerbose(boolean verbose) {
    this.verbose = verbose;
  }

  /**
   * Return the minimum transaction execution to be included in profiling.
   */
  public long getMinimumMicros() {
    return minimumMicros;
  }

  /**
   * Set the minimum transaction execution to be included in profiling.
   */
  public void setMinimumMicros(long minimumMicros) {
    this.minimumMicros = minimumMicros;
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

    enabled = p.getBoolean("profiling", enabled);
    verbose = p.getBoolean("profiling.verbose", verbose);

    directory = p.get("profiling.directory", directory);
    profilesPerFile = p.getLong("profiling.profilesPerFile", profilesPerFile);
    minimumMicros = p.getLong("profiling.minimumMicros", minimumMicros);

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
