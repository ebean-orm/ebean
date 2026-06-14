package io.ebean.meta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Canonical "v2" mapping of Ebean's internal flat metric names (e.g.
 * {@code orm.Customer.findList}, {@code iud.User.save}, {@code txn.named.X},
 * {@code l2.<region>.<op>}) into a metric family name plus a tag string following
 * the label-tag convention.
 *
 * <p>This is the source-of-truth mapping for the v2 metrics JSON form
 * ({@link ServerMetricsAsJson#writeV2(Appendable)}). The tag string is a canonical,
 * sorted, comma separated list of {@code key:value} pairs, e.g.
 * {@code "kind:orm,label:Customer.findList,type:Customer"}.
 *
 * <table>
 *   <caption>Ebean prefix → family name + tags</caption>
 *   <tr><th>Ebean prefix</th><th>name</th><th>tags</th></tr>
 *   <tr><td>{@code iud.X}</td><td>{@code ebean.dml}</td><td>{@code label=X}</td></tr>
 *   <tr><td>{@code orm.X}</td><td>{@code ebean.query}</td><td>{@code kind=orm, type=<bean>, label=X}</td></tr>
 *   <tr><td>{@code dto.X}</td><td>{@code ebean.query}</td><td>{@code kind=dto, type=<bean>, label=X}</td></tr>
 *   <tr><td>{@code sql.X}</td><td>{@code ebean.query}</td><td>{@code kind=sql, type=<bean>, label=X}</td></tr>
 *   <tr><td>{@code txn.named.X} / {@code txn.X}</td><td>{@code ebean.txn}</td><td>{@code label=X}</td></tr>
 *   <tr><td>{@code l2.<region>.<op>}</td><td>{@code ebean.l2}</td><td>{@code op=<op>, region=<region>}</td></tr>
 *   <tr><td>(unrecognised)</td><td>{@code ebean.other}</td><td>{@code label=<original name>}</td></tr>
 * </table>
 *
 * <p>The {@code kind} tag is the query category (orm/dto/sql) while the {@code type}
 * tag is the queried bean/entity simple name. The {@code type} tag is omitted when
 * the bean type is unknown.
 */
final class MetricNamingV2 {

  /** Result of a name mapping: family name plus canonical tag string. */
  static final class Mapped {
    private final String name;
    private final String tags;

    Mapped(String name, String tags) {
      this.name = name;
      this.tags = tags;
    }

    String name() {
      return name;
    }

    String tags() {
      return tags;
    }
  }

  private MetricNamingV2() {
  }

  /**
   * Map an Ebean flat metric name (and optional bean type for query metrics) into
   * the canonical family name plus tag string.
   */
  static Mapped map(String ebeanName, String beanType) {
    if (ebeanName == null || ebeanName.isEmpty()) {
      return new Mapped("ebean.other", "");
    }
    int firstDot = ebeanName.indexOf('.');
    if (firstDot <= 0) {
      return new Mapped("ebean.other", tags("label", ebeanName));
    }
    String prefix = ebeanName.substring(0, firstDot);
    String rest = ebeanName.substring(firstDot + 1);
    switch (prefix) {
      case "iud":
        return new Mapped("ebean.dml", tags("label", rest));
      case "orm":
        return query("orm", rest, beanType);
      case "dto":
        return query("dto", rest, beanType);
      case "sql":
        return query("sql", rest, beanType);
      case "txn":
        String txnLabel = rest.startsWith("named.") ? rest.substring("named.".length()) : rest;
        return new Mapped("ebean.txn", tags("label", txnLabel));
      case "l2":
        return l2(rest);
      default:
        return new Mapped("ebean.other", tags("label", ebeanName));
    }
  }

  private static Mapped query(String kind, String label, String beanType) {
    if (beanType == null || beanType.isEmpty()) {
      return new Mapped("ebean.query", tags("kind", kind, "label", label));
    }
    return new Mapped("ebean.query", tags("kind", kind, "type", beanType, "label", label));
  }

  private static Mapped l2(String rest) {
    int dot = rest.indexOf('.');
    if (dot <= 0) {
      return new Mapped("ebean.l2", tags("op", rest));
    }
    String region = rest.substring(0, dot);
    String op = rest.substring(dot + 1);
    return new Mapped("ebean.l2", tags("op", op, "region", region));
  }

  /**
   * Build a canonical (sorted) {@code key:value,key2:value2} tag string from the given
   * key/value pairs, skipping null/empty values and sanitising the reserved
   * delimiter characters from values.
   */
  private static String tags(String... keyValues) {
    List<String> pairs = new ArrayList<>(keyValues.length / 2);
    for (int i = 0; i + 1 < keyValues.length; i += 2) {
      String value = keyValues[i + 1];
      if (value != null && !value.isEmpty()) {
        pairs.add(keyValues[i] + ':' + sanitize(value));
      }
    }
    Collections.sort(pairs);
    return String.join(",", pairs);
  }

  /**
   * Replace the reserved tag delimiter characters ({@code ,} and {@code :}) so they
   * cannot break the {@code key:value,key2:value2} encoding.
   */
  private static String sanitize(String value) {
    if (value.indexOf(',') < 0 && value.indexOf(':') < 0) {
      return value;
    }
    return value.replace(',', '_').replace(':', '_');
  }
}
