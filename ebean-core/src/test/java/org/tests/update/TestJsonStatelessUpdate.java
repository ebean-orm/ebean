package org.tests.update;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.text.json.JsonContext;
import io.ebean.text.json.JsonWriteOptions;
import org.tests.model.basic.UUOne;
import org.tests.model.basic.UUTwo;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestJsonStatelessUpdate extends BaseTestCase {

  @Test
  public void test() throws IOException {

    UUOne one = new UUOne();
    one.setName("oneName");

    Ebean.save(one);

    UUTwo two = new UUTwo();
    two.setMaster(one);
    two.setName("twoName");

    Ebean.save(two);

    UUTwo twoX = Ebean.find(UUTwo.class, two.getId());

    JsonContext jsonContext = Ebean.json();

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

    Ebean.update(two2);
    awaitL2Cache();


    // confirm the properties where updated as expected
    UUTwo twoConfirm = Ebean.find(UUTwo.class, two.getId());

    assertEquals("twoNameModified", twoConfirm.getName());
    assertEquals("oneNameModified", twoConfirm.getMaster().getName());

  }

}
