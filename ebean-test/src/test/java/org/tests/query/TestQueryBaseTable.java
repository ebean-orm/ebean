package org.tests.query;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.annotation.ForPlatform;
import io.ebean.annotation.Platform;
import io.ebean.test.LoggedSql;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Customer;
import org.tests.model.basic.ResetBasicData;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestQueryBaseTable extends BaseTestCase {

  @ForPlatform({Platform.H2, Platform.POSTGRES})
  @Test
  public void test() {

    ResetBasicData.reset();

    LoggedSql.start();

    Customer one = DB.find(Customer.class)
      .setBaseTable("O_CUSTOMER")
      .where().startsWith("name", "Rob")
      .findOne();

    Customer two = DB.find(Customer.class)
      .where().startsWith("name", "Rob")
      .findOne();

    Customer three = DB.find(Customer.class)
      .setBaseTable("O_CUSTOMER")
      .where().startsWith("name", "Fiona")
      .findOne();

    assertThat(one.getName()).isEqualTo(two.getName());
    assertThat(three.getName()).isEqualTo("Fiona");

    List<String> sql = LoggedSql.stop();

    assertThat(sql).hasSize(3);
    assertSql(sql.get(0)).contains("from O_CUSTOMER");
    assertSql(sql.get(1)).contains("from o_customer");
    assertSql(sql.get(2)).contains("from O_CUSTOMER");

  }
}
