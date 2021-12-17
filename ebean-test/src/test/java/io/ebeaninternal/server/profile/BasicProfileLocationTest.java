package io.ebeaninternal.server.profile;

import io.ebean.metric.MetricFactory;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BasicProfileLocationTest {

  @Test
  void obtain() {

    DProfileLocation loc = new DTimedProfileLocation(12, "foo", MetricFactory.get().createTimedMetric("junk"));

    String javaVersion = System.getProperty("java.version");
    assertThat(loc.obtain()).isTrue();
    if (javaVersion.startsWith("1.8")) {
      assertThat(loc.fullLocation()).endsWith("invoke0(Native Method:12)");
      assertThat(loc.location()).isEqualTo("sun.reflect.NativeMethodAccessorImpl.invoke0");
      assertThat(loc.label()).isEqualTo("NativeMethodAccessorImpl.invoke0");
    } else if (javaVersion.startsWith("18")){
      assertThat(loc.fullLocation()).endsWith("jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:104)");
      assertThat(loc.location()).isEqualTo("java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke");
      assertThat(loc.label()).isEqualTo("DirectMethodHandleAccessor.invoke");
    } else {
      assertThat(loc.fullLocation()).endsWith("jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method:12)");
      assertThat(loc.location()).isEqualTo("java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0");
      assertThat(loc.label()).isEqualTo("NativeMethodAccessorImpl.invoke0");
    }
  }

  @Test
  void basic_trimPackage() {

    BasicProfileLocation loc = new BasicProfileLocation("com.foo.Bar.all");
    assertThat(loc.obtain()).isFalse();
    assertThat(loc.fullLocation()).isEqualTo("com.foo.Bar.all");
    assertThat(loc.location()).isEqualTo("com.foo.Bar.all");
    assertThat(loc.label()).isEqualTo("Bar.all");
  }

  @Test
  void basic_trimSinglePackage() {

    BasicProfileLocation loc = new BasicProfileLocation("foo.Bar.all");
    assertThat(loc.obtain()).isFalse();
    assertThat(loc.fullLocation()).isEqualTo("foo.Bar.all");
    assertThat(loc.location()).isEqualTo("foo.Bar.all");
    assertThat(loc.label()).isEqualTo("Bar.all");
  }
}
