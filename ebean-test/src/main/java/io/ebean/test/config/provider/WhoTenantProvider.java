package io.ebean.test.config.provider;

import io.ebean.config.CurrentTenantProvider;
import io.ebean.test.UserContext;

final class WhoTenantProvider implements CurrentTenantProvider {

  @Override
  public Object currentId() {
    return UserContext.currentTenantId();
  }
}
