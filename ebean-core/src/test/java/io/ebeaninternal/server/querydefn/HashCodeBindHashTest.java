package io.ebeaninternal.server.querydefn;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class HashCodeBindHashTest {

  @Test
  public void update_with_null() {

    HashCodeBindHash hash = new HashCodeBindHash();
    hash.update(1).update(null).update("hello");

    HashCodeBindHash hash2 = new HashCodeBindHash();
    hash2.update(1).update(null).update("hello");

    assertThat(hash).isEqualTo(hash2);
  }

  @Test
  public void notEqual() {

    HashCodeBindHash hash = new HashCodeBindHash();
    hash.update(1).update(null).update("hello");

    HashCodeBindHash hash2 = new HashCodeBindHash();
    hash2.update(1).update("hello");

    HashCodeBindHash hash3 = new HashCodeBindHash();
    hash2.update(1).update(null);

    assertThat(hash).isNotEqualTo(hash2);
    assertThat(hash).isNotEqualTo(hash3);
    assertThat(hash2).isNotEqualTo(hash3);
  }
}
