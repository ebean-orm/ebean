package io.ebeaninternal.server.expression;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

class RawExpressionBuilder {

  private static final String BP_1 = "?1";

  private static final String[] BP = {BP_1, "?2", "?3", "?4", "?5", "?6", "?7", "?8",
    "?9", "?10", "?11", "?12", "?13", "?14", "?15", "?16", "?17", "?18", "?19", "?20"};

  /**
   * Build RawExpression taking into account parameter expansion.
   */
  static RawExpression buildSingle(String raw, Object value) {
    if (isExpand(value, raw, BP_1)) {
      Collection val = (Collection) value;
      raw = raw.replace(BP_1, expand(val));
      return new RawExpression(raw, val.toArray());
    }
    return new RawExpression(raw, new Object[]{value});
  }

  /**
   * Build RawExpression for multiple bind values taking into account parameter expansion.
   */
  static RawExpression build(String raw, Object[] values) {

    for (int i = 0; i < values.length; i++) {
      if (isExpand(values[i], raw, match(i))) {
        return new RawExpressionBuilder(raw, values).build();
      }
    }
    return new RawExpression(raw, values);
  }

  /**
   * Return true if this value is a collection that should be expanded.
   */
  private static boolean isExpand(Object value, String raw, String bindMatch) {
    return value instanceof Collection && raw.contains(bindMatch);
  }

  private static String match(int i) {
    if (i < 20) {
      return BP[i];
    }
    return "?" + (i + 1);
  }

  private static String expand(Collection values) {

    StringBuilder sqlExpand = new StringBuilder(values.size() * 2);
    for (int i = 0; i < values.size(); i++) {
      if (i > 0) {
        sqlExpand.append(",");
      }
      sqlExpand.append("?");
    }
    return sqlExpand.toString();
  }

  private final String expanded;

  private final List<Object> params = new ArrayList<>();

  private RawExpressionBuilder(String raw, Object[] values) {
    for (int i = 0; i < values.length; i++) {
      String match = match(i);
      if (!isExpand(values[i], raw, match)) {
        params.add(values[i]);
      } else {
        Collection val = (Collection) values[i];
        params.addAll(val);
        raw = raw.replace(match, expand(val));
      }
    }
    this.expanded = raw;
  }

  private RawExpression build() {
    return new RawExpression(expanded, params.toArray());
  }
}
