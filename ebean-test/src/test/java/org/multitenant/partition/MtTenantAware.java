package org.multitenant.partition;

import io.ebean.annotation.TenantId;

import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class MtTenantAware extends MtBaseDomain {

  @TenantId
  @ManyToOne(optional = false)
  MtTenant tenant;

  public MtTenant getTenant() {
    return tenant;
  }

  public void setTenant(MtTenant tenant) {
    this.tenant = tenant;
  }
}
