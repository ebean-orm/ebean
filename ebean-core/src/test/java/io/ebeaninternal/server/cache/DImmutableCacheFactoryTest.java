package io.ebeaninternal.server.cache;

import io.ebean.FetchGroup;
import io.ebeaninternal.api.SpiEbeanServer;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DImmutableCacheFactoryTest {

  @SuppressWarnings("unchecked")
  @Test
  void build_with_loading_registers_immutable_cache() {
    SpiEbeanServer server = mock(SpiEbeanServer.class);
    FetchGroup<FooRef> fetchGroup = mock(FetchGroup.class);

    new DImmutableCacheFactory().builder(FooRef.class)
      .loading(server, fetchGroup)
      .build();

    verify(server).registerImmutableCache(any());
  }

  @SuppressWarnings("unchecked")
  @Test
  void build_with_loader_after_loading_is_disconnected() {
    SpiEbeanServer server = mock(SpiEbeanServer.class);
    FetchGroup<FooRef> fetchGroup = mock(FetchGroup.class);

    new DImmutableCacheFactory().builder(FooRef.class)
      .loading(server, fetchGroup)
      .loader(ids -> Collections.emptyMap())
      .build();

    verify(server, never()).registerImmutableCache(any());
  }

  static final class FooRef {
  }
}
