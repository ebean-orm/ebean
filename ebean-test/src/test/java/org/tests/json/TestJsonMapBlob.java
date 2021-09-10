package org.tests.json;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.text.json.EJson;
import org.junit.jupiter.api.Test;
import org.tests.model.json.EBasicJsonMapBlob;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestJsonMapBlob extends BaseTestCase {

  @Test
  public void testInsertUpdateDelete() throws IOException {

    String s0 = "{\"docId\":18,\"contentId\":\"asd\",\"active\":true,\"contentType\":\"pg-hello\",\"content\":{\"name\":\"rob\",\"age\":45}}";

    Map<String, Object> content = EJson.parseObject(s0);

    EBasicJsonMapBlob bean = new EBasicJsonMapBlob();
    bean.setName("one");
    bean.setContent(content);

    DB.save(bean);

    EBasicJsonMapBlob bean1 = DB.find(EBasicJsonMapBlob.class, bean.getId());

    assertEquals(bean.getId(), bean1.getId());
    assertEquals(bean.getName(), bean1.getName());
    assertEquals(bean.getContent().get("contentType"), bean1.getContent().get("contentType"));
    assertEquals(18L, bean1.getContent().get("docId"));

    bean1.setName("just change name");
    DB.save(bean1);
  }
}
