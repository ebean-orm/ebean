package com.avaje.ebeaninternal.server.autotune.service;

import com.avaje.ebeaninternal.api.SpiQuery;
import com.avaje.ebeaninternal.server.autotune.model.Origin;
import com.avaje.ebeaninternal.server.querydefn.OrmQueryDetail;
import com.avaje.ebeaninternal.server.querydefn.OrmQueryDetailParser;

import java.io.Serializable;

/**
 * Holds tuned query information. Is immutable so this represents the tuning at
 * a given point in time.
 */
public class TunedQueryInfo implements Serializable {

  private final Origin origin;

  private final OrmQueryDetail tunedDetail;

  public TunedQueryInfo(Origin origin) {
    this.origin = origin;
    this.tunedDetail = new OrmQueryDetailParser(origin.getDetail()).parse();
  }

  /**
   * Return the origin entry (includes call stack and bean type).
   */
  public Origin getOrigin() {
    return origin;
  }

  /**
   * Return the tuned detail (for comparison with profiling information).
   */
  public OrmQueryDetail getTunedDetail() {
    return tunedDetail;
  }

  /**
   * Tune the query by replacing its OrmQueryDetail with a tuned one.
   *
   * @return true if the query was tuned, otherwise false.
   */
  public boolean tuneQuery(SpiQuery<?> query) {
    if (tunedDetail == null) {
      return false;
    }

    boolean tuned;
    if (query.isDetailEmpty()) {
      tuned = true;
      // tune by 'replacement'
      query.setDetail(tunedDetail.copy());
    } else {
      // tune by 'addition'
      tuned = query.tuneFetchProperties(tunedDetail);
    }
    if (tuned) {
      query.setAutoTuned(true);
    }
    return tuned;
  }

  public String toString() {
    return tunedDetail.toString();
  }

}
