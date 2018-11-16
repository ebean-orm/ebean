package org.tests.expression.bitwise;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.Filter;
import io.ebean.Query;
import io.ebean.QueryDsl;

import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

public class TestBitwiseExpressions extends BaseTestCase {

  private static BwBean nothing;
  private static BwBean colourOnly;
  private static BwBean sizeOnly;
  private static BwBean colourAndBulk;
  private static BwBean everything;

  @BeforeClass
  public static void setup() {

    Ebean.find(BwBean.class).delete();

    nothing = new BwBean("Nothing", BwFlags.NOTHING);
    colourOnly = new BwBean("ColourOnly", BwFlags.HAS_COLOUR);
    sizeOnly = new BwBean("SizeOnly", BwFlags.HAS_SIZE);
    colourAndBulk = new BwBean("ColourAndBulk", BwFlags.HAS_COLOUR + BwFlags.HAS_BULK);
    everything = new BwBean("Everything", BwFlags.HAS_COLOUR + BwFlags.HAS_BULK + BwFlags.HAS_SIZE);
    nothing.save();
    colourOnly.save();
    sizeOnly.save();
    colourAndBulk.save();
    everything.save();

  }
  @Test
  public void bitwiseNot() {
    // HAS_COLOUR not set ...
    testQuery(condition -> {
      condition.bitwiseNot("flags", BwFlags.HAS_COLOUR);
    }, nothing, sizeOnly);

  }

  @Test
  public void bitwiseAnd() {
    // not bulk set AND size set
    testQuery(condition -> {
      condition.bitwiseAnd("flags", BwFlags.HAS_BULK + BwFlags.HAS_SIZE, BwFlags.HAS_SIZE);
    }, sizeOnly);


    // bulk set AND not size set
    testQuery(condition -> {
      condition.bitwiseAnd("flags", BwFlags.HAS_BULK + BwFlags.HAS_SIZE, BwFlags.HAS_BULK);
    }, colourAndBulk);


    // bulk set AND not size set
    testQuery(condition -> {
      condition.bitwiseAnd("flags", BwFlags.HAS_BULK + BwFlags.HAS_COLOUR, BwFlags.HAS_COLOUR);
    }, colourOnly);
  }

  @Test
  public void bitwiseAny() {

    testQuery(condition -> {
      condition.bitwiseAny("flags", BwFlags.HAS_BULK + BwFlags.HAS_SIZE);
    }, sizeOnly, colourAndBulk, everything);

    testQuery(condition -> {
      condition.bitwiseAny("flags", BwFlags.HAS_BULK + BwFlags.HAS_SIZE + BwFlags.HAS_COLOUR);
    }, colourOnly, sizeOnly, colourAndBulk, everything);

    testQuery(condition -> {
      condition.bitwiseAny("flags", BwFlags.HAS_SIZE + BwFlags.HAS_COLOUR);
    }, colourOnly, sizeOnly, colourAndBulk, everything);

    testQuery(condition -> {
      condition.bitwiseAny("flags", BwFlags.HAS_SIZE);
    }, sizeOnly, everything);

    testQuery(condition -> {
      condition.bitwiseAny("flags", BwFlags.HAS_COLOUR);
    }, colourOnly, colourAndBulk, everything);

  }

  @Test
  public void bitwiseAll() {

    testQuery(condition -> {
      condition.bitwiseAll("flags", BwFlags.HAS_BULK + BwFlags.HAS_SIZE);
    }, everything);

    testQuery(condition -> {
      condition.bitwiseAll("flags", BwFlags.HAS_BULK + BwFlags.HAS_COLOUR);
    }, colourAndBulk, everything);

    testQuery(condition -> {
      condition.bitwiseAll("flags", BwFlags.HAS_SIZE);
    }, sizeOnly, everything);

    testQuery(condition -> {
      condition.bitwiseAll("flags", BwFlags.HAS_COLOUR);
    }, colourOnly, colourAndBulk, everything);

  }



  @SafeVarargs
  private final void testQuery(Consumer<QueryDsl<BwBean,?>> condition, BwBean... expected) {

    // Query
    Query<BwBean> query = Ebean.find(BwBean.class);
    condition.accept(query.where());

    List<BwBean> ref = query.order("id").findList();
    assertThat(ref).containsExactly(expected);

    Filter<BwBean> filter = Ebean.filter(BwBean.class);
    condition.accept(filter);

    // now search all, and filter them manually
    List<BwBean> all = Ebean.find(query.getBeanType()).order("id").findList();
    List<BwBean> filtered = filter.filter(all);

    // System.out.println(query.getQueryPlanKey());
    assertThat(filtered).contains(expected).containsExactly(expected);
  }
}
