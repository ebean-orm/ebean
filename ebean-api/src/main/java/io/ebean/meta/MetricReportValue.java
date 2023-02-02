package io.ebean.meta;

/**
 * A Metric report value used to configure a metric report.
 * This is most likely a REST call from the provided web-application
 */
public class MetricReportValue {
  private String name;
  private String value;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public int intValue() {
    return Integer.valueOf(value);
  }

  public MetricReportValue() {

  }

  public MetricReportValue(String name, Object value) {
    this.name = name;
    this.value = String.valueOf(value);
  }
}
