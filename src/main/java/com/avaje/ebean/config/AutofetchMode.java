package com.avaje.ebean.config;

import com.avaje.ebean.Query;

/**
 * The mode for determining if Autofetch will be used for a given query when
 * {@link Query#setAutofetch(boolean)} has not been explicitly set on a query.
 * <p>
 * The explicit control of {@link Query#setAutofetch(boolean)} will always take
 * precedence. This mode is used when this has not been explicitly set on a
 * query.
 * </p>
 */
public enum AutofetchMode {

  /**
   * Don't implicitly use Autofetch. Must explicitly turn it on.
   */
  DEFAULT_OFF,

  /**
   * Use Autofetch implicitly. Must explicitly turn it off.
   */
  DEFAULT_ON,

  /**
   * Implicitly use Autofetch if the query has not got either select() or join()
   * defined.
   */
  DEFAULT_ONIFEMPTY

}