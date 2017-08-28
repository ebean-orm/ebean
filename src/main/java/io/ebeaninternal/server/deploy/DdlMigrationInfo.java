package io.ebeaninternal.server.deploy;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import io.ebean.Platform;
import io.ebean.annotation.DdlMigration;
import io.ebean.annotation.DiscriminatorDdlMigration;
/**
 * Class to hold the DDL-migration information that is needed to do correct alters.
 * 
 * @author Roland Praml, FOCONIS AG
 */
public class DdlMigrationInfo {

  private final List<Platform> platforms;
  private final List<String> preDdl;
  private final List<String> postDdl;
  private final String defaultValue;

  public DdlMigrationInfo(DdlMigration ann) {
    this.platforms = Arrays.asList(ann.platforms());
    this.preDdl = Arrays.asList(ann.preDdl());    
    this.postDdl = Arrays.asList(ann.postDdl());
    this.defaultValue = ann.defaultValue().equals("__UNSET__") ? null : ann.defaultValue();
  }

  public DdlMigrationInfo(DiscriminatorDdlMigration ann) {
    this.platforms = Arrays.asList(ann.platforms());
    this.preDdl = Arrays.asList(ann.preDdl());      
    this.postDdl = Arrays.asList(ann.postDdl());
    this.defaultValue = ann.defaultValue().equals("__UNSET__") ? null : ann.defaultValue();
  }
  
  public DdlMigrationInfo(List<Platform> platforms, List<String> preDdl, List<String> postDdl, String defaultValue) {
    this.platforms = platforms;
    this.preDdl = preDdl;
    this.postDdl = postDdl;
    this.defaultValue = defaultValue;
  }

  public List<Platform> getPlatforms() {
    return platforms;
  }

  public List<String> getPreDdl() {
    return preDdl;
  }

  public List<String> getPostDdl() {
    return postDdl;
  }

  public String getDefaultValue() {
    return defaultValue;
  }
  
  @Override
  public int hashCode() {
    return Objects.hash(defaultValue, platforms, preDdl, postDdl);
  }
  
  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (!(obj instanceof DdlMigrationInfo)) {
      return false;
    }
    DdlMigrationInfo other = (DdlMigrationInfo) obj;
    return Objects.equals(defaultValue, other.defaultValue)
        && Objects.equals(platforms, other.platforms)
        && Objects.equals(preDdl, other.preDdl)
        && Objects.equals(postDdl, other.postDdl);
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
        sb.append(p.name());
      }
      return sb.toString();
    }
  }
}
