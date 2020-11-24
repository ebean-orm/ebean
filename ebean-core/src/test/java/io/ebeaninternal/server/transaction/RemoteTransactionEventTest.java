package io.ebeaninternal.server.transaction;

import io.ebeaninternal.server.cache.RemoteCacheEvent;
import org.junit.Test;

import java.util.Collections;
import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;

public class RemoteTransactionEventTest {

  @Test
  public void isEmpty_when_remoteCacheEvent_expect_false() {

    RemoteTransactionEvent event = new RemoteTransactionEvent("db");
    RemoteCacheEvent remote = new RemoteCacheEvent(true);
    event.addRemoteCacheEvent(remote);

    assertThat(event.isEmpty()).isFalse();
  }

  @Test
  public void isEmpty_when_remoteTableMod_expect_false() {

    RemoteTransactionEvent event = new RemoteTransactionEvent("db");
    RemoteTableMod remote = new RemoteTableMod(new HashSet<>(Collections.singletonList("junk")));
    event.addRemoteTableMod(remote);

    assertThat(event.isEmpty()).isFalse();
  }
}
