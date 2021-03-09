package org.tests.query;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.FetchConfig;
import io.ebean.PagedList;
import org.assertj.core.util.Lists;
import org.ebeantest.LoggedSqlCollector;
import org.junit.Test;
import org.tests.model.basic.Contact;
import org.tests.model.basic.Customer;
import org.tests.model.basic.ResetBasicData;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestQueryFindNative extends BaseTestCase {

  @Test
  public void test_in_bindCount() {

    ResetBasicData.reset();

    LoggedSqlCollector.start();

    String sql = "select id,first_name from contact where id in(:ids)";
    DB.findNative(Contact.class, sql).setParameter("ids", Lists.newArrayList(1, 2, 3)).findList();
    DB.findNative(Contact.class, sql).setParameter("ids", Lists.newArrayList(1, 2)).findList();

    List<String> loggedSql = LoggedSqlCollector.stop();
    assertThat(loggedSql).hasSize(2);
    if (isH2()) {
      assertThat(loggedSql.get(0)).contains("(?,?,?)");
      assertThat(loggedSql.get(1)).contains("(?,?)");
    }
  }

  @Test
  public void findCount() {

    ResetBasicData.reset();
    String sql = "select n.id from contact n where n.first_name like ?";

    LoggedSqlCollector.start();

    int rowCount = server()
      .findNative(Contact.class, sql)
      .setParameter("J%")
      .findCount();

    List<Integer> nativeIds =
      server()
        .findNative(Contact.class, sql)
        .setParameter(1, "J%")
        .findIds();

    List<String> loggedSql = LoggedSqlCollector.stop();

    assertThat(nativeIds).hasSize(rowCount);

    assertThat(loggedSql).hasSize(2);
    assertThat(loggedSql.get(0)).contains("select count(*) from ( select n.id from contact n where n.first_name like ?)");
    assertThat(loggedSql.get(1)).startsWith("select n.id from contact n where n.first_name like ?");
  }

  @Test
  public void findPagedList() {

    ResetBasicData.reset();
    String sql = "select n.id, n.first_name from contact n where n.first_name like ?";

    PagedList<Contact> pagedList = server()
      .findNative(Contact.class, sql)
      .setParameter("J%")
      .setMaxRows(100)
      .findPagedList();

    LoggedSqlCollector.start();

    int listSize = pagedList.getList().size();
    int totalCount = pagedList.getTotalCount();

    List<String> loggedSql = LoggedSqlCollector.stop();

    assertThat(listSize).isEqualTo(totalCount);

    assertThat(loggedSql).hasSize(2);
    assertThat(loggedSql.get(0)).startsWith("select n.id, n.first_name from contact n where n.first_name like ?");
    assertThat(loggedSql.get(1)).contains("select count(*) from ( select n.id, n.first_name from contact n where n.first_name like ?)");
  }


  @Test
  public void findPagedList_withColumnAlias() {

    ResetBasicData.reset();
    String sql = "select n.id, 'SillyName' first_name from contact n where n.id < ? ";

    PagedList<Contact> pagedList = server()
      .findNative(Contact.class, sql)
      .setParameter(100)
      .setMaxRows(100)
      .findPagedList();

    int listSize = pagedList.getList().size();
    int totalCount = pagedList.getTotalCount();

    assertThat(listSize).isEqualTo(totalCount);

    for (Contact contact : pagedList.getList()) {
      assertThat(contact.getFirstName()).isEqualTo("SillyName");
    }
  }


  @Test
  public void findIds() {

    ResetBasicData.reset();

    String sql = "select c.id from contact c where c.first_name like ? ";

    List<Integer> ids = DB.sqlQuery(sql)
      .setParameter(1, "J%")
      .findSingleAttributeList(Integer.class);

    List<Integer> idsScalar =
      server()
        .findNative(Contact.class, sql)
        .setParameter(1, "J%")
        .findSingleAttributeList();

    List<Integer> nativeIds =
      server()
        .findNative(Contact.class, sql)
        .setParameter(1, "J%")
        .findIds();


    assertThat(nativeIds).isNotEmpty();
    assertThat(nativeIds).containsAll(ids);
    assertThat(idsScalar).containsAll(ids);
  }


  @Test
  public void joinFromManyToOne() {

    ResetBasicData.reset();

    String sql =
      "select c.id, c.first_name, c.last_name, t.id, t.name " +
        " from contact c  " +
        " join o_customer t on t.id = c.customer_id " +
        " where t.name like ? " +
        " order by c.first_name, c.last_name";

    List<Contact> contacts =
      server()
        .findNative(Contact.class, sql)
        .setParameter("Rob")
        .findList();


    assertThat(contacts).isNotEmpty();

    Customer customer = contacts.get(0).getCustomer();
    assertThat(customer).isNotNull();
  }

  @Test
  public void joinFromOneToMany() {

    ResetBasicData.reset();

    String sql =
      "select cu.id, cu.name, ct.id, ct.first_name " +
        " from o_customer cu " +
        " left join contact ct on cu.id = ct.customer_id " +
        " where cu.name like ? " +
        " order by name";

    List<Customer> customers =
      server()
        .findNative(Customer.class, sql)
        .setParameter(1, "Rob")
        .findList();

    assertThat(customers).isNotEmpty();

    List<Contact> contacts = customers.get(0).getContacts();
    assertThat(contacts).isNotEmpty();
  }

  @Test
  public void bind_positionedParams_InSelect() {

    ResetBasicData.reset();

    String sql = "select id, name, " +
      "case when anniversary >= ? then 1 when anniversary < ? then 2 end as order_column_1 " +
      "from o_customer " +
      "order by order_column_1";

    LoggedSqlCollector.start();

    List<Customer> result = DB.findNative(Customer.class, sql)
      .setParameter(LocalDate.now())
      .setParameter(LocalDate.now())
      .setFirstRow(1)
      .setMaxRows(10)
      .findList();

    assertThat(result).isNotEmpty();
    List<String> loggedSql = LoggedSqlCollector.stop();

    if (isH2()) {
      assertThat(loggedSql.get(0)).contains("from o_customer order by order_column_1 limit 10 offset 1");
    }
  }

  @Test
  public void bind_namedParams_InSelect() {

    ResetBasicData.reset();

    String sql = "select id, name, " +
      "case when anniversary >= :date then 1 when anniversary < :date then 2 end as order_column_1 " +
      "from o_customer " +
      "order by order_column_1";

    LoggedSqlCollector.start();

    List<Customer> result = DB.findNative(Customer.class, sql)
      .setParameter("date", LocalDate.now())
      .setFirstRow(1)
      .setMaxRows(10)
      .findList();

    assertThat(result).isNotEmpty();
    List<String> loggedSql = LoggedSqlCollector.stop();

    if (isH2()) {
      assertThat(loggedSql.get(0)).contains("from o_customer order by order_column_1 limit 10 offset 1");
    }
  }


  @Test
  public void findNativeWithOneFetchQuery() {

    ResetBasicData.reset();

    String sql = "select * from o_customer";

    LoggedSqlCollector.start();

    List<Customer> result = DB.findNative(Customer.class, sql)
            .fetchQuery("contacts")
            .findList();

    assertThat(result).isNotEmpty();
    List<String> loggedSql = LoggedSqlCollector.stop();

    if (isH2()) {
      assertThat(loggedSql).hasSize(2);
      assertThat(loggedSql.get(0)).contains("from o_customer");
      assertThat(loggedSql.get(1)).contains("from contact");
    }
  }

  @Test
  public void findNativeWithOneFetch() {

    ResetBasicData.reset();

    String sql = "select * from o_customer";

    LoggedSqlCollector.start();

    List<Customer> result = DB.findNative(Customer.class, sql)
            .fetch("contacts", "firstName, lastName")
            .findList();

    assertThat(result).isNotEmpty();
    List<String> loggedSql = LoggedSqlCollector.stop();

    if (isH2()) {
      assertThat(loggedSql).hasSize(2);
      assertThat(loggedSql.get(0)).contains("from o_customer");
      assertThat(loggedSql.get(1)).contains("select t0.customer_id, t0.id, t0.first_name, t0.last_name from contact t0 where");
    }
  }

  @Test
  public void findNativeWithMultipleFetchQuery() {

    ResetBasicData.reset();

    String sql = "select * from o_customer";

    LoggedSqlCollector.start();

    List<Customer> result = DB.findNative(Customer.class, sql)
            .fetchQuery("orders")
            .fetchQuery("contacts")
            .findList();

    assertThat(result).isNotEmpty();
    List<String> loggedSql = LoggedSqlCollector.stop();

    if (isH2()) {
      assertThat(loggedSql).hasSize(3);
      assertThat(loggedSql.get(0)).contains("from o_customer");
      assertThat(loggedSql).anyMatch(s -> s.contains("from o_order"));
      assertThat(loggedSql).anyMatch(s -> s.contains("from contact"));
    }
  }

  @Test
  public void findNativeWithMultipleFetch() {

    ResetBasicData.reset();

    String sql = "select * from o_customer";

    LoggedSqlCollector.start();

    List<Customer> result = DB.findNative(Customer.class, sql)
            // with nativeSql fetch (default) are converted to fetchQuery()
            .fetch("orders")
            .fetch("contacts", FetchConfig.ofDefault())
            .findList();

    assertThat(result).isNotEmpty();
    List<String> loggedSql = LoggedSqlCollector.stop();

    if (isH2()) {
      assertThat(loggedSql).hasSize(3);
      assertThat(loggedSql.get(0)).contains("from o_customer");
      assertThat(loggedSql).anyMatch(s -> s.contains("from o_order"));
      assertThat(loggedSql).anyMatch(s -> s.contains("from contact"));
    }
  }

}
