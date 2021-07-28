package io.ebeaninternal.server.util;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ChecksumTest {

  @Test
  public void checksum() {

    final long val = Checksum.checksum("Hello world");
    assertThat(val).isEqualTo(413860925L);

    assertThat(Checksum.checksum("Hello world")).isEqualTo(val);
    assertThat(Checksum.checksum("hello world")).isNotEqualTo(val);
  }
}
