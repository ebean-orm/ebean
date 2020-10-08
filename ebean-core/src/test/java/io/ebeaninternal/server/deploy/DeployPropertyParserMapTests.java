package io.ebeaninternal.server.deploy;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class DeployPropertyParserMapTests {

  @Test
  public void test_like_escape() {


    Map<String, String> map = new HashMap<>();
    map.put("customer.name", "t1.name");
    map.put("id", "t0.id");

    DeployPropertyParserMap parser = new DeployPropertyParserMap(map);

    String output = parser.parse("(lower(customer.name) like ? escape'' or id > ?)");
    Assert.assertEquals("(lower(t1.name) like ? escape'' or t0.id > ?)", output);
  }

}
