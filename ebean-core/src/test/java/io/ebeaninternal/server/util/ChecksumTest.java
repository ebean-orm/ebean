package io.ebeaninternal.server.util;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ChecksumTest {

  @Test
  public void checksum() {
    final long val = Checksum.checksum("Hello world");
    assertThat(val).isEqualTo(2346098258L);
    assertThat(Checksum.checksum("Hello world")).isEqualTo(val);
    assertThat(Checksum.checksum("hello world")).isNotEqualTo(val);
  }

  @Test
  public void checksum_shortString() {
    final long val0 = Checksum.checksum("2012-01-11");
    final long val1 = Checksum.checksum("2012-10-02");
    assertThat(val0).isNotEqualTo(val1);
  }
}
