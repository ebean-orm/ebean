package io.ebean.dbmigration.migration;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import io.ebean.Platform;
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
  
  @XmlAttribute(name="since")
  private String since;
  
  @XmlAttribute(name="default")
  private String defaultValue;
  
  @XmlElement
  private List<String> preDdl;

  @XmlElement
  private List<String> postDdl;


  public void setPlatforms(Platform[] platforms) {
    if (platforms == null || platforms.length == 0) {
      this.platforms = null;
    } else {
      StringBuilder sb = new StringBuilder();
      for (Platform p : platforms) {
        if (sb.length() > 0) {
          sb.append(',');
        }
        sb.append(p.name());
      }
      this.platforms = sb.toString();
    }
  }

  public void setSince(String since) {
    this.since = since;
  }
  
  public String getSince() {
    return since;
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
}
