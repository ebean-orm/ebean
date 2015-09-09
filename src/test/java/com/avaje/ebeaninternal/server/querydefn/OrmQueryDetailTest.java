package com.avaje.ebeaninternal.server.querydefn;

import org.junit.Test;

import static org.junit.Assert.*;

public class OrmQueryDetailTest {


  @Test
  public void test_isAutoTuneEqual() {

    OrmQueryDetailParser parser1 = new OrmQueryDetailParser("select (id,name) fetch customer (name) fetch details (code)");
    OrmQueryDetail detail1 = parser1.parse();

    OrmQueryDetailParser parser2 = new OrmQueryDetailParser("select (id,name)  fetch details (code) fetch customer (name)");
    OrmQueryDetail detail2 = parser2.parse();

    assertTrue(detail1.isAutoTuneEqual(detail2));
  }
}