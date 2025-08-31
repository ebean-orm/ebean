package org.tests.model.m2o;

import jakarta.persistence.*;

@Entity
public class MTJTrans {

  @Id
  Long id;

  @Column(name = "org_id")
  Long orgId;

  @ManyToOne
  @JoinColumns({
    @JoinColumn(name = "org_id", referencedColumnName = "org_id", insertable = false, updatable = false),
    @JoinColumn(name = "order_id", referencedColumnName = "id")
  })
  MTJOrder order;

  public Long id() {
    return id;
  }

  public MTJTrans setId(Long id) {
    this.id = id;
    return this;
  }

  public Long orgId() {
    return orgId;
  }

  public MTJTrans setOrgId(Long orgId) {
    this.orgId = orgId;
    return this;
  }

  public MTJOrder order() {
    return order;
  }

  public MTJTrans setOrder(MTJOrder order) {
    this.order = order;
    return this;
  }
}
