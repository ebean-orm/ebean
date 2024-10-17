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
  void max100Beans_expect_noMoreContentAfterMax100() {

    String loop100 = addLoopForMaxBeans(100);
    String loop101 = addLoopForMaxBeans(101);
    String loop900 = addLoopForMaxBeans(900);

    assertThat(loop100).isEqualTo(loop101);
    assertThat(loop101).isEqualTo(loop900);
  }

  private String addLoopForMaxBeans(int loopMax) {
    ToStringBuilder builder = new ToStringBuilder();
    builder.start(new Object());
    for (int i = 0; i <= loopMax; i++) {
      addForMax(builder, i);
    }
    builder.end();
    return builder.toString();
  }

  private void addForMax(ToStringBuilder builder, int i) {
    builder.add("c", new B(i));
  }

  @Test
  void maxContent_expect_noMoreContentAfterMax2000() {
    String loop23 = addContentLoop(23);
    String loop24 = addContentLoop(24);
    String loop25 = addContentLoop(25);

    assertThat(loop23).isEqualTo(loop24);
    assertThat(loop23).isEqualTo(loop25);
  }

  private String addContentLoop(int loopMax) {
    ToStringBuilder builder = new ToStringBuilder();
    builder.start(new Object());
    for (int i = 0; i <= loopMax; i++) {
      addForMaxContent(builder, i);
    }
    builder.end();
    return builder.toString();
  }

  private void addForMaxContent(ToStringBuilder builder, int i) {
    builder.add("someContentThatAddsUp", new Recurse(i, "SomeContentThatAddsUp_SomeContentThatAddsUp!!"));
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

  @Test
  void map_recurse() {
    final ParamStore paramStore = new ParamStore(1);

    paramStore.map.put("aaa", new ParamValue(1, paramStore));
    paramStore.map.put("bbb", new ParamValue(2, paramStore));

    assertThat(toStringFor(paramStore))
      .isEqualTo("ParamStore@0(id:1, map:{aaa:ParamValue@1(id:1, pm:ParamStore@0), bbb:ParamValue@2(id:2, pm:ParamStore@0)})");
  }

  private String toStringFor(ToStringAware aware) {
    ToStringBuilder builder = new ToStringBuilder();
    aware.toString(builder);
    return builder.toString();
  }

  static final class ParamValue implements ToStringAware {

    final int id;

    final ParamStore parentModel;

    ParamValue(final int id, final ParamStore parentModel) {
      this.id = id;
      this.parentModel = parentModel;
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
      builder.add("pm", parentModel);
      builder.end();
    }
  }

  static final class ParamStore implements ToStringAware {

    final int id;

    final Map<String, ParamValue> map = new LinkedHashMap<>();


    ParamStore(final int id) {
      this.id = id;
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
      builder.add("map", map);
      builder.end();
    }
  }


  static final class Recurse implements ToStringAware {

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

  static final class B implements ToStringAware {

    final int id;

    B(int id) {
      this.id = id;
    }

    public String toString() {
      ToStringBuilder builder = new ToStringBuilder();
      toString(builder);
      return builder.toString();
    }

    @Override
    public void toString(ToStringBuilder builder) {
      builder.start(this);
      builder.add("b", id);
      builder.end();
    }
  }
}
