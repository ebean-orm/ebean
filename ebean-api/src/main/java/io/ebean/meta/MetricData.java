package io.ebean.meta;

/**
 * An individual metric.
 */
public class MetricData {

  private String name;
  private String hash;
  private String loc;
  private String sql;

  private Long count;
  private Long mean;
  private Long max;
  private Long total;

  public MetricData(String name) {
    this.name = name;
  }

  public MetricData() {
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getHash() {
    return hash;
  }

  public void setHash(String hash) {
    this.hash = hash;
  }

  public String getLoc() {
    return loc;
  }

  public void setLoc(String loc) {
    this.loc = loc;
  }

  public String getSql() {
    return sql;
  }

  public void setSql(String sql) {
    this.sql = sql;
  }

  public Long getCount() {
    return count;
  }

  public void setCount(Long count) {
    this.count = count;
  }

  public Long getMean() {
    return mean;
  }

  public void setMean(Long mean) {
    this.mean = mean;
  }

  public Long getMax() {
    return max;
  }

  public void setMax(Long max) {
    this.max = max;
  }

  public Long getTotal() {
    return total;
  }

  public void setTotal(Long total) {
    this.total = total;
  }
}
