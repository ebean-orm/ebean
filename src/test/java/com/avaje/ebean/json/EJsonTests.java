package com.avaje.ebean.json;

import java.io.StringReader;
import java.util.List;
import java.util.Map;

import javax.json.Json;
import javax.json.stream.JsonParser;

import org.junit.Assert;
import org.junit.Test;

public class EJsonTests {

  @Test
  public void test_map_simple() {
    
    String jsonInput = "{\"name\":\"rob\",\"age\":12}";
    Object result = EJson.parse(jsonInput);
    
    Assert.assertTrue(result instanceof Map);
    Map<?,?> map = (Map<?,?>)result;
    Assert.assertEquals("rob", map.get("name"));
    Assert.assertEquals(12L, map.get("age"));
    
    String jsonOutput = EJson.write(result);
    Assert.assertEquals(jsonInput, jsonOutput);
  }
  
  @Test
  public void test_map_nested() {
    
    String jsonInput = "{\"name\":\"rob\",\"age\":12,\"org\":{\"name\":\"superorg\",\"rating\":4},\"nums\":[1,2,3]}";
    Object result = EJson.parse(jsonInput);
    
    Assert.assertTrue(result instanceof Map);
    Map<?,?> map = (Map<?,?>)result;
    Assert.assertEquals(4, map.size());
    Assert.assertEquals("rob", map.get("name"));
    Assert.assertEquals(12L, map.get("age"));
    
    Map<?,?> org = (Map<?,?>)map.get("org");
    Assert.assertEquals("superorg", org.get("name"));
    Assert.assertEquals(4L, org.get("rating"));

    List<?> nums = (List<?>)map.get("nums");
    Assert.assertEquals(3, nums.size());
    Assert.assertEquals(1L, nums.get(0));
    Assert.assertEquals(2L, nums.get(1));
    Assert.assertEquals(3L, nums.get(2));
    
    String jsonOutput = EJson.write(result);
    Assert.assertEquals(jsonInput, jsonOutput);
  }

  @Test
  public void test_map_withNull() {
    
    String jsonInput = "{\"name\":\"rob\",\"age\":null}";
    Object result = EJson.parse(jsonInput);
    
    Assert.assertTrue(result instanceof Map);
    Map<?,?> map = (Map<?,?>)result;
    Assert.assertEquals("rob", map.get("name"));
    Assert.assertNull(map.get("age"));
    
    String jsonOutput = EJson.write(result);
    Assert.assertEquals(jsonInput, jsonOutput);
  }

  @Test
  public void test_list_simple() {
    
    String jsonInput = "[\"name\",\"rob\",12,13]";
    Object result = EJson.parse(jsonInput);
    
    Assert.assertTrue(result instanceof List);
    List<?> list = (List<?>)result;
    Assert.assertEquals(4, list.size());
    Assert.assertEquals("name", list.get(0));
    Assert.assertEquals("rob", list.get(1));
    Assert.assertEquals(12L, list.get(2));
    Assert.assertEquals(13L, list.get(3));
    
    String jsonOutput = EJson.write(result);
    Assert.assertEquals(jsonInput, jsonOutput);
  }
  
  @SuppressWarnings("unchecked")
  @Test
  public void test_list_ofMaps() {
    
    String jsonInput = "[{\"name\":\"rob\",\"age\":12},{\"name\":\"mike\",\"age\":13}]";
    Object result = EJson.parse(jsonInput);
    
    Assert.assertTrue(result instanceof List);

    List<Map<?,?>> list = (List<Map<?,?>>)result;
    Assert.assertEquals(2, list.size());
    Assert.assertEquals("rob", list.get(0).get("name"));
    Assert.assertEquals(12L, list.get(0).get("age"));
    Assert.assertEquals("mike", list.get(1).get("name"));
    Assert.assertEquals(13L, list.get(1).get("age"));
    
    String jsonOutput = EJson.write(result);
    Assert.assertEquals(jsonInput, jsonOutput);
  }
  
  @Test
  public void test_partial_read() {
    
    String jsonInput = "{\"name\":\"rob\",\"age\":null,\"friend\":{\"name\":\"mike\",\"age\":13}},some more json would follow...";
    StringReader reader = new StringReader(jsonInput);
    JsonParser parser = Json.createParser(reader);
    
    Object result = EJson.parsePartial(parser);
    
    Assert.assertTrue(result instanceof Map);
    Map<?,?> map = (Map<?,?>)result;
    Assert.assertEquals("rob", map.get("name"));
    Assert.assertNull(map.get("age"));

    Map<?,?> friend = (Map<?,?>)map.get("friend");
    Assert.assertEquals("mike", friend.get("name"));
    Assert.assertEquals(13L, friend.get("age"));

  }

//  @Test
//  public void test_partial_read_primitives() {
//    
//    Object result = null;
//    JsonParser parser = Json.createParser(new StringReader(","));
//    
////    Object result = EJson.parsePartial(parser);
////    Assert.assertNull(result);
//    
//    parser = Json.createParser(new StringReader("12L"));
//    result = EJson.parsePartial(parser);
//    Assert.assertEquals(12L, result);
//
//    parser = Json.createParser(new StringReader("true"));
//    result = EJson.parsePartial(parser);
//    Assert.assertEquals(Boolean.TRUE, result);
//
//    parser = Json.createParser(new StringReader("\"foo\""));
//    result = EJson.parsePartial(parser);
//    Assert.assertEquals("foo", result);
//
//  }

}
