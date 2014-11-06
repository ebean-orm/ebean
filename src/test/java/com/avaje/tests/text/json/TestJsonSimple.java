package com.avaje.tests.text.json;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.Reader;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.json.EJson;
import com.avaje.ebean.text.json.JsonContext;

public class TestJsonSimple extends BaseTestCase {

  @SuppressWarnings("unchecked")
  @Test
  public void test() throws IOException {

    InputStream is = this.getClass().getResourceAsStream("/example1.json");

    final Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
    LineNumberReader lineReader = new LineNumberReader(reader);

    String readLine = null;

    StringBuilder sb = new StringBuilder();
    while ((readLine = lineReader.readLine()) != null) {
      sb.append(readLine);
    }

    String jsonText = sb.toString();

    Object el = EJson.parse(jsonText);
    System.out.println("Got " + el);

    Map<String,Object> e2 = EJson.parseObject("{\"a\":12, \"name\":{\"first\":\"rob\", \"last\":\"byg\"}}");

    Assert.assertEquals(12L, e2.get("a"));
    Assert.assertEquals("rob", ((Map<String,Object>)e2.get("name")).get("first"));

    Map<String, String> m = new LinkedHashMap<String, String>();
    m.put("hello", "rob");
    m.put("test", "me");

    JsonContext jsonContext = Ebean.createJsonContext();
    String jsonString = jsonContext.toJsonString(m);
    System.out.println(jsonString);

    String s = "{\"parishId\":\"18\",\"contentId\":null,\"contentStatus\":null,\"contentType\":\"pg-hello\",\"content\":\"asd\"}";

    Object jsonElement = EJson.parse(s);
    Assert.assertNotNull(jsonElement);

    Map<String,Object> e3 = EJson.parseObject("{\"name\":\"\\u60a8\\u597d\"}");

    Assert.assertTrue(((String)e3.get("name")).length()==2);

  }

}
