package io.ebeaninternal.server.profile;

import io.ebean.ProfileLocation;

class DQueryPlanMeta {

  private final Class<?> type;
  private final String label;
  private final ProfileLocation profileLocation;
  private final String name;
  private final String sql;

  DQueryPlanMeta(Class<?> type, String label, ProfileLocation profileLocation, String sql) {
    this.type = type;
    this.label = label;
    this.profileLocation = profileLocation;
    this.sql = sql;
    String name = type.getSimpleName();
    if (label != null) {
      name += "_" + label;
    }
    this.name = name;
  }

  public Class<?> getType() {
    return type;
  }

  public String getName() {
    return name;
  }

  public String getLabel() {
    return label;
  }

  public ProfileLocation getProfileLocation() {
    return profileLocation;
  }

  public String getSql() {
    return sql;
  }

  @Override
  public String toString() {
    return "type:" + type + " label:" + label;
  }
}
