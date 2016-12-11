package io.ebean;

import io.ebean.Ebean;
import io.ebean.EbeanServer;
import org.tests.model.basic.EBasic;
import org.tests.model.basic.Order;
import org.tests.model.basic.ResetBasicData;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

public class EbeanServer_refresh {

  @Test
  public void basic() {

    EbeanServer server = Ebean.getDefaultServer();

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
