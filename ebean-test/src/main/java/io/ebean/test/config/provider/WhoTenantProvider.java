package io.ebean.test.config.provider;

import io.ebean.config.CurrentTenantProvider;
import io.ebean.test.UserContext;

class WhoTenantProvider implements CurrentTenantProvider{

  @Override
  public Object currentId() {
    return UserContext.currentTenantId();
  }
}
