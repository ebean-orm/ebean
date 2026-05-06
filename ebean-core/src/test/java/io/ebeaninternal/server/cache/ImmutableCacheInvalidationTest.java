package io.ebeaninternal.server.cache;

import io.ebean.cache.ServerCacheNotification;
import io.ebean.cache.QueryCacheEntry;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.transaction.TableModState;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ImmutableCacheInvalidationTest {

  @Test
  void cacheChangeSet_remove_id_invalidates_descriptor_immutable_cache() {
    BeanDescriptor<FooRef> descriptor = descriptor(FooRef.class);
    CacheChangeSet changeSet = new CacheChangeSet();
    changeSet.addImmutableRemove(descriptor, 42);
    changeSet.apply();
    verify(descriptor).removeImmutableCacheByIds(Set.of(42));
  }

  @Test
  void cacheChangeSet_clear_invalidates_descriptor_immutable_cache() {
    BeanDescriptor<BarRef> descriptor = descriptor(BarRef.class);
    CacheChangeSet changeSet = new CacheChangeSet();
    changeSet.addImmutableClear(descriptor);
    changeSet.apply();
    verify(descriptor).clearImmutableCaches();
  }

  @Test
  void table_notify_does_not_clear_descriptor_immutable_cache() {
    TableModState tableModState = new TableModState();
    tableModState.notify(new ServerCacheNotification(Set.of("t_foo")));

    QueryCacheEntry entry = mock(QueryCacheEntry.class);
    when(entry.dependentTables()).thenReturn(Set.of("t_foo"));
    when(entry.timestamp()).thenReturn(Instant.now().minusSeconds(1));

    assertThat(tableModState.isValid(entry)).isFalse();
  }

  private static <T> BeanDescriptor<T> descriptor(Class<T> type) {
    BeanDescriptor<T> descriptor = mock(BeanDescriptor.class);
    when(descriptor.type()).thenReturn(type);
    return descriptor;
  }

  static final class FooRef {
  }

  static final class BarRef {
  }
}
