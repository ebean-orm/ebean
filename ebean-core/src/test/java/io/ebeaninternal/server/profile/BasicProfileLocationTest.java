package io.ebeaninternal.server.profile;

import io.ebean.metric.MetricFactory;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class BasicProfileLocationTest {

  @Test
  public void obtain() {

    DProfileLocation loc = new DTimedProfileLocation(12, "foo", MetricFactory.get().createTimedMetric("junk"));

    assertThat(loc.obtain()).isTrue();
    assertThat(loc.fullLocation()).endsWith(":12)");
    if (System.getProperty("java.version").startsWith("1.8")) {
      assertThat(loc.location()).isEqualTo("sun.reflect.NativeMethodAccessorImpl.invoke0");
    } else {
      assertThat(loc.location()).isEqualTo("java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0");
    }
    assertThat(loc.label()).isEqualTo("NativeMethodAccessorImpl.invoke0");
  }

  @Test
  public void basic_trimPackage() {

    BasicProfileLocation loc = new BasicProfileLocation("com.foo.Bar.all");
    assertThat(loc.obtain()).isFalse();
    assertThat(loc.fullLocation()).isEqualTo("com.foo.Bar.all");
    assertThat(loc.location()).isEqualTo("com.foo.Bar.all");
    assertThat(loc.label()).isEqualTo("Bar.all");
  }

  @Test
  public void basic_trimSinglePackage() {

    BasicProfileLocation loc = new BasicProfileLocation("foo.Bar.all");
    assertThat(loc.obtain()).isFalse();
    assertThat(loc.fullLocation()).isEqualTo("foo.Bar.all");
    assertThat(loc.location()).isEqualTo("foo.Bar.all");
    assertThat(loc.label()).isEqualTo("Bar.all");
  }
}
