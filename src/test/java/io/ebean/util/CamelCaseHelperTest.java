package io.ebean.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CamelCaseHelperTest {

  @Test
  public void when_underscore() throws Exception {

    assertEquals(CamelCaseHelper.toCamelFromUnderscore("hello_there"), "helloThere");
    assertEquals(CamelCaseHelper.toCamelFromUnderscore("hello_there_jim"), "helloThereJim");
  }

  @Test
  public void when_trailing_numbers() throws Exception {

    assertEquals(CamelCaseHelper.toCamelFromUnderscore("hello_1"), "hello1");
    assertEquals(CamelCaseHelper.toCamelFromUnderscore("hello_there_2"), "helloThere2");
  }

  @Test
  public void when_already_camel() throws Exception {
    assertEquals(CamelCaseHelper.toCamelFromUnderscore("helloThere"), "helloThere");
    assertEquals(CamelCaseHelper.toCamelFromUnderscore("helloThereJim"), "helloThereJim");
    assertEquals(CamelCaseHelper.toCamelFromUnderscore("hello"), "hello");
    assertEquals(CamelCaseHelper.toCamelFromUnderscore("HELLO"), "hello");
  }

}
