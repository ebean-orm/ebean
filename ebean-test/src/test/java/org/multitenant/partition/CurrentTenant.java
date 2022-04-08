package org.multitenant.partition;

import io.ebean.config.CurrentTenantProvider;

import java.util.concurrent.atomic.AtomicInteger;

class CurrentTenant implements CurrentTenantProvider {

  static AtomicInteger callCounter = new AtomicInteger();

  static int count() {
    return callCounter.get();
  }

  /**
   * Return the current tenantId from the user context.
   */
  @Override
  public String currentId() {
    callCounter.incrementAndGet();
    return UserContext.get().getTenantId();
  }
}
