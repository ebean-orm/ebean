package io.ebeaninternal.server.deploy;

import io.ebean.Platform;

/**
 * A Migration ddl script.
 * @author Roland Praml, FOCONIS AG
 *
 */
public class MigrationDdlScript {
  private final String value;
  
  private final Platform[] platforms;

  public MigrationDdlScript(String value) {
    this (value, new Platform[0]);
  }
  
  public MigrationDdlScript(String value, Platform[] platforms) {
    super();
    this.value = value;
    this.platforms = platforms;
  }

  public String getValue() {
    return value;
  }
  
  public Platform[] getPlatforms() {
    return platforms;
  }
  
  public String joinPlatforms() {
    if (platforms.length == 0) {
      return null;
    } else {
      StringBuilder sb = new StringBuilder();
      for (Platform p : platforms) {
        if (sb.length() > 0) {
          sb.append(',');
        }
        sb.append(p.name());
      }
      return sb.toString();
    }
  }
}
