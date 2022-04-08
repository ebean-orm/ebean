package io.ebean.bean;

import io.ebean.common.BeanList;
import io.ebean.common.BeanMap;
import io.ebean.common.BeanSet;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

class ToStringBuilderTest {

  @Test
  void basic() {
    ToStringBuilder builder = new ToStringBuilder();
    builder.start(new Object());
    builder.end();
    assertThat(builder.toString()).isEqualTo("Object@0()");
  }

  @Test
  void fields() {
    ToStringBuilder builder = new ToStringBuilder();
    builder.start(new Object());
    builder.add("a", 1);
    builder.add("b", "B");
    builder.end();
    assertThat(builder.toString()).isEqualTo("Object@0(a:1, b:B)");
  }

  @Test
  void add_referenceBeanCollection_expect_nothingAdded() {
    ToStringBuilder builder = new ToStringBuilder();
    builder.add("ref", new BeanList<>(null));
    assertThat(builder.toString()).isEqualTo("");
  }

  @Test
  void addCollection_some() {
    ToStringBuilder builder = new ToStringBuilder();
    builder.addCollection( new BeanList<>(List.of("A","B")));
    assertThat(builder.toString()).isEqualTo("[A, B]");
  }

  @Test
  void addCollection_null() {
    ToStringBuilder builder = new ToStringBuilder();
    builder.addCollection(null);
    assertThat(builder.toString()).isEqualTo("[]");
  }

  @Test
  void addCollection_empty() {
    ToStringBuilder builder = new ToStringBuilder();
    builder.addCollection(Collections.emptyList());
    assertThat(builder.toString()).isEqualTo("[]");
  }

  @Test
  void flatBean() {
    Recurse instance0 = new Recurse(42, "java");
    assertThat(instance0.toString()).isEqualTo("Recurse@0(id:42, nm:java)");
  }

  @Test
  void recursive_expect_reference() {
    Recurse instance0 = new Recurse(42, "java");
    instance0.other = instance0;

    assertThat(instance0.toString()).isEqualTo("Recurse@0(id:42, nm:java, other:Recurse@0)");
  }

  @Test
  void notRecursive() {
    Recurse instance0 = new Recurse(42, "java");
    instance0.other = new Recurse(43, "jvm");
    assertThat(instance0.toString()).isEqualTo("Recurse@0(id:42, nm:java, other:Recurse@1(id:43, nm:jvm))");
  }

  @Test
  void beanList_null_empty() {
    assertThat(toStringFor(new BeanList<String>(null))).isEqualTo("[]");
    assertThat(toStringFor(new BeanList<String>(Collections.emptyList()))).isEqualTo("[]");
  }

  @Test
  void beanSet_null_empty() {
    assertThat(toStringFor(new BeanSet<String>(null))).isEqualTo("[]");
    assertThat(toStringFor(new BeanSet<String>(Collections.emptySet()))).isEqualTo("[]");
  }

  @Test
  void beanMap_null_empty() {
    assertThat(toStringFor(new BeanMap<String, String>(null))).isEqualTo("{}");
    assertThat(toStringFor(new BeanMap<String, String>(Collections.emptyMap()))).isEqualTo("{}");
  }

  @Test
  void beanList_some() {
    BeanList<Recurse> list = new BeanList<>(List.of(new Recurse(1, "a"), new Recurse(2, "b")));
    assertThat(toStringFor(list)).isEqualTo("[Recurse@1(id:1, nm:a), Recurse@2(id:2, nm:b)]");
  }

  @Test
  void beanSet_some() {
    BeanSet<Recurse> list = new BeanSet<>(new LinkedHashSet<>(List.of(new Recurse(1, "a"), new Recurse(2, "b"))));
    assertThat(toStringFor(list)).isEqualTo("[Recurse@1(id:1, nm:a), Recurse@2(id:2, nm:b)]");
  }

  @Test
  void beanMap_some() {
    Map<String, Recurse> under = new LinkedHashMap<>();
    under.put("a", new Recurse(1, "a"));
    under.put("b", new Recurse(2, "b"));
    BeanMap<String, Recurse> list = new BeanMap<>(under);
    assertThat(toStringFor(list)).isEqualTo("{a:Recurse@1(id:1, nm:a), b:Recurse@2(id:2, nm:b)}");
  }

  private String toStringFor(ToStringAware aware) {
    ToStringBuilder builder = new ToStringBuilder();
    aware.toString(builder);
    return builder.toString();
  }

  static class Recurse implements ToStringAware {

    final int id;
    final String nm;
    Recurse other;

    Recurse(int id, String nm) {
      this.id = id;
      this.nm = nm;
    }

    public String toString() {
      ToStringBuilder builder = new ToStringBuilder();
      toString(builder);
      return builder.toString();
    }

    @Override
    public void toString(ToStringBuilder builder) {
      builder.start(this);
      builder.add("id", id);
      builder.add("nm", nm);
      builder.add("other", other);
      builder.end();
    }
  }
}
