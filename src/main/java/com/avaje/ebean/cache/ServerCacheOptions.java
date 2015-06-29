package com.avaje.ebean.cache;

import com.avaje.ebean.annotation.CacheTuning;

/**
 * Options for controlling a cache.
 */
public class ServerCacheOptions {

  private int maxSize;
  private int maxIdleSecs;
  private int maxSecsToLive;
  private int trimFrequency;

  /**
   * Construct with no set options.
   */
  public ServerCacheOptions() {

  }

  /**
   * Create from the cacheTuning deployment annotation.
   */
  public ServerCacheOptions(CacheTuning cacheTuning) {
    this.maxSize = cacheTuning.maxSize();
    this.maxIdleSecs = cacheTuning.maxIdleSecs();
    this.maxSecsToLive = cacheTuning.maxSecsToLive();
    this.trimFrequency = cacheTuning.trimFrequency();
  }

  /**
   * Create merging default options with the deployment specified ones.
   */
  public ServerCacheOptions(ServerCacheOptions defaults) {
    this.maxSize = defaults.getMaxSize();
    this.maxIdleSecs = defaults.getMaxIdleSecs();
    this.maxSecsToLive = defaults.getMaxIdleSecs();
    this.trimFrequency = defaults.getTrimFrequency();
  }

  /**
   * Apply any settings from the default settings that have not already been
   * specifically set.
   */
  public void applyDefaults(ServerCacheOptions defaults) {
    if (maxSize == 0) {
      maxSize = defaults.getMaxSize();
    }
    if (maxIdleSecs == 0) {
      maxIdleSecs = defaults.getMaxIdleSecs();
    }
    if (maxSecsToLive == 0) {
      maxSecsToLive = defaults.getMaxSecsToLive();
    }
    if (trimFrequency == 0) {
      trimFrequency = defaults.getTrimFrequency();
    }
  }

  /**
   * Return a copy of this object.
   */
  public ServerCacheOptions copy() {

    ServerCacheOptions copy = new ServerCacheOptions();
    copy.maxSize = maxSize;
    copy.maxIdleSecs = maxIdleSecs;
    copy.maxSecsToLive = maxSecsToLive;
    copy.trimFrequency = trimFrequency;
    return copy;
  }

  /**
   * Return the maximum cache size.
   */
  public int getMaxSize() {
    return maxSize;
  }

  /**
   * Set the maximum cache size.
   */
  public void setMaxSize(int maxSize) {
    this.maxSize = maxSize;
  }

  /**
   * Return the maximum idle time.
   */
  public int getMaxIdleSecs() {
    return maxIdleSecs;
  }

  /**
   * Set the maximum idle time.
   */
  public void setMaxIdleSecs(int maxIdleSecs) {
    this.maxIdleSecs = maxIdleSecs;
  }

  /**
   * Return the maximum time to live.
   */
  public int getMaxSecsToLive() {
    return maxSecsToLive;
  }

  /**
   * Set the maximum time to live.
   */
  public void setMaxSecsToLive(int maxSecsToLive) {
    this.maxSecsToLive = maxSecsToLive;
  }

  /**
   * Return the trim frequency in seconds.
   */
  public int getTrimFrequency() {
    return trimFrequency;
  }

  /**
   * Set the trim frequency in seconds.
   */
  public void setTrimFrequency(int trimFrequency) {
    this.trimFrequency = trimFrequency;
  }
}
