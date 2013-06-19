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
import com.avaje.ebean.text.json.JsonContext;
import com.avaje.ebean.text.json.JsonElement;
import com.avaje.ebean.text.json.JsonElementObject;
import com.avaje.ebeaninternal.server.text.json.InternalJsonParser;

public class TestJsonSimple extends BaseTestCase {

  @Test
  public void test() throws IOException {

    boolean b = JsonElement.class.isAssignableFrom(JsonElementObject.class);
    Assert.assertTrue(b);

    InputStream is = this.getClass().getResourceAsStream("/example1.json");

    final Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
    LineNumberReader lineReader = new LineNumberReader(reader);

    String readLine = null;

    StringBuilder sb = new StringBuilder();
    while ((readLine = lineReader.readLine()) != null) {
      sb.append(readLine);
    }

    String jsonText = sb.toString();

    JsonElement el = InternalJsonParser.parse(jsonText);

    System.out.println("Got " + el);

    JsonElement e2 = InternalJsonParser
        .parse("{\"a\":12, \"name\":{\"first\":\"rob\", \"last\":\"byg\"}}");

    Assert.assertEquals(Double.valueOf(12), e2.eval("a"));
    Assert.assertEquals(12, e2.evalInt("a"));
    Assert.assertEquals("rob", e2.evalString("name.first"));

    Assert.assertEquals("byg", e2.evalString("name.last"));

    Map<String, String> m = new LinkedHashMap<String, String>();
    m.put("hello", "rob");
    m.put("test", "me");

    JsonContext jsonContext = Ebean.createJsonContext();
    String jsonString = jsonContext.toJsonString(m, true);
    System.out.println(jsonString);

    String s = "{\"parishId\":\"18\",\"contentId\":null,\"contentStatus\":null,\"contentType\":\"pg-hello\",\"content\":\"<p>\n\tSomeThing</p>\n\"}";

    JsonElement jsonElement = InternalJsonParser.parse(s);
    Assert.assertNotNull(jsonElement);

    JsonElement e3 = InternalJsonParser.parse("{\"name\":\"\\u60a8\\u597d\"}");

    Assert.assertTrue(e3.evalString("name").length()==2);

  }

}
