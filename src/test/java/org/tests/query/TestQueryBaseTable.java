package org.tests.query;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.annotation.ForPlatform;
import io.ebean.annotation.Platform;
import org.ebeantest.LoggedSqlCollector;
import org.junit.Test;
import org.tests.model.basic.Customer;
import org.tests.model.basic.ResetBasicData;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestQueryBaseTable extends BaseTestCase {

  @ForPlatform({Platform.H2, Platform.POSTGRES})
  @Test
  public void test() {

    ResetBasicData.reset();

    LoggedSqlCollector.start();

    Customer one = Ebean.find(Customer.class)
      .setBaseTable("O_CUSTOMER")
      .where().startsWith("name", "Rob")
      .findOne();

    Customer two = Ebean.find(Customer.class)
      .where().startsWith("name", "Rob")
      .findOne();

    Customer three = Ebean.find(Customer.class)
      .setBaseTable("O_CUSTOMER")
      .where().startsWith("name", "Fiona")
      .findOne();

    assertThat(one.getName()).isEqualTo(two.getName());
    assertThat(three.getName()).isEqualTo("Fiona");

    List<String> sql = LoggedSqlCollector.stop();

    assertThat(sql).hasSize(3);
    assertSql(sql.get(0)).contains("from O_CUSTOMER");
    assertSql(sql.get(1)).contains("from o_customer");
    assertSql(sql.get(2)).contains("from O_CUSTOMER");

  }
}
