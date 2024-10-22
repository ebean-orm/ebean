package org.tests.query;

import io.ebean.DB;
import io.ebean.Query;
import io.ebean.annotation.Platform;
import io.ebean.test.LoggedSql;
import io.ebean.xtest.BaseTestCase;
import io.ebean.xtest.ForPlatform;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Contact;
import org.tests.model.basic.Customer;
import org.tests.model.basic.OrderDetail;
import org.tests.model.basic.ResetBasicData;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TestDistinctOnQuery extends BaseTestCase {

  @ForPlatform({Platform.POSTGRES, Platform.YUGABYTE})
  @Test
  void distinctOn() {
    ResetBasicData.reset();

    LoggedSql.start();

    List<Customer> customers = DB.find(Customer.class)
      .distinctOn("id")
      .select("name")
      .fetch("contacts", "firstName, lastName")
      .orderBy("id, contacts.lastName")
      .setMaxRows(10)
      .findList();

    for (Customer customer : customers) {
      List<Contact> contacts = customer.getContacts();
      assertThat(contacts.size()).isEqualTo(1);
    }

    List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(1);
    assertThat(sql.get(0)).contains("select distinct on (t0.id) t0.id, t0.name, t1.id, t1.first_name, t1.last_name from o_customer t0 left join contact t1 on t1.customer_id = t0.id order by t0.id, t1.last_name limit 10;");
  }

  @ForPlatform({Platform.POSTGRES, Platform.YUGABYTE})
  @Test
  void distinctOnSubQuery() {
    ResetBasicData.reset();

    LoggedSql.start();

    Query<OrderDetail> subQuery = DB.find(OrderDetail.class)
      .distinctOn("product")
      .select("id")
      .orderBy("product, updtime desc")
      .setMaxRows(10);

    List<OrderDetail> lines = DB.find(OrderDetail.class)
      .select("product, orderQty, shipQty, cretime")
      .where().in("id", subQuery)
      .orderBy().asc("cretime")
      .findList();

    assertThat(lines).isNotEmpty();
    List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(1);
    assertThat(sql.get(0)).contains("select t0.id, t0.product_id, t0.order_qty, t0.ship_qty, t0.cretime from o_order_detail t0 where t0.id in (select distinct on (t0.product_id) t0.id from o_order_detail t0 order by t0.product_id, t0.updtime desc limit 10) order by t0.cretime");
  }

  @ForPlatform({Platform.POSTGRES, Platform.YUGABYTE})
  @Test
  void distinctOnManyToOne() {
    ResetBasicData.reset();
    int totalCount = DB.find(Contact.class).findCount();

    LoggedSql.start();
    List<Contact> contacts = DB.find(Contact.class)
      .distinctOn("customer")
      .select("*")
      .orderBy("customer, updtime desc")
      .setMaxRows(10)
      .findList();

    assertThat(contacts.size()).isLessThan(totalCount);

    List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(1);
    assertThat(sql.get(0)).contains("select distinct on (t0.customer_id) t0.id, t0.first_name, t0.last_name");
    assertThat(sql.get(0)).contains("from contact t0 order by t0.customer_id, t0.updtime desc limit 10");
  }

  @ForPlatform({Platform.POSTGRES, Platform.YUGABYTE})
  @Test
  void distinctOnToDto() {
    ResetBasicData.reset();
    int totalCount = DB.find(Contact.class).findCount();

    LoggedSql.start();
    List<MyDto> contacts = DB.find(Contact.class)
      .distinctOn("lastName")
      .select("firstName, lastName")
      .orderBy("lastName desc")
      .setMaxRows(10)
      .asDto(MyDto.class)
      .findList();

    assertThat(contacts.size()).isLessThan(totalCount);
    for (MyDto contact : contacts) {
      assertThat(contact.firstName()).isNotBlank();
      assertThat(contact.lastName()).isNotBlank();
    }

    List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(1);
    assertThat(sql.get(0)).contains("select distinct on (t0.last_name) t0.first_name, t0.last_name from contact t0 order by t0.last_name desc limit 10");
  }

  @ForPlatform({Platform.POSTGRES, Platform.YUGABYTE})
  @Test
  void distinctOnToDtoWithId() {
    ResetBasicData.reset();
    int totalCount = DB.find(Contact.class).findCount();

    LoggedSql.start();
    List<MyDtoWithId> contacts = DB.find(Contact.class)
      .distinctOn("lastName")
      .select("id, firstName, lastName")
      .orderBy("lastName desc")
      .setMaxRows(10)
      .asDto(MyDtoWithId.class)
      .findList();

    assertThat(contacts.size()).isLessThan(totalCount);
    for (MyDtoWithId contact : contacts) {
      assertThat(contact.id()).isGreaterThan(0);
      assertThat(contact.firstName()).isNotBlank();
      assertThat(contact.lastName()).isNotBlank();
    }

    List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(1);
    assertThat(sql.get(0)).contains("select distinct on (t0.last_name) t0.id, t0.first_name, t0.last_name from contact t0 order by t0.last_name desc limit 10");
  }

  public static class MyDto {
    private final String firstName;
    private final String lastName;

    public MyDto(String firstName, String lastName) {
      this.firstName = firstName;
      this.lastName = lastName;
    }

    public String firstName() {
      return firstName;
    }

    public String lastName() {
      return lastName;
    }
  }

  public static class MyDtoWithId {
    private final long id;
    private final String firstName;
    private final String lastName;

    public MyDtoWithId(long id, String firstName, String lastName) {
      this.id = id;
      this.firstName = firstName;
      this.lastName = lastName;
    }

    public long id() {
      return id;
    }

    public String firstName() {
      return firstName;
    }

    public String lastName() {
      return lastName;
    }
  }
}
