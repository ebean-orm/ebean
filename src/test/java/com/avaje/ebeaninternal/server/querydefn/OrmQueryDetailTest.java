package com.avaje.ebeaninternal.server.querydefn;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

public class OrmQueryDetailTest {

  OrmQueryDetail parse(String query) {
    return new OrmQueryDetailParser(query).parse();
  }

  @Test
  public void isAutoTuneEqual_when_fetchOrderIsDifferent_then_stillEqual() {

    OrmQueryDetail detail1 = parse("select (id,name) fetch customer (name) fetch details (code)");
    OrmQueryDetail detail2 = parse("select (id,name)  fetch details (code) fetch customer (name)");

    assertTrue(detail1.isAutoTuneEqual(detail2));
  }

  @Test
  public void isAutoTuneEqual_when_different_select() {

    OrmQueryDetail detail1 = parse("select (id,name) fetch customer (name) fetch details (code)");
    OrmQueryDetail detail2 = parse("select (id)  fetch details (code) fetch customer (name)");

    assertFalse(detail1.isAutoTuneEqual(detail2));
  }

  @Test
  public void isAutoTuneEqual_when_different_selectInFetch() {

    OrmQueryDetail detail1 = parse("select (id,name) fetch customer (name) fetch details (code)");
    OrmQueryDetail detail2 = parse("select (id,name) fetch customer (id,name) fetch details (code)");

    assertFalse(detail1.isAutoTuneEqual(detail2));
  }

  @Test
  public void isAutoTuneEqual_when_different_additionalFetch() {

    OrmQueryDetail detail1 = parse("select (id,name) fetch customer (name) fetch details (code)");
    OrmQueryDetail detail2 = parse("select (id,name) fetch customer (name) fetch details (code) fetch customer.contacts");

    assertFalse(detail1.isAutoTuneEqual(detail2));
  }

  @Test
  public void select_whenMultiple() throws Exception {

    OrmQueryDetail other = new OrmQueryDetail();
    other.select("id,name");

    OrmQueryProperties root = other.getChunk(null, false);
    assertNull(root.getPath());
    assertThat(root.getIncluded()).containsExactly("id", "name");
  }

  @Test
  public void select_whenOne() throws Exception {

    OrmQueryDetail other = new OrmQueryDetail();
    other.select("name");

    OrmQueryProperties root = other.getChunk(null, false);
    assertNull(root.getPath());
    assertThat(root.getIncluded()).containsExactly("name");
  }
}