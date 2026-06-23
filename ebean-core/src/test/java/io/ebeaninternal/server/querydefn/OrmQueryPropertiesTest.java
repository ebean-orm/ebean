package io.ebeaninternal.server.querydefn;

import io.ebeaninternal.api.SpiExpressionList;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.LinkedHashSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertSame;

public class OrmQueryPropertiesTest {

  String append(String prefix, OrmQueryProperties p1) {
    StringBuilder sb = new StringBuilder();
    p1.asStringDebug(prefix, sb);
    return sb.toString();
  }

  @Test
  public void construct_with_propertySet_when_empty() {

    OrmQueryProperties p1 = new OrmQueryProperties(null, new LinkedHashSet<>());
    assertThat(p1.allProperties()).isFalse();
    assertThat(p1.getIncluded()).isEmpty();
  }

  @Test
  public void construct_with_propertySet_when_one() {

    LinkedHashSet<String> set = new LinkedHashSet<>();
    set.add("name");
    OrmQueryProperties p1 = new OrmQueryProperties(null, set);

    assertThat(p1.getIncluded()).containsOnly("name");
    assertThat(p1.allProperties()).isFalse();
  }

  @Test
  public void construct_with_propertySet_when_some() {

    LinkedHashSet<String> set = new LinkedHashSet<>();
    set.add("id");
    set.add("name");
    set.add("startDate");
    OrmQueryProperties p1 = new OrmQueryProperties(null, set);

    assertThat(p1.getIncluded()).containsOnly("id", "name", "startDate");
    assertThat(p1.allProperties()).isFalse();
  }

  @Test
  public void construct_with_propertySet_expect_defensiveCopy() {

    LinkedHashSet<String> set = new LinkedHashSet<>();
    set.add("name");
    OrmQueryProperties p1 = new OrmQueryProperties(null, set);
    set.add("status");

    assertThat(p1.getIncluded()).containsOnly("name");
  }

  @Test
  public void copy_expect_reuseIncludedSet() {

    OrmQueryProperties p1 = new OrmQueryProperties(null, "id,name");
    OrmQueryProperties p2 = p1.copy();

    assertSame(p1.getIncluded(), p2.getIncluded());
  }

  @Test
  public void append_when_empty() {

    OrmQueryProperties p1 = new OrmQueryProperties();
    assertThat(append("select ", p1)).isEqualTo("select ");
  }

  @Test
  public void append_when_someProperties() {

    OrmQueryProperties p1 = new OrmQueryProperties(null, "id,name");
    assertThat(append("select ", p1)).isEqualTo("select (id,name)");
  }

  @Test
  public void append_when_somePropertiesWithOptions() {

    OrmQueryProperties p1 = new OrmQueryProperties(null, "id,name,+cache");
    //FIXME: assertThat(append("select ", p1)).isEqualTo("select (id,name,+cache)");
  }

  @Test
  public void append_when_path_and_emptyProperties() {

    OrmQueryProperties p1 = new OrmQueryProperties("customer", "");
    assertThat(append("fetch ", p1)).isEqualTo("fetch customer ");
  }

  @Test
  public void append_when_path_and_somePropertiesWithOptions() {

    OrmQueryProperties p1 = new OrmQueryProperties("customer", "id,name,+cache");
    //FIXME: assertThat(append("fetch ", p1)).isEqualTo("fetch customer (id,name,+cache)");
  }

  @Test
  public void queryPlanHash_expect_stableAndIncludeDynamicParts() {

    OrmQueryProperties p1 = new OrmQueryProperties("customer", "id,name");

    String hash1 = queryPlanHash(p1);
    String hash2 = queryPlanHash(p1);
    assertThat(hash2).isEqualTo(hash1);

    p1.addSecondaryQueryJoin("id");
    String hash3 = queryPlanHash(p1);
    assertThat(hash3).isNotEqualTo(hash1);
    assertThat(hash3).contains("/s[id]");

    p1.setFilterMany(dummyFilterMany("f1"));
    String hash4 = queryPlanHash(p1);
    assertThat(hash4).contains("/f");
    assertThat(hash4).contains("f1");
  }

  private String queryPlanHash(OrmQueryProperties properties) {
    StringBuilder sb = new StringBuilder();
    properties.queryPlanHash(sb);
    return sb.toString();
  }

  private SpiExpressionList<?> dummyFilterMany(String marker) {
    return (SpiExpressionList<?>) Proxy.newProxyInstance(
      getClass().getClassLoader(),
      new Class<?>[]{SpiExpressionList.class},
      (proxy, method, args) -> {
        String methodName = method.getName();
        if ("queryPlanHash".equals(methodName)) {
          ((StringBuilder) args[0]).append(marker);
          return null;
        }
        Class<?> returnType = method.getReturnType();
        if (returnType == boolean.class) {
          return false;
        }
        if (returnType == int.class) {
          return 0;
        }
        if (returnType == long.class) {
          return 0L;
        }
        if (returnType == float.class) {
          return 0f;
        }
        if (returnType == double.class) {
          return 0d;
        }
        return null;
      });
  }

}
