package io.ebean.test.config.provider;

import io.ebean.config.CurrentUserProvider;
import io.ebean.test.UserContext;

class WhoUserProvider implements CurrentUserProvider {

  @Override
  public Object currentUser() {
    return UserContext.currentUserId();
  }
}
