package com.avaje.ebeaninternal.server.querydefn;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.tests.model.basic.Order;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class OrmQueryDetailTest extends BaseTestCase {

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

  @Test
  public void getFetchPaths_when_noFetches_then_expect_empty() {

    assertThat(new OrmQueryDetail().getFetchPaths()).isEmpty();

    OrmQueryDetail detail = new OrmQueryDetail();
    detail.select("foo");

    assertThat(detail.getFetchPaths()).isEmpty();
  }

  @Test
  public void getFetchPaths_when_oneFetch() {

    OrmQueryDetail detail = new OrmQueryDetail();
    detail.select("foo");
    detail.fetch("customer", null, null);

    assertThat(detail.getFetchPaths()).containsExactly("customer");
  }

  @Test
  public void getFetchPaths_when_multipleFetch_expect_preserveOrder() {

    OrmQueryDetail detail = new OrmQueryDetail();
    detail.select("foo");
    detail.fetch("customer", null, null);
    detail.fetch("details", null, null);

    assertThat(detail.getFetchPaths()).containsExactly("customer", "details");
  }

  @Test
  public void getFetchPaths_when_multipleFetch_expect_preserveOrder_v2() {

    OrmQueryDetail detail = new OrmQueryDetail();
    detail.select("foo");
    detail.fetch("details", null, null);
    detail.fetch("customer", null, null);
    detail.fetch("details.product", null, null);

    assertThat(detail.getFetchPaths()).containsExactly("details", "customer", "details.product");
  }

  @Test
  public void markQueryJoins_when_allowOne_expect_stillFetchJoin() {

    OrmQueryDetail detail = new OrmQueryDetail();
    detail.fetch("details", null, null);

    detail.markQueryJoins(orderDesc(), null, true);

    assertThat(detail.getChunk("details", false).isQueryFetch()).isFalse();
  }

  @Test
  public void markQueryJoins_when_allowNone_expect_queryJoin() {

    OrmQueryDetail detail = new OrmQueryDetail();
    detail.fetch("details", null, null);

    detail.markQueryJoins(orderDesc(), null, false);

    assertThat(detail.getChunk("details", false).isQueryFetch()).isTrue();
  }

  @Test
  public void markQueryJoins_when_allowOneButSecond_expect_queryJoin() {

    OrmQueryDetail detail = new OrmQueryDetail();
    detail.fetch("details", null, null);
    detail.fetch("customer.contacts", null, null);

    detail.markQueryJoins(orderDesc(), null, true);

    assertThat(detail.getChunk("details", false).isQueryFetch()).isFalse();
    assertThat(detail.getChunk("customer.contacts", false).isQueryFetch()).isTrue();
  }

  @Test
  public void markQueryJoins_when_allowNone_expect_bothQueryJoin() {

    OrmQueryDetail detail = new OrmQueryDetail();
    detail.fetch("details", null, null);
    detail.fetch("customer.contacts", null, null);

    detail.markQueryJoins(orderDesc(), null, false);

    assertThat(detail.getChunk("details", false).isQueryFetch()).isTrue();
    assertThat(detail.getChunk("customer.contacts", false).isQueryFetch()).isTrue();
  }

  @Test
  public void sortFetchPaths_when_missingParent_expect_addsMissing() {

    OrmQueryDetail detail = new OrmQueryDetail();
    detail.fetch("customer.contacts", null, null);

    detail.sortFetchPaths(orderDesc());

    assertThat(detail.getFetchPaths()).containsExactly("customer","customer.contacts");
    assertThat(detail.getChunk("customer",false).getIncluded()).containsExactly("id");
  }

  @Test
  public void sortFetchPaths_when_outOfOrder_expect_correctOrder() {

    OrmQueryDetail detail = new OrmQueryDetail();
    detail.fetch("customer.contacts", "email", null);
    detail.fetch("customer", "name", null);

    detail.sortFetchPaths(orderDesc());

    assertThat(detail.getFetchPaths()).containsExactly("customer","customer.contacts");
    assertThat(detail.getChunk("customer",false).getIncluded()).containsExactly("name");
  }

  @Test
  public void sortFetchPaths_when_empty_expect_stillEmpty() {

    OrmQueryDetail detail = new OrmQueryDetail();
    detail.sortFetchPaths(orderDesc());

    assertThat(detail.getFetchPaths()).isEmpty();
  }

  BeanDescriptor<Order> orderDesc() {
    return getBeanDescriptor(Order.class);
  }

}