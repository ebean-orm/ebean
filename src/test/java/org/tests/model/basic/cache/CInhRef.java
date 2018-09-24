package org.tests.model.basic.cache;

import org.tests.model.basic.BasicDomain;

import io.ebean.annotation.Cache;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity
@Cache(enableQueryCache = true)
public class CInhRef extends BasicDomain {
  private static final long serialVersionUID = -4673953370819311120L;

  @ManyToOne(cascade = {})
  private CInhRoot ref;

  public CInhRoot getRef() {
    return ref;
  }

  public void setRef(CInhRoot ref) {
    this.ref = ref;
  }
}
