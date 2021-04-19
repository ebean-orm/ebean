package io.ebeaninternal.server.core;


import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DtoQueryRequestTest {

  @Test
  public void testParse() {

    assertEquals("foo", DtoQueryRequest.parseColumn("foo"));
    assertEquals("bar", DtoQueryRequest.parseColumn("zx__t0_bar"));
    assertEquals("BAR", DtoQueryRequest.parseColumn("ZX__T0_BAR"));
    assertEquals("baz", DtoQueryRequest.parseColumn("zx__t42_baz"));
    assertEquals("BAZ", DtoQueryRequest.parseColumn("ZX__T42_BAZ"));

    assertEquals("e_t42_nope", DtoQueryRequest.parseColumn("e_t42_nope"));
    assertEquals("_f_t42_nope", DtoQueryRequest.parseColumn("_f_t42_nope"));
  }
}
