package io.ebeaninternal.api;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class BindParamsTest {

  @Test
  public void test_hash() {

    BindParams bindParams = new BindParams();

    List<String> ids = Arrays.asList("1", "2", "3");
    bindParams.setParameter("ids", ids);
    BindParams.Param param = bindParams.getParameter("ids");
    assertEquals(3, param.queryBindCount());
    assertFalse(bindParams.isSameBindHash());

    List<String> ids2 = Arrays.asList("1", "2", "3", "4");
    bindParams.setParameter("ids", ids2);
    assertEquals(4, param.queryBindCount());
    assertFalse(bindParams.isSameBindHash());

    List<String> ids3 = Arrays.asList("2", "99", "44");
    bindParams.setParameter("ids", ids3);
    assertEquals(3, param.queryBindCount());
    assertFalse(bindParams.isSameBindHash());


    List<String> ids4 = Arrays.asList("4545", "3499", "3444");
    bindParams.setParameter("ids", ids4);
    assertEquals(3, param.queryBindCount());
    assertTrue(bindParams.isSameBindHash());
  }

}
