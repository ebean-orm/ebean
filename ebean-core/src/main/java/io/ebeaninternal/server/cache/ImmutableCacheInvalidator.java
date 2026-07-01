package io.ebeaninternal.server.cache;

import java.util.Collection;

public interface ImmutableCacheInvalidator {

  void clear();

  void removeAll(Collection<Object> ids);
}
