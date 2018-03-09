package io.ebeaninternal.server.profile;

class DQueryPlanMeta {

  private final Class<?> type;
  private final String label;
  private final String sql;

  DQueryPlanMeta(Class<?> type, String label, String sql) {
    this.type = type;
    this.label = label;
    this.sql = sql;
  }

  public Class<?> getType() {
    return type;
  }

  public String getLabel() {
    return label;
  }

  public String getSql() {
    return sql;
  }

  @Override
  public String toString() {
    return "type:" + type + " label:" + label;
  }
}
