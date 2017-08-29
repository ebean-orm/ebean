package io.ebeaninternal.server.deploy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import io.ebean.Platform;
import io.ebean.annotation.DdlScript;
/**
 * Class to hold the DDL-migration information that is needed to do correct alters.
 * 
 * @author Roland Praml, FOCONIS AG
 */
public class MigrationDdlInfo {

  private final List<MigrationDdlScript> preAdd;
  private final List<MigrationDdlScript> postAdd;
  private final List<MigrationDdlScript> preAlter;
  private final List<MigrationDdlScript> postAlter;  
  
  public MigrationDdlInfo(DdlScript[] preAdd, DdlScript[] postAdd, DdlScript[] preAlter, DdlScript[] postAlter) {
    this.preAdd = toList(preAdd);
    this.postAdd = toList(postAdd);
    this.preAlter = toList(preAlter);
    this.postAlter = toList(postAlter);
  }

  private List<MigrationDdlScript> toList(DdlScript[] scripts) {
    if (scripts.length == 0) {
      return Collections.emptyList();
    }
    List<MigrationDdlScript> ret = new ArrayList<>(scripts.length);
    for (DdlScript script : scripts) {
      ret.add(new MigrationDdlScript(script.value(), script.platforms()));
    }
    return Collections.unmodifiableList(ret);
  }
  
  public List<MigrationDdlScript> getPreAdd() {
    return preAdd;
  }
  public List<MigrationDdlScript> getPostAdd() {
    return postAdd;
  }
  public List<MigrationDdlScript> getPreAlter() {
    return preAlter;
  }
  public List<MigrationDdlScript> getPostAlter() {
    return postAlter;
  }
//
//  public DdlMigrationInfo(DdlMigration ann) {
//    this.platforms = Arrays.asList(ann.platforms());
//    this.preDdl = Arrays.asList(ann.preDdl());    
//    this.postDdl = Arrays.asList(ann.postDdl());
//    this.defaultValue = ann.defaultValue().equals("__UNSET__") ? null : ann.defaultValue();
//  }
//
//  public DdlMigrationInfo(DiscriminatorDdlMigration ann) {
//    this.platforms = Arrays.asList(ann.platforms());
//    this.preDdl = Arrays.asList(ann.preDdl());      
//    this.postDdl = Arrays.asList(ann.postDdl());
//    this.defaultValue = ann.defaultValue().equals("__UNSET__") ? null : ann.defaultValue();
//  }
//  
//  public DeployDdlInfo(List<Platform> platforms, List<String> preDdl, List<String> postDdl, String defaultValue) {
//    this.platforms = platforms;
//    this.preDdl = preDdl;
//    this.postDdl = postDdl;
//    this.defaultValue = defaultValue;
//  }
//
//  public List<Platform> getPlatforms() {
//    return platforms;
//  }
//
//  public List<String> getPreDdl() {
//    return preDdl;
//  }
//
//  public List<String> getPostDdl() {
//    return postDdl;
//  }
//
//  public String getDefaultValue() {
//    return defaultValue;
//  }
//  
//  @Override
//  public int hashCode() {
//    return Objects.hash(defaultValue, platforms, preDdl, postDdl);
//  }
//  
//  @Override
//  public boolean equals(Object obj) {
//    if (obj == this) {
//      return true;
//    }
//    if (!(obj instanceof DeployDdlInfo)) {
//      return false;
//    }
//    DeployDdlInfo other = (DeployDdlInfo) obj;
//    return Objects.equals(defaultValue, other.defaultValue)
//        && Objects.equals(platforms, other.platforms)
//        && Objects.equals(preDdl, other.preDdl)
//        && Objects.equals(postDdl, other.postDdl);
//  }
//  
//  public String joinPlatforms() {
//    if (platforms.isEmpty()) {
//      return null;
//    } else {
//      StringBuilder sb = new StringBuilder();
//      for (Platform p : platforms) {
//        if (sb.length() > 0) {
//          sb.append(',');
//        }
//        sb.append(p.name());
//      }
//      return sb.toString();
//    }
//  }
}
