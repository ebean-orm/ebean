package io.ebean.xtest.base;

import io.ebean.DB;
import io.ebean.Database;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.EBasic;
import org.tests.model.basic.Order;
import org.tests.model.basic.OrderDetail;
import org.tests.model.basic.ResetBasicData;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class EbeanServer_refresh {

  private Path createSqlFile() throws IOException {
    File f = File.createTempFile("test-script", ".sql");
    Path path = f.toPath();
    List<String> lines = new ArrayList<>();
    lines.add("select * from o_customer;");
    Files.write(path, lines, StandardOpenOption.TRUNCATE_EXISTING);
    return path;
  }

  @Test
  void script_run_asFile() throws IOException {
    ResetBasicData.reset();

    Path path = createSqlFile();
    DB.script().run(path);
    DB.script().run(path, Collections.emptyMap());
  }

  @Test
  void basic() {
    Map<String, String> map = new HashMap<>();
    map.put("tableName", "e_basic");

    Database server = DB.getDefault();
    DB.script().run("/scripts/test-script.sql");
    DB.script().run("/scripts/test-script-2.sql", map);
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

    basic.setName("modify");
    assertTrue(DB.beanState(basic).isDirty());
    server.refresh(basic);
    assertEquals(basic.getStatus(), EBasic.Status.ACTIVE);
    assertFalse(DB.beanState(basic).isDirty());
  }

  @Test
  void refresh_when_oneToManyLoaded() {
    ResetBasicData.reset();

    Order order = DB.find(Order.class, 1);
    order.getCustomer().getName();
    order.getDetails().size();

    DB.refresh(order);
  }

  @Test
  void refresh_when_oneToManyVanilla() {
    ResetBasicData.reset();

    Order order = DB.find(Order.class, 1);
    order.getCustomer().getName();
    order.setDetails(new ArrayList<>());

    DB.refresh(order);
  }

  @Test
  void refresh_when_oneToManyNull() {
    ResetBasicData.reset();

    Order order = DB.find(Order.class, 1);
    order.getCustomer().getName();
    order.setDetails(null);

    DB.refresh(order);
  }

  @Test
  void refresh_on_details_new() {
    ResetBasicData.reset();

    Order order = DB.find(Order.class, 1);
    DB.refresh(order); // call refresh BEFORE first access on "getDetail";

    assertThat(order.getDetails()).hasSize(3);

    OrderDetail detail = new OrderDetail();
    detail.setOrder(order);
    DB.save(detail);

    try {
      assertThat(order.getDetails()).hasSize(3);
      DB.refresh(order);
      assertThat(order.getDetails()).hasSize(4);
    } finally {
      DB.delete(detail); // restore old state
    }

    DB.refresh(order);

    assertThat(order.getDetails()).hasSize(3);
  }

  @Test
  void refresh_on_details_changed() {
    ResetBasicData.reset();
    Order order = DB.find(Order.class, 1);

    DB.refresh(order); // call refresh BEFORE first access on "getDetail"
    // this changes the loader of the details-bean collection from DefaultServer to DLoadManyContext$LoadBuffer
    // if this refresh is commented out, the test will pass

    assertThat(order.getDetails().get(0).getOrderQty()).isEqualTo(5);

    // search the detail in the DB and change qty to 42
    OrderDetail detail = DB.find(OrderDetail.class, order.getDetails().get(0).getId());
    assertThat(order.getDetails().get(0)).isEqualTo(detail).isNotSameAs(detail);
    detail.setOrderQty(42);
    DB.save(detail);

    try {
      assertThat(order.getDetails().get(0).getOrderQty()).isEqualTo(5);
      DB.refresh(order);
      assertThat(order.getDetails().get(0).getOrderQty()).isEqualTo(42);
    } finally {
      // restore old value
      detail.setOrderQty(5);
      DB.save(detail);
    }

    DB.refresh(order);
    assertThat(order.getDetails().get(0).getOrderQty()).isEqualTo(5);
  }
}
