package org.tests.text.json;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.text.json.EJson;
import io.ebean.text.json.JsonContext;
import org.junit.Assert;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.Reader;
import java.util.LinkedHashMap;
import java.util.Map;

public class TestJsonSimple extends BaseTestCase {

  @SuppressWarnings("unchecked")
  @Test
  public void test() throws IOException {

    InputStream is = this.getClass().getResourceAsStream("/example1.json");

    final Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
    LineNumberReader lineReader = new LineNumberReader(reader);

    String readLine;

    StringBuilder sb = new StringBuilder();
    while ((readLine = lineReader.readLine()) != null) {
      sb.append(readLine);
    }

    String jsonText = sb.toString();

    Object el = EJson.parse(jsonText);
    assertThat(el).isNotNull();
    
    Map<String, Object> e2 = EJson.parseObject("{\"a\":12, \"name\":{\"first\":\"rob\", \"last\":\"byg\"}}");

    Assert.assertEquals(12L, e2.get("a"));
    Assert.assertEquals("rob", ((Map<String, Object>) e2.get("name")).get("first"));

    Map<String, String> m = new LinkedHashMap<>();
    m.put("hello", "rob");
    m.put("test", "me");

    JsonContext jsonContext = Ebean.json();
    jsonContext.toJson(m);

    String s = "{\"parishId\":\"18\",\"contentId\":null,\"contentStatus\":null,\"contentType\":\"pg-hello\",\"content\":\"asd\"}";

    Object jsonElement = EJson.parse(s);
    Assert.assertNotNull(jsonElement);

    Map<String, Object> e3 = EJson.parseObject("{\"name\":\"\\u60a8\\u597d\"}");

    Assert.assertTrue(((String) e3.get("name")).length() == 2);

  }

}
