package com.avaje.ebean.text.json;

import org.junit.Assert;
import org.junit.Test;

import com.avaje.ebean.text.PathProperties;
import com.avaje.ebean.text.json.JsonWriteOptions;

public class JsonWriteOptionsTests {

  @Test
  public void test_parse() {
    
    JsonWriteOptions options = JsonWriteOptions.parsePath("id,status,name");
    PathProperties pathProps = options.getPathProperties();
    
    Assert.assertEquals(1, pathProps.getPaths().size());
    Assert.assertTrue(pathProps.get(null).contains("id"));
    Assert.assertTrue(pathProps.get(null).contains("name"));
    Assert.assertTrue(pathProps.get(null).contains("status"));
    Assert.assertFalse(pathProps.get(null).contains("foo"));
    
  }
}
