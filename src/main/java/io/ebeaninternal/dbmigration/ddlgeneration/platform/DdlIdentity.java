package io.ebeaninternal.dbmigration.ddlgeneration.platform;

import io.ebean.annotation.IdentityGenerated;
import io.ebean.config.dbplatform.IdType;
import io.ebeaninternal.server.deploy.IdentityMode;

public class DdlIdentity {

  public static final DdlIdentity NONE = new DdlIdentity();

  private final IdType idType;
  private final IdentityMode identityMode;

  public DdlIdentity(IdType idType, IdentityMode identityMode) {
    this.idType = idType;
    this.identityMode = identityMode;
  }

  private DdlIdentity() {
    this.idType = null;
    this.identityMode = null;
  }

  public boolean useSequence() {
    return idType == IdType.SEQUENCE;
  }

  public boolean useIdentity() {
    return idType == IdType.IDENTITY;
  }

  private String generatedBy() {
    return isAlways() ? "always" : "by default";
  }

  private boolean isAlways() {
    return IdentityGenerated.ALWAYS == identityMode.getGenerated();
  }

  public String optionGenerated() {
    return " generated " + generatedBy() +" as identity";
  }

  public String options(String startWith, String incrementBy, String cache) {
    if (!identityMode.hasOptions()) {
      return "";
    }
    StringBuilder sb = new StringBuilder(40);
    sb.append(" (");
    optionFor(sb, startWith, identityMode.getStart());
    optionFor(sb, incrementBy, identityMode.getIncrement());
    optionFor(sb, cache, identityMode.getCache());
    sb.append(")");
    return sb.toString();
  }

  private void optionFor(StringBuilder sb, String prefix, int val) {
    if (val > 0 && prefix != null) {
      if (sb.length() > 2) {
        sb.append(" ");
      }
      sb.append(prefix).append(" ").append(val);
    }
  }

}
