package io.ebeaninternal.server.querydefn;

import io.ebeaninternal.api.BindHash;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class BindHashTest {

  @Test
  public void update_with_null() {

    BindHash hash = new BindHash();
    hash.update(1).update(null).update("hello");

    BindHash hash2 = new BindHash();
    hash2.update(1).update(null).update("hello");

    assertThat(hash).isEqualTo(hash2);
  }

  @Test
  public void notEqual() {

    BindHash hash = new BindHash();
    hash.update(1).update(null).update("hello");

    BindHash hash2 = new BindHash();
    hash2.update(1).update("hello");

    BindHash hash3 = new BindHash();
    hash2.update(1).update(null);

    assertThat(hash).isNotEqualTo(hash2);
    assertThat(hash).isNotEqualTo(hash3);
    assertThat(hash2).isNotEqualTo(hash3);
  }
}
