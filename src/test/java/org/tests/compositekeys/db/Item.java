package org.tests.compositekeys.db;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Version;

@Entity
public class Item {
  @Id
  private ItemKey key;

  private String description;

  private String units;

  private int type;

  private int region;

  @Embedded
  @AttributeOverride(name = "lastUpdated", column = @Column(name = "DATE_MODIFIED"))
  @AttributeOverride(name = "created", column = @Column(name = "DATE_CREATED"))
  @AttributeOverride(name = "updatedBy", column = @Column(name = "MODIFIED_BY"))
  @AttributeOverride(name = "createdBy", column = @Column(name = "CREATED_BY"))
  private AuditInfo auditInfo = new AuditInfo();

  @Version
  private Long version;

  @ManyToOne
  @JoinColumn(name = "customer", referencedColumnName = "customer", insertable = false, updatable = false)
  @JoinColumn(name = "type", referencedColumnName = "type", insertable = false, updatable = false)
  private Type eType;

  @ManyToOne
  @JoinColumn(name = "customer", referencedColumnName = "customer", insertable = false, updatable = false)
  @JoinColumn(name = "region", referencedColumnName = "type", insertable = false, updatable = false)
  private Region eRegion;

  public ItemKey getKey() {
    return key;
  }

  public void setKey(ItemKey key) {
    this.key = key;
  }

  public String getUnits() {
    return units;
  }

  public void setUnits(String units) {
    this.units = units;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public int getType() {
    return type;
  }

  public void setType(int type) {
    this.type = type;
  }

  public int getRegion() {
    return region;
  }

  public void setRegion(int region) {
    this.region = region;
  }

  public Long getVersion() {
    return version;
  }

  public Type getEType() {
    return eType;
  }

  public Region getERegion() {
    return eRegion;
  }

  public void setVersion(Long version) {
    this.version = version;
  }

  public void setEType(Type eType) {
    this.eType = eType;
  }

  public void setERegion(Region eRegion) {
    this.eRegion = eRegion;
  }

  public AuditInfo getAuditInfo() {
    return auditInfo;
  }
}
