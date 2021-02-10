package io.ebeaninternal.server.querydefn;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class OrmQueryPropertiesParserTest {

  @Test
  public void when_null() {

    OrmQueryPropertiesParser.Response res = OrmQueryPropertiesParser.parse(null);
    assertAllDefaults(res);
    assertThat(res.properties).isEqualTo("");
  }

  @Test
  public void when_empty() {

    OrmQueryPropertiesParser.Response res = OrmQueryPropertiesParser.parse("");
    assertAllDefaults(res);
    assertThat(res.properties).isEqualTo("");
  }

  @Test
  public void when_hasStar() {

    OrmQueryPropertiesParser.Response res = OrmQueryPropertiesParser.parse("*");
    assertAllDefaults(res);
    assertThat(res.properties).isEqualTo("*");
  }

  @Test
  public void when_hasCache() {

    OrmQueryPropertiesParser.Response res = OrmQueryPropertiesParser.parse("+cache");
    assertThat(res.cache).isTrue();
    assertThat(res.included).isNull();
  }

  @Test
  public void when_hasCache_first() {

    OrmQueryPropertiesParser.Response res = OrmQueryPropertiesParser.parse("+cache,id");
    assertThat(res.cache).isTrue();
    assertThat(res.included).containsExactly("id");
  }

  @Test
  public void when_hasCache_last() {

    OrmQueryPropertiesParser.Response res = OrmQueryPropertiesParser.parse("name,+cache");
    assertThat(res.cache).isTrue();
    assertThat(res.included).containsExactly("name");
  }

  @Test
  public void when_hasCache_middle() {

    OrmQueryPropertiesParser.Response res = OrmQueryPropertiesParser.parse("name,+cache, id");
    assertThat(res.cache).isTrue();
    assertThat(res.included).containsExactly("name", "id");
  }

  @Test
  public void when_hasReadOnly() {

    OrmQueryPropertiesParser.Response res = OrmQueryPropertiesParser.parse("+readonly");
    assertThat(res.readOnly).isTrue();
    assertThat(res.included).isNull();
  }

  @Test
  public void when_hasLazy() {

    OrmQueryPropertiesParser.Response res = OrmQueryPropertiesParser.parse("+lazy");
    assertThat(res.fetchConfig.getLazyBatchSize()).isEqualTo(0);
    assertThat(res.included).isNull();
  }

  @Test
  public void when_hasLazyValue() {

    OrmQueryPropertiesParser.Response res = OrmQueryPropertiesParser.parse("+lazy(20)");
    assertThat(res.fetchConfig.getLazyBatchSize()).isEqualTo(20);
    assertThat(res.included).isNull();
  }

  @Test
  public void when_hasLazyValue_last() {

    OrmQueryPropertiesParser.Response res = OrmQueryPropertiesParser.parse("name,+lazy(20)");
    assertThat(res.fetchConfig.getLazyBatchSize()).isEqualTo(20);
    assertThat(res.included).containsExactly("name");
  }

  @Test
  public void when_hasLazyValue_first() {

    OrmQueryPropertiesParser.Response res = OrmQueryPropertiesParser.parse("+lazy(20),id,name");
    assertThat(res.fetchConfig.getLazyBatchSize()).isEqualTo(20);
    assertThat(res.included).containsExactly("id", "name");
  }

  @Test
  public void when_allProperties() {

    OrmQueryPropertiesParser.Response res = OrmQueryPropertiesParser.parse("+query(4),+lazy(5)");
    assertThat(res.fetchConfig.getLazyBatchSize()).isEqualTo(5);
    assertThat(res.included).isNull();
  }

  @Test
  public void when_everything_set() {

    OrmQueryPropertiesParser.Response res = OrmQueryPropertiesParser.parse("id, name, +readonly ,+lazy(20), +query(30) ,+cache");
    assertThat(res.included).containsExactly("id", "name");
    assertThat(res.fetchConfig.getLazyBatchSize()).isEqualTo(20);
    assertThat(res.fetchConfig.getQueryBatchSize()).isEqualTo(30);
    assertThat(res.readOnly).isTrue();
    assertThat(res.cache).isTrue();
  }

  @Test
  public void when_formula() {

    OrmQueryPropertiesParser.Response res = OrmQueryPropertiesParser.parse("a,MD5(id::text) as b,c");
    assertThat(res.included).containsExactly("a", "MD5(id::text) as b", "c");
  }

  private void assertAllDefaults(OrmQueryPropertiesParser.Response res) {
    assertThat(res.cache).isFalse();
    assertThat(res.readOnly).isFalse();
    assertThat(res.fetchConfig.getLazyBatchSize()).isEqualTo(-1);
    assertThat(res.fetchConfig.getQueryBatchSize()).isEqualTo(-1);
    assertThat(res.included).isNull();
  }
}
