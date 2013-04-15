package com.avaje.ebean.meta;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;

import com.avaje.ebean.bean.ObjectGraphOrigin;

/**
 * "Tuned fetch" information used by AutoFetch.
 * <p>
 * Note that the queryPoint is effectively the Id field for this bean.
 * </p>
 * <p>
 * The queryPoint identifies both the query and call stack.
 * </p>
 */
@Entity
public class MetaAutoFetchTunedQueryInfo implements Serializable {

  private static final long serialVersionUID = 3119991928889170215L;

  @Id
  private String id;

  private String beanType;

  /**
   * The profile query point (call stack and query).
   */
  private ObjectGraphOrigin origin;

  /**
   * The tuned query details with joins and properties.
   */
  private String tunedDetail;

  /**
   * The number of times profiling has been collected for this query point.
   */
  private int profileCount;

  /**
   * The number of queries tuned by this info.
   */
  private int tunedCount;

  private long lastTuneTime;

  public MetaAutoFetchTunedQueryInfo() {

  }

  public MetaAutoFetchTunedQueryInfo(final ObjectGraphOrigin origin, String tunedDetail,
      int profileCount, int tunedCount, long lastTuneTime) {

    this.origin = origin;
    this.beanType = origin == null ? null : origin.getBeanType();
    this.id = origin == null ? null : origin.getKey();
    this.tunedDetail = tunedDetail;
    this.profileCount = profileCount;
    this.tunedCount = tunedCount;
    this.lastTuneTime = lastTuneTime;
  }

  /**
   * Return the query point key.
   */
  public String getId() {
    return id;
  }

  /**
   * Return the type of bean this is tuned for.
   */
  public String getBeanType() {
    return beanType;
  }

  /**
   * Return the query point.
   */
  public ObjectGraphOrigin getOrigin() {
    return origin;
  }

  /**
   * The tuned query detail in string form.
   */
  public String getTunedDetail() {
    return tunedDetail;
  }

  /**
   * The number of profiled queries the tuned query is based on.
   */
  public int getProfileCount() {
    return profileCount;
  }

  /**
   * Return the number of queries tuned.
   */
  public int getTunedCount() {
    return tunedCount;
  }

  /**
   * Return the time of the last tune (that changed the query).
   */
  public long getLastTuneTime() {
    return lastTuneTime;
  }

  public String toString() {
    return "origin[" + origin + "] query[" + tunedDetail + "] profileCount[" + profileCount + "]";
  }
}
