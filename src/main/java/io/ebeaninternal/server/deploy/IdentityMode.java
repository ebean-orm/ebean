package io.ebeaninternal.server.deploy;

import io.ebean.annotation.IdentityGenerated;
import io.ebean.annotation.IdentityType;
import io.ebean.config.dbplatform.IdType;

public class IdentityMode {

  public static IdentityMode auto() {
    return new IdentityMode(IdType.AUTO);
  }

  public static IdentityMode none() {
    return new IdentityMode(null);
  }

  public static IdType idType(IdentityType type) {
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

  private IdType type;
  private IdentityGenerated generated;
  private int start;
  private int increment;
  private int cache;
  private String sequenceName;
  private boolean platformDefault;

  public IdentityMode(IdType type, IdentityGenerated generated, int start, int increment, String seqName) {
    this.type = type;
    this.generated = generated;
    this.start = start;
    this.increment = increment;
    this.sequenceName = seqName;
  }

  /**
   * Create from <code>@SequenceGenerator</code> annotation.
   */
  public IdentityMode(int initialValue, int allocationSize, String sequenceName) {
    this.type = IdType.AUTO;
    this.generated = IdentityGenerated.AUTO;
    this.sequenceName = sequenceName;
    this.start = initialValue;
    this.increment = allocationSize;
    this.cache = 0;
  }

  private IdentityMode(IdType type) {
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

  public void setSequenceBatchMode() {
    this.increment = 1;
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

  public boolean isDatabaseIdentity() {
    return type != IdType.EXTERNAL && type != IdType.GENERATOR;
  }

}
