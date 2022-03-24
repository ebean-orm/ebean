package org.tests.update;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import io.ebean.text.json.JsonContext;
import io.ebean.text.json.JsonWriteOptions;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.UUOne;
import org.tests.model.basic.UUTwo;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestJsonStatelessUpdate extends BaseTestCase {

  @Test
  public void test() throws IOException {

    UUOne one = new UUOne();
    one.setName("oneName");

    DB.save(one);

    UUTwo two = new UUTwo();
    two.setMaster(one);
    two.setName("twoName");

    DB.save(two);

    UUTwo twoX = DB.find(UUTwo.class, two.getId());

    JsonContext jsonContext = DB.json();

    JsonWriteOptions writeOptions = JsonWriteOptions.parsePath("(id,name,master(*))");
    String jsonString = jsonContext.toJson(twoX, writeOptions);

    jsonString = jsonString.replace("twoName", "twoNameModified");
    jsonString = jsonString.replace("oneName", "oneNameModified");


    UUTwo two2 = jsonContext.toBean(UUTwo.class, jsonString);

    assertEquals(twoX.getId(), two2.getId());
    assertEquals("twoNameModified", two2.getName());
    assertEquals("oneNameModified", two2.getMaster().getName());

    // The update below cascades to also save "master" and that fails
    // as it thinks it should INSERT master rather than UPDATE master

    DB.update(two2);
    awaitL2Cache();


    // confirm the properties where updated as expected
    UUTwo twoConfirm = DB.find(UUTwo.class, two.getId());

    assertEquals("twoNameModified", twoConfirm.getName());
    assertEquals("oneNameModified", twoConfirm.getMaster().getName());

  }

}
