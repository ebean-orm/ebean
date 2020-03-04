package io.ebeaninternal.server.deploy.meta;

import io.ebean.annotation.Identity;
import io.ebean.annotation.IdentityGenerated;
import io.ebean.annotation.IdentityType;
import io.ebean.config.dbplatform.IdType;

public class DeployIdentityMode {

  public static DeployIdentityMode auto() {
    return new DeployIdentityMode(IdType.AUTO);
  }

  private IdType type;
  private IdentityGenerated generated;
  private int start;
  private int increment;
  private int cache;
  private String sequenceName;
  private boolean platformDefault;

  public DeployIdentityMode(Identity id) {
    this.type = idType(id.type());
    this.generated = id.generated();
    this.start = id.start();
    this.increment = id.increment();
    this.cache = id.cache();
    this.sequenceName = id.sequenceName();
  }

  private DeployIdentityMode(IdType type) {
    this.type = type;
    this.generated = IdentityGenerated.AUTO;
    this.sequenceName = "";
    this.start = 0;
    this.increment = 0;
    this.cache = 0;
  }

  public void setPlatformType(IdType type) {
    this.type = type;
    this.platformDefault = true;
  }

  public void setSequence(int initialValue, int allocationSize, String sequenceName) {
    this.start = initialValue;
    this.increment = allocationSize;
    this.sequenceName = sequenceName;
  }

  public void setSequenceGenerator(String genName) {
    if (sequenceName == null || sequenceName.isEmpty()) {
      sequenceName = genName;
    }
  }

  public int setSequenceBatchMode(boolean sequenceBatchMode) {
    if (sequenceBatchMode) {
      // Ebean batch fetching multiple sequence values
      increment = 1;
    } else if (increment == 0) {
      // Use JPA default of 50
      increment = 50;
    }
    return increment;
  }

  public void setIdType(IdType type) {
    this.type = type;
  }

  public void setStart(int start) {
    this.start = start;
  }

  public void setIncrement(int increment) {
    this.increment = increment;
  }

  public void setSequenceName(String sequenceName) {
    this.sequenceName = sequenceName;
  }

  public boolean isPlatformDefault() {
    return platformDefault;
  }

  public IdType getIdType() {
    return type;
  }

  public IdentityGenerated getGenerated() {
    return generated;
  }

  public int getStart() {
    return start;
  }

  public int getIncrement() {
    return increment;
  }

  public int getCache() {
    return cache;
  }

  public String getSequenceName() {
    return sequenceName;
  }

  public boolean isSequence() {
    return type == IdType.SEQUENCE;
  }

  public boolean isIdentity() {
    return type == IdType.IDENTITY;
  }

  public boolean isExternal() {
    return type == IdType.EXTERNAL;
  }

  public boolean isAuto() {
    return type == IdType.AUTO;
  }

  private static IdType idType(IdentityType type) {
    switch (type) {
      case AUTO:
        return IdType.AUTO;
      case SEQUENCE:
        return IdType.SEQUENCE;
      case IDENTITY:
        return IdType.IDENTITY;
      case APPLICATION:
        return IdType.EXTERNAL;
      default:
        throw new IllegalStateException("type " + type + " not expected?");
    }
  }
}
