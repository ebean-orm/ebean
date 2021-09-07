package org.tests.update;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import org.tests.model.basic.EBasicVer;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestMarkAsDirty extends BaseTestCase {

  @Test
  public void test() throws InterruptedException {

    EBasicVer bean = new EBasicVer("markAsDirty");
    Ebean.save(bean);

    Timestamp lastUpdate = bean.getLastUpdate();
    assertNotNull(lastUpdate);

    Thread.sleep(100);

    // ensure the update occurs and version property is updated/incremented
    Ebean.markAsDirty(bean);
    Ebean.save(bean);

    Timestamp lastUpdate2 = bean.getLastUpdate();
    assertNotNull(lastUpdate2);
    assertNotEquals(lastUpdate, lastUpdate2);

  }

}
