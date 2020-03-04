package io.ebeaninternal.server.deploy;

import io.ebean.annotation.IdentityGenerated;
import io.ebean.config.dbplatform.IdType;
import io.ebeaninternal.server.deploy.meta.DeployIdentityMode;

public class IdentityMode {

  public static IdentityMode NONE = new IdentityMode();

  private final IdType type;
  private final IdentityGenerated generated;
  private final int start;
  private final int increment;
  private final int cache;
  private final String sequenceName;
  private final boolean platformDefault;

  /**
   * Create from deployment.
   */
  public IdentityMode(DeployIdentityMode deploy) {
    this.type = deploy.getIdType();
    this.generated = deploy.getGenerated();
    this.start = deploy.getStart();
    this.increment = deploy.getIncrement();
    this.cache = deploy.getCache();
    this.sequenceName = deploy.getSequenceName();
    this.platformDefault = deploy.isPlatformDefault();
  }

  /**
   * Create from migration model CreateTable.
   */
  public IdentityMode(IdType type, IdentityGenerated auto, int start, int increment, int cache, String seqName) {
    this.type = type;
    this.generated = auto;
    this.start = start;
    this.increment = increment;
    this.cache = cache;
    this.sequenceName = seqName;
    this.platformDefault = false;
  }

  /**
   * NONE constructor.
   */
  private IdentityMode() {
    this.type = null;
    this.generated = IdentityGenerated.AUTO;
    this.start = 0;
    this.increment = 0;
    this.cache = 0;
    this.sequenceName = "";
    this.platformDefault = false;
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

  public boolean hasOptions() {
    return start > 0 || cache > 0 || increment > 0;
  }
}
