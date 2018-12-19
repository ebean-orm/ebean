package io.ebean.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CamelCaseHelperTest {

  @Test
  public void when_underscore() {
    assertEquals(CamelCaseHelper.toCamelFromUnderscore("hello_there"), "helloThere");
    assertEquals(CamelCaseHelper.toCamelFromUnderscore("hello_there_jim"), "helloThereJim");
  }

  @Test
  public void when_trailing_numbers() {
    assertEquals(CamelCaseHelper.toCamelFromUnderscore("hello_1"), "hello1");
    assertEquals(CamelCaseHelper.toCamelFromUnderscore("hello_there_2"), "helloThere2");
  }

  @Test
  public void when_numbers() {
    assertEquals(CamelCaseHelper.toCamelFromUnderscore("hello1_id"), "hello1Id");
    assertEquals(CamelCaseHelper.toCamelFromUnderscore("hello_there2_foo"), "helloThere2Foo");
    assertEquals(CamelCaseHelper.toCamelFromUnderscore("hello1id"), "hello1id");
    assertEquals(CamelCaseHelper.toCamelFromUnderscore("hello_there2foo"), "helloThere2foo");
  }

  @Test
  public void when_numbersProceedUppercase() {
    assertEquals(CamelCaseHelper.toCamelFromUnderscore("hello1_id"), "hello1Id");
    assertEquals(CamelCaseHelper.toCamelFromUnderscore("hello_there2_foo"), "helloThere2Foo");

    assertEquals(CamelCaseHelper.toUnderscoreFromCamel("hello1Id"), "hello1_id");
    assertEquals(CamelCaseHelper.toUnderscoreFromCamel("helloThere2Foo"), "hello_there2_foo");
  }

  @Test
  public void when_already_camel() {
    assertEquals(CamelCaseHelper.toCamelFromUnderscore("helloThere"), "helloThere");
    assertEquals(CamelCaseHelper.toCamelFromUnderscore("helloThereJim"), "helloThereJim");
    assertEquals(CamelCaseHelper.toCamelFromUnderscore("hello"), "hello");
    assertEquals(CamelCaseHelper.toCamelFromUnderscore("HELLO"), "hello");
  }

}
