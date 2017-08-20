package io.ebeaninternal.server.deploy;

import io.ebean.Platform;
import io.ebean.annotation.DdlMigration;
import io.ebean.annotation.DiscriminatorDdlMigration;
/**
 * Class to hold the DDL-migration information that is needed to do correct alters.
 * 
 * @author Roland Praml, FOCONIS AG
 */
public class DdlMigrationInfo {

  private final Platform[] platforms;
  private final String[] preDdl;
  private final String[] postDdl;
  private final String defaultValue;
  private final String since;

  public DdlMigrationInfo(DdlMigration ann) {
    this.platforms = ann.platforms();
    this.preDdl = getPreDdl(ann.preDdl(), ann.defaultValue());    
    this.postDdl = ann.postDdl();
    this.defaultValue = ann.defaultValue().equals("__UNSET__") ? null : ann.defaultValue();
    this.since = ann.since();
  }

  private String[] getPreDdl(String[] preDdl, String defaultValue) {
    if (preDdl.length == 1 && "${SET_DEFAULT}".equals(preDdl[0]) && defaultValue == null) {
      return null;
    } else {
      return preDdl;
    }
  }

  public DdlMigrationInfo(DiscriminatorDdlMigration ann) {
    this.platforms = ann.platforms();
    this.preDdl = getPreDdl(ann.preDdl(), ann.defaultValue());      
    this.postDdl = ann.postDdl();
    this.defaultValue = ann.defaultValue().equals("__UNSET__") ? null : ann.defaultValue();
    this.since = ann.since();
  }
  
  public Platform[] getPlatforms() {
    return platforms;
  }

  public String[] getPreDdl() {
    return preDdl;
  }

  public String[] getPostDdl() {
    return postDdl;
  }

  public String getDefaultValue() {
    return defaultValue;
  }

  public String getSince() {
    return since;
  }

  

}
