package io.ebean.dbmigration.migration;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import io.ebean.Platform;
import io.ebean.util.StringHelper;
/**
 * Represents the MigrationInfo changeset.
 * 
 * @author Roland Praml, FOCONIS AG
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "preDdl",
    "postDdl"
  })
@XmlRootElement(name = "migrationInfo")
public class MigrationInfo {

  @XmlAttribute(name="platforms")
  private String platforms;
 
  @XmlAttribute(name="default")
  private String defaultValue;
  
  @XmlElement
  private List<String> preDdl;

  @XmlElement
  private List<String> postDdl;


  public void setPlatforms(String platforms) {
    this.platforms = platforms;
  }
  
  public String getPlatforms() {
    return platforms;
  }
 
  public List<String> getPreDdl() {
    if (preDdl == null) {
      preDdl = new ArrayList<>();
    }
    return preDdl;
  }
  
  public List<String> getPostDdl() {
    if (postDdl == null) {
      postDdl = new ArrayList<>();
    }
    return postDdl;
  }

  public void setDefaultValue(String defaultValue) {
    this.defaultValue = defaultValue;
  }
  
  public String getDefaultValue() {
    return defaultValue;
  }
  
}
