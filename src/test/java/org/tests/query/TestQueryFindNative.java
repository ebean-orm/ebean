package org.tests.query;

import io.ebean.BaseTestCase;
import io.ebean.DB;
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
      .setParameter(1, "J%")
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
      .setParameter(1, "J%")
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
      .setParameter(1, 100)
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
        .setParameter(1, "Rob")
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
      .setParameter(1, LocalDate.now())
      .setParameter(2, LocalDate.now())
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

}
