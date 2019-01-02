package io.ebean;

import org.junit.Test;
import org.tests.model.basic.EBasic;
import org.tests.model.basic.Order;
import org.tests.model.basic.ResetBasicData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class EbeanServer_refresh {

  @Test
  public void basic() {

    Map<String, String> map = new HashMap<>();
    map.put("tableName", "e_basic");

    EbeanServer server = Ebean.getDefaultServer();
    server.script().run("/scripts/test-script.sql");
    server.script().run("/scripts/test-script-2.sql", map);
    server.script().run(this.getClass().getResource("/scripts/test-script.sql"));
    server.script().run(this.getClass().getResource("/scripts/test-script-2.sql"), map);

    EBasic basic = new EBasic("basic refresh");
    basic.setStatus(EBasic.Status.NEW);
    server.save(basic);

    int rows =
      server.update(EBasic.class)
        .set("status", EBasic.Status.ACTIVE)
        .where().idEq(basic.getId())
        .update();

    assertEquals(rows, 1);

    server.refresh(basic);
    assertEquals(basic.getStatus(), EBasic.Status.ACTIVE);
  }

  @Test
  public void refresh_when_oneToManyLoaded() {

    ResetBasicData.reset();

    Order order = Ebean.find(Order.class, 1);
    order.getCustomer().getName();
    order.getDetails().size();

    Ebean.refresh(order);
  }

  @Test
  public void refresh_when_oneToManyVanilla() {

    ResetBasicData.reset();

    Order order = Ebean.find(Order.class, 1);
    order.getCustomer().getName();
    order.setDetails(new ArrayList<>());

    Ebean.refresh(order);
  }

  @Test
  public void refresh_when_oneToManyNull() {

    ResetBasicData.reset();

    Order order = Ebean.find(Order.class, 1);
    order.getCustomer().getName();
    order.setDetails(null);

    Ebean.refresh(order);
  }

}
