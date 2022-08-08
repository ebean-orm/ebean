package io.ebeaninternal.server.profile;

import io.ebean.meta.BasicMetricVisitor;
import io.ebean.meta.MetaTimedMetric;
import io.ebean.metric.MetricFactory;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

class BasicProfileLocationTest {

  Function<String,String> naming = (String name) -> "prefix[" + name.replace('.', '-') + "]";

  @Test
  void metricNameFromOverride() {
    DTimedProfileLocation loc = new DTimedProfileLocation(12, "", MetricFactory.get().createTimedMetric("a.b.c"));
    loc.initWith("foo.label");
    loc.add(42);

    BasicMetricVisitor visitor = new BasicMetricVisitor("v", naming);
    loc.visit(visitor);

    List<MetaTimedMetric> result = visitor.timedMetrics();
    assertThat(result).hasSize(1);
    assertThat(result.get(0).name()).isEqualTo("prefix[txn-named-foo-label]");
    assertThat(result.get(0).total()).isEqualTo(42);

    loc.add(21);
    BasicMetricVisitor visitor2 = new BasicMetricVisitor("v", naming);
    loc.visit(visitor2);
    List<MetaTimedMetric> result2 = visitor2.timedMetrics();
    assertThat(result2).hasSize(1);
    assertThat(result2.get(0).name()).isEqualTo("prefix[txn-named-foo-label]");
    assertThat(result2.get(0).total()).isEqualTo(21);
  }

  @Test
  void metricNameFromTimed() {
    DTimedProfileLocation loc = new DTimedProfileLocation(12, "foo", MetricFactory.get().createTimedMetric("a.b.c"));
    loc.add(42);

    BasicMetricVisitor visitor = new BasicMetricVisitor("v", naming);
    loc.visit(visitor);

    List<MetaTimedMetric> result = visitor.timedMetrics();
    assertThat(result).hasSize(1);
    assertThat(result.get(0).name()).isEqualTo("prefix[a-b-c]");
  }

  @Test
  void obtain() {
    DProfileLocation loc = new DTimedProfileLocation(12, "foo", MetricFactory.get().createTimedMetric("junk"));

    String javaVersion = System.getProperty("java.version");
    assertThat(loc.obtain()).isTrue();
    if (javaVersion.startsWith("1.8")) {
      assertThat(loc.fullLocation()).endsWith("invoke0(Native Method:12)");
      assertThat(loc.location()).isEqualTo("sun.reflect.NativeMethodAccessorImpl.invoke0");
      assertThat(loc.label()).isEqualTo("NativeMethodAccessorImpl.invoke0");
    } else if (javaVersion.startsWith("18") || javaVersion.startsWith("19") || javaVersion.startsWith("20")){
      assertThat(loc.fullLocation()).endsWith("jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:104)");
      assertThat(loc.location()).isEqualTo("java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke");
      assertThat(loc.label()).isEqualTo("DirectMethodHandleAccessor.invoke");
    } else if (javaVersion.startsWith("11") || javaVersion.startsWith("17")) {
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
