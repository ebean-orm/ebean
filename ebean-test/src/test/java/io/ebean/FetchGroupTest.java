package io.ebean;

import io.ebean.service.SpiFetchGroupQuery;
import io.ebean.test.LoggedSql;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Address;
import org.tests.model.basic.Contact;
import org.tests.model.basic.Customer;
import org.tests.model.basic.ResetBasicData;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class FetchGroupTest extends BaseTestCase {

  @Test
  public void simple() {

    FetchGroup<Customer> fetch = FetchGroup.of(Customer.class, "name, status");

    Query<Customer> query = Customer.find
      .query()
      .where()
      .ilike("name", "rob")
      .select(fetch);

    query.findList();

    assertThat(sqlOf(query)).contains("select t0.id, t0.name, t0.status from");
  }


  @Test
  public void nestedWithQueryJoin() {

    ResetBasicData.reset();

    FetchGroup<Customer> fetch = FetchGroup.of(Customer.class)
      .select("name, status")
      .fetchQuery("contacts", "firstName, lastName, email")
      .build();

    Query<Customer> query = Customer.find
      .query()
      .where()
      .ilike("name", "rob")
      .select(fetch);

    LoggedSql.start();
    query.findList();

    List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(2);
    assertSql(sql.get(0)).contains("select t0.id, t0.name, t0.status from o_customer");
    assertSql(sql.get(1)).contains("select t0.customer_id, t0.id, t0.first_name, t0.last_name, t0.email from contact");
  }


  @Test
  public void nestedWithQueryJoin_asNestedFetchGroup() {

    ResetBasicData.reset();

    FetchGroup<Contact> CT_NAME = FetchGroup.of(Contact.class, "firstName, lastName, email");

    FetchGroup<Customer> fetch = FetchGroup.of(Customer.class)
      .select("name")
      .fetchQuery("contacts", CT_NAME)
      .build();

    Query<Customer> query = Customer.find
      .query()
      .where()
      .ilike("name", "rob")
      .select(fetch);

    LoggedSql.start();
    query.findList();

    List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(2);
    assertSql(sql.get(0)).contains("select t0.id, t0.name from o_customer");
    assertSql(sql.get(1)).contains(" from contact");
  }

  @Test
  public void nested_withNestedFetchGroup() {

    ResetBasicData.reset();

    FetchGroup<Address> FGAddress = FetchGroup.of(Address.class)
      .select("line1, line2, city")
      .fetch("country", "name")
      .build();

    FetchGroup<Customer> FBCustomer = FetchGroup.of(Customer.class)
      .select("name, version")
      .fetch("billingAddress", FGAddress)
      .build();

    Query<Customer> query = Customer.find
      .query()
      .where()
      .ilike("name", "rob")
      .select(FBCustomer);

    LoggedSql.start();
    query.findList();

    List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(1);
    assertSql(sql.get(0)).contains("select t0.id, t0.name, t0.version, t1.id, t1.line_1, t1.line_2, t1.city, t2.code, t2.name from o_customer t0 left join o_address t1 on t1.id = t0.billing_address_id left join o_country t2 on t2.code = t1.country_code ");
  }

  @Test
  public void fetchGroupQuery() {

    // in practice this query is used by query beans for type safe FetchGroup construction
    final SpiFetchGroupQuery<Customer> query = FetchGroup.queryFor(Customer.class);
    query.select("name");

    final FetchGroup<Customer> fetchGroup = query.buildFetchGroup();

    LoggedSql.start();

    Customer.find
      .query()
      .where()
      .ilike("name", "rob")
      .select(fetchGroup)
      .findList();

    List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(1);
    assertSql(sql.get(0)).contains("select t0.id, t0.name from o_customer t0 where");
  }
}
