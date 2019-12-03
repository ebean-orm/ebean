package io.ebeaninternal.server.profile;

import io.ebean.ProfileLocation;
import io.ebeaninternal.server.util.Md5;

class DQueryPlanMeta {

  private final Class<?> type;
  private final String label;
  private final ProfileLocation profileLocation;
  private final String name;
  private final String sql;
  private final String hash;

  DQueryPlanMeta(Class<?> type, String label, ProfileLocation profileLocation, String sql) {
    this.type = type;
    this.label = label;
    this.profileLocation = profileLocation;
    this.sql = sql;
    String name = "dto." + type.getSimpleName();
    if (label != null) {
      name += "_" + label;
    }
    this.name = name;
    this.hash = initHash();
  }

  private String initHash() {
    StringBuilder sb = new StringBuilder(sql).append("|").append(name);
    if (profileLocation != null) {
      sb.append("|").append(profileLocation.location());
    }
    return Md5.hash(sb.toString());
  }

  public Class<?> getType() {
    return type;
  }

  public String getHash() {
    return hash;
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

  public String getLocation() {
    return (profileLocation == null) ? null : profileLocation.location();
  }

  public String getSql() {
    return sql;
  }

  @Override
  public String toString() {
    return name;
  }
}
