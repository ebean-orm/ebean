package io.ebeaninternal.server.querydefn;

import io.ebeaninternal.api.BindValuesKey;
import io.ebeaninternal.server.deploy.BaseTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class BindValuesKeyTest extends BaseTest {

  @Test
  public void update_with_null() {

    BindValuesKey hash = new BindValuesKey(spiEbeanServer());
    hash.add(1).add(null).add("hello");

    BindValuesKey hash2 = new BindValuesKey(spiEbeanServer());
    hash2.add(1).add(null).add("hello");

    assertThat(hash).isEqualTo(hash2);
  }

  @Test
  public void notEqual() {

    BindValuesKey hash = new BindValuesKey(spiEbeanServer());
    hash.add(1).add(null).add("hello");

    BindValuesKey hash2 = new BindValuesKey(spiEbeanServer());
    hash2.add(1).add("hello");

    BindValuesKey hash3 = new BindValuesKey(spiEbeanServer());
    hash2.add(1).add(null);

    assertThat(hash).isNotEqualTo(hash2);
    assertThat(hash).isNotEqualTo(hash3);
    assertThat(hash2).isNotEqualTo(hash3);
  }
}
