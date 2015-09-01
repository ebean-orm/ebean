package com.avaje.ebeaninternal.server.autofetch.service;

import com.avaje.ebean.bean.ObjectGraphOrigin;
import com.avaje.ebeaninternal.api.SpiQuery;
import com.avaje.ebeaninternal.server.querydefn.OrmQueryDetail;

import java.io.Serializable;

/**
 * Holds tuned query information. Is immutable so this represents the tuning at
 * a given point in time.
 */
public class TunedQueryInfo implements Serializable {

  private final ObjectGraphOrigin origin;

  private final OrmQueryDetail tunedDetail;

  public TunedQueryInfo(ObjectGraphOrigin queryPoint, OrmQueryDetail tunedDetail) {
    this.origin = queryPoint;
    this.tunedDetail = tunedDetail;
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
      query.setAutoFetchTuned(true);
    }
    return tuned;
  }

  public String toString() {
    return origin.getBeanType() + " " + origin.getKey() + " " + tunedDetail;
  }

}
