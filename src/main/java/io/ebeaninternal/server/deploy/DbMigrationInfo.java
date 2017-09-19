package io.ebeaninternal.server.deploy;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import io.ebean.annotation.Platform;
/**
 * Class to hold the DDL-migration information that is needed to do correct alters.
 *
 * @author Roland Praml, FOCONIS AG
 */
public class DbMigrationInfo {

  private final List<String> preAdd;
  private final List<String> postAdd;
  private final List<String> preAlter;
  private final List<String> postAlter;
  private final List<Platform> platforms;

  public DbMigrationInfo(String[] preAdd, String[] postAdd, String[] preAlter, String[] postAlter, Platform[] platforms) {
    this.preAdd = toList(preAdd);
    this.postAdd = toList(postAdd);
    this.preAlter = toList(preAlter);
    this.postAlter = toList(postAlter);
    this.platforms = toList(platforms);
  }

  private <T> List<T> toList(T[] scripts) {
    if (scripts.length == 0) {
      return Collections.emptyList();
    } else {
      return Collections.unmodifiableList(Arrays.asList(scripts));
    }
  }

  public List<String> getPreAdd() {
    return preAdd;
  }
  public List<String> getPostAdd() {
    return postAdd;
  }
  public List<String> getPreAlter() {
    return preAlter;
  }
  public List<String> getPostAlter() {
    return postAlter;
  }
  public List<Platform> getPlatforms() {
    return platforms;
  }

  public String joinPlatforms() {
    if (platforms.isEmpty()) {
      return null;
    } else {
      StringBuilder sb = new StringBuilder();
      for (Platform p : platforms) {
        if (sb.length() > 0) {
          sb.append(',');
        }
        sb.append(p.name().toLowerCase());
      }
      return sb.toString();
    }
  }


}
