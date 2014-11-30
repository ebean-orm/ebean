package com.avaje.ebean.config;

/**
 * Defines the Autofetch behaviour for a EbeanServer.
 */
public class AutofetchConfig {

  private AutofetchMode mode = AutofetchMode.DEFAULT_ONIFEMPTY;

  private boolean queryTuning = false;

  private boolean queryTuningAddVersion = false;

  private boolean profiling = false;

  private int profilingMin = 1;

  private int profilingBase = 10;

  private double profilingRate = 0.05;

  private String logDirectory;

  private int profileUpdateFrequency = 60;

  private int garbageCollectionWait = 100;
  
  private boolean garbageCollectionOnShutdown;

  public AutofetchConfig() {
  }

  /**
   * Return the mode used when autofetch has not been explicit defined on a
   * query.
   */
  public AutofetchMode getMode() {
    return mode;
  }

  /**
   * Set the mode used when autofetch has not been explicit defined on a query.
   */
  public void setMode(AutofetchMode mode) {
    this.mode = mode;
  }

  /**
   * Return true if the queries are being tuned.
   */
  public boolean isQueryTuning() {
    return queryTuning;
  }

  /**
   * Set to true if the queries should be tuned by autofetch.
   */
  public void setQueryTuning(boolean queryTuning) {
    this.queryTuning = queryTuning;
  }

  /**
   * Return true if the version property should be added when the query is
   * tuned.
   * <p>
   * If this is false then the version property will be added when profiling
   * detects that the bean is possibly going to be modified.
   * </p>
   */
  public boolean isQueryTuningAddVersion() {
    return queryTuningAddVersion;
  }

  /**
   * Set to true to force the version property to be always added by the query
   * tuning.
   * <p>
   * If this is false then the version property will be added when profiling
   * detects that the bean is possibly going to be modified.
   * </p>
   */
  public void setQueryTuningAddVersion(boolean queryTuningAddVersion) {
    this.queryTuningAddVersion = queryTuningAddVersion;
  }

  /**
   * Return true if profiling information should be collected.
   */
  public boolean isProfiling() {
    return profiling;
  }

  /**
   * Set to true if profiling information should be collected.
   * <p>
   * The profiling information is collected and then used to generate the tuned
   * queries for autofetch.
   * </p>
   */
  public void setProfiling(boolean profiling) {
    this.profiling = profiling;
  }

  /**
   * Return the minimum number of queries to profile before autofetch will start
   * tuning the queries.
   */
  public int getProfilingMin() {
    return profilingMin;
  }

  /**
   * Set the minimum number of queries to profile before autofetch will start
   * tuning the queries.
   */
  public void setProfilingMin(int profilingMin) {
    this.profilingMin = profilingMin;
  }

  /**
   * Return the base number of queries to profile before changing to profile
   * only a percentage of following queries (profileRate).
   */
  public int getProfilingBase() {
    return profilingBase;
  }

  /**
   * Set the based number of queries to profile.
   */
  public void setProfilingBase(int profilingBase) {
    this.profilingBase = profilingBase;
  }

  /**
   * Return the rate (%) of queries to be profiled after the 'base' amount of
   * profiling.
   */
  public double getProfilingRate() {
    return profilingRate;
  }

  /**
   * Set the rate (%) of queries to be profiled after the 'base' amount of
   * profiling.
   */
  public void setProfilingRate(double profilingRate) {
    this.profilingRate = profilingRate;
  }

  /**
   * Return the log directory to put the autofetch log.
   */
  public String getLogDirectory() {
    return logDirectory;
  }

  /**
   * Set the directory to put the autofetch log in.
   */
  public void setLogDirectory(String logDirectory) {
    this.logDirectory = logDirectory;
  }

  /**
   * Return the frequency in seconds to update the autofetch tuned queries from
   * the profiled information.
   */
  public int getProfileUpdateFrequency() {
    return profileUpdateFrequency;
  }

  /**
   * Set the frequency in seconds to update the autofetch tuned queries from the
   * profiled information.
   */
  public void setProfileUpdateFrequency(int profileUpdateFrequency) {
    this.profileUpdateFrequency = profileUpdateFrequency;
  }

  /**
   * Return the time in millis to wait after a system gc to collect profiling
   * information.
   * <p>
   * The profiling information is collected on object finalise. As such we
   * generally don't want to trigger GC (let the JVM do its thing) but on
   * shutdown the autofetch manager will trigger System.gc() and then wait
   * (default 100 millis) to hopefully collect profiling information -
   * especially for short run unit tests.
   * </p>
   */
  public int getGarbageCollectionWait() {
    return garbageCollectionWait;
  }

  /**
   * Set the time in millis to wait after a System.gc() to collect profiling
   * information.
   */
  public void setGarbageCollectionWait(int garbageCollectionWait) {
    this.garbageCollectionWait = garbageCollectionWait;
  }

  
  /**
   * Return true if GC should be trigger on shutdown.
   * <p>
   * Autofetch profiling information is collected as part of garbage collection. 
   * </p>
   */
  public boolean isGarbageCollectionOnShutdown() {
    return garbageCollectionOnShutdown;
  }

  /**
   * Set to true if you want GC to trigger on shutdown.
   * <p>
   *  This would be done if you want to try and collect Autofetch profiling information
   *  on shutdown.
   * </p>
   */
  public void setGarbageCollectionOnShutdown(boolean garbageCollectionOnShutdown) {
    this.garbageCollectionOnShutdown = garbageCollectionOnShutdown;
  }

  /**
   * Load the settings from the properties file.
   */
  public void loadSettings(PropertiesWrapper p) {

    logDirectory = p.get("autofetch.logDirectory", logDirectory);
    queryTuning = p.getBoolean("autofetch.querytuning", queryTuning);
    queryTuningAddVersion = p.getBoolean("autofetch.queryTuningAddVersion", queryTuningAddVersion);
    garbageCollectionOnShutdown = p.getBoolean("autofetch.garbageCollectionOnShutdown", garbageCollectionOnShutdown);
    
    profiling = p.getBoolean("autofetch.profiling", profiling);
    mode = p.getEnum(AutofetchMode.class, "autofetch.implicitmode", mode);

    profilingMin = p.getInt("autofetch.profiling.min", profilingMin);
    profilingBase = p.getInt("autofetch.profiling.base", profilingBase);

    profilingRate = p.getDouble("autofetch.profiling.rate", profilingRate);
    profileUpdateFrequency = p.getInt("autofetch.profiling.updatefrequency", profileUpdateFrequency);
  }
}
