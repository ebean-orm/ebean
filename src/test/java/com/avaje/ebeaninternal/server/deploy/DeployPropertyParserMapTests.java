package com.avaje.ebeaninternal.server.deploy;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

public class DeployPropertyParserMapTests {

  @Test
  public void test_like_escape() {
    
    
    Map<String, String> map = new HashMap<String, String>();
    map.put("customer.name", "t1.name");
    map.put("id", "t0.id");
    
    DeployPropertyParserMap parser = new DeployPropertyParserMap(map);
    
    String output = parser.parse("(lower(customer.name) like ? escape'' or id > ?)");
    
    System.out.println(output);
    Assert.assertEquals("(lower(t1.name) like ? escape'' or t0.id > ?)", output);
  }
  
}
