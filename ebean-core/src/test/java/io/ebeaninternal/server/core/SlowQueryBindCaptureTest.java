package io.ebeaninternal.server.core;

import io.ebean.DB;
import io.ebean.Database;
import io.ebean.Query;
import io.ebeaninternal.api.SpiQuery;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Customer;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SlowQueryBindCaptureTest {

  static Database db;

  @BeforeAll
  static void beforeAll() {
    db = DB.getDefault();
    db.script().run("/h2-init.sql");
  }

  @Test
  void bindParams_capture() {
    String sql = "select * from o_customer p where p.status = ?";
    Query<Customer> query = db.findNative(Customer.class, sql)
      .setParameter("N");
    query.findList();

    var sbc = new SlowQueryBindCapture((SpiQuery<?>) query);
    List<Object> bindParams = sbc.capture();
    assertThat(bindParams).hasSize(1);
    assertThat(bindParams.get(0)).isEqualTo("N");
  }

  @Test
  void expressionsCapture() {
    Query<Customer> query = db.find(Customer.class)
      .where()
      .startsWith("name", "rob")
      .eq("status", "N")
      .query();

    query.findList();

    var sbc = new SlowQueryBindCapture((SpiQuery<?>) query);
    List<Object> bindParams = sbc.capture();
    assertThat(bindParams).hasSize(2);
    assertThat(bindParams.get(0)).isEqualTo("rob%");
    assertThat(bindParams.get(1)).isEqualTo("N");
  }

  @SuppressWarnings("unchecked")
  @Test
  void multiValueCapture() {
    Query<Customer> query = db.find(Customer.class)
      .where()
      .in("id", List.of(1, 2, 3, 4))
      .query();

    query.findList();

    var sbc = new SlowQueryBindCapture((SpiQuery<?>) query);
    List<Object> bindParams = sbc.capture();
    assertThat(bindParams).hasSize(1);
    Object bindParam0 = bindParams.get(0);
    assertThat(bindParam0).isInstanceOf(ArrayList.class);
    var values = (ArrayList<Object>) bindParam0;
    assertThat(values).contains(1, 2, 3, 4);
  }
}
