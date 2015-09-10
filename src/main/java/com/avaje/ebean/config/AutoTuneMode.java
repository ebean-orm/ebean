package com.avaje.ebean.config;

import com.avaje.ebean.Query;

/**
 * The mode for determining if AutoTune will be used for a given query when
 * {@link Query#setAutoTune(boolean)} has not been explicitly set on a query.
 * <p>
 * The explicit control of {@link Query#setAutoTune(boolean)} will always take
 * precedence. This mode is used when this has not been explicitly set on a
 * query.
 * </p>
 */
public enum AutoTuneMode {

  /**
   * Don't implicitly use AutoTune. Must explicitly turn it on.
   */
  DEFAULT_OFF,

  /**
   * Use AutoTune implicitly. Must explicitly turn it off.
   */
  DEFAULT_ON,

  /**
   * Implicitly use AutoTune if the query has not got either select() or join()
   * defined.
   */
  DEFAULT_ONIFEMPTY

}