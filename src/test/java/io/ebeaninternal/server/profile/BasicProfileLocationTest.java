package io.ebeaninternal.server.profile;

import io.ebean.meta.MetricType;
import io.ebeaninternal.metric.MetricFactory;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class BasicProfileLocationTest {

  @Test
  public void obtain() {

    DProfileLocation loc = new DTimedProfileLocation(12, "foo", MetricFactory.get().createTimedMetric(MetricType.TXN, "junk"));

    assertThat(loc.obtain()).endsWith(":12)");
    assertThat(loc.shortDescription()).isEqualTo("NativeMethodAccessorImpl.invoke0(Native Method:12)");
  }

  @Test
  public void basic_trimPackage() {

    BasicProfileLocation loc = new BasicProfileLocation("com.foo.Bar.all");
    assertThat(loc.obtain()).isEqualTo("com.foo.Bar.all");
    assertThat(loc.shortDescription()).isEqualTo("Bar.all");
  }

  @Test
  public void basic_trimSinglePackage() {

    BasicProfileLocation loc = new BasicProfileLocation("foo.Bar.all");
    assertThat(loc.obtain()).isEqualTo("foo.Bar.all");
    assertThat(loc.shortDescription()).isEqualTo("Bar.all");
  }
}
