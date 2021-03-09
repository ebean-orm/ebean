package io.ebeaninternal.server.querydefn;

import org.junit.Test;

import java.util.LinkedHashSet;

import static org.assertj.core.api.Assertions.assertThat;

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

}
