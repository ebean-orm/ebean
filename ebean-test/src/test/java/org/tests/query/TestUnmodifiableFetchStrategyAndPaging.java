package org.tests.query;

import io.ebean.DB;
import io.ebean.LazyInitialisationException;
import io.ebean.PagedList;
import io.ebean.Paging;
import io.ebean.test.LoggedSql;
import io.ebean.xtest.BaseTestCase;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Contact;
import org.tests.model.basic.Customer;
import org.tests.model.basic.Order;
import org.tests.model.basic.ResetBasicData;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Validates that fetch strategy (fetchQuery / fetchLazy secondary query hints) and pagination
 * (setPaging / findPagedList) carry over correctly onto setUnmodifiable(true) entity graphs.
 * <p>
 * This is groundwork for issue #2540 (nested DTO mapping) - the plan is to map an unmodifiable
 * entity graph into a nested DTO graph, so we need confidence that these existing query features
 * continue to behave as expected on that unmodifiable graph.
 */
class TestUnmodifiableFetchStrategyAndPaging extends BaseTestCase {

  @Test
  void fetchQuery_withUnmodifiable_expectSecondaryQueryEagerlyLoadedAndUnmodifiable() {
    ResetBasicData.reset();

    LoggedSql.start();

    List<Customer> customers = DB.find(Customer.class)
      .setUnmodifiable(true)
      .select("id,name")
      .fetchQuery("contacts", "firstName,lastName")
      .orderBy("id")
      .findList();

    List<String> sql = LoggedSql.stop();
    // 1 query for customers + 1 secondary query for contacts (fetchQuery = eager secondary query)
    assertThat(sql).hasSize(2);

    assertThat(customers).isNotEmpty();
    Customer customer = customers.get(0);
    assertThat(DB.beanState(customer).isUnmodifiable()).isTrue();

    // fetched via fetchQuery so already loaded - accessing it must not throw and must not
    // trigger any further lazy loading query
    LoggedSql.start();
    List<Contact> contacts = customer.getContacts();
    assertThat(LoggedSql.stop()).isEmpty();

    assertThat(contacts).isNotEmpty();
    Contact contact = contacts.get(0);
    assertThat(DB.beanState(contact).isUnmodifiable()).isTrue();
    assertThatThrownBy(() -> contact.setFirstName("junk"))
      .isInstanceOf(io.ebean.UnmodifiableEntityException.class);
  }

  @Test
  void fetchLazy_withUnmodifiable_expectNotEagerlyLoadedAndFailsFastOnAccess() {
    ResetBasicData.reset();

    LoggedSql.start();

    List<Order> orders = DB.find(Order.class)
      .setUnmodifiable(true)
      .select("status")
      .fetchLazy("customer", "name")
      .findList();

    List<String> sql = LoggedSql.stop();
    // fetchLazy defers loading to first access - with disableLazyLoading (via unmodifiable)
    // that deferred load can never happen, so only the root query should run
    assertThat(sql).hasSize(1);

    assertThat(orders).isNotEmpty();
    Order order = orders.get(0);
    assertThat(DB.beanState(order).isUnmodifiable()).isTrue();

    // the *ToOne reference itself (just the foreign key / id) is available without lazy
    // loading, so getCustomer() alone does not throw
    Customer customerRef = order.getCustomer();
    assertThat(customerRef).isNotNull();
    assertThat(customerRef.getId()).isNotNull();

    // but accessing a property that requires the fetchLazy relation to actually be loaded
    // must fail fast rather than silently lazy load
    assertThatThrownBy(customerRef::getName)
      .isInstanceOf(LazyInitialisationException.class)
      .hasMessageContaining("Property not loaded: name");
  }

  @Test
  void setPaging_withUnmodifiable_expectCorrectSqlAndUnmodifiableResults() {
    ResetBasicData.reset();

    var paging = Paging.of(0, 2, "id");

    LoggedSql.start();
    List<Customer> customers = DB.find(Customer.class)
      .setUnmodifiable(true)
      .select("id,name")
      .setPaging(paging)
      .findList();
    List<String> sql = LoggedSql.stop();

    assertThat(sql).hasSize(1);
    if (isLimitOffset()) {
      assertThat(sql.get(0)).contains("order by t0.id limit 2");
    }

    assertThat(customers).hasSizeLessThanOrEqualTo(2);
    for (Customer customer : customers) {
      assertThat(DB.beanState(customer).isUnmodifiable()).isTrue();
      assertThatThrownBy(() -> customer.setName("junk"))
        .isInstanceOf(io.ebean.UnmodifiableEntityException.class);
    }
  }

  @Test
  void findPagedList_withUnmodifiable_expectTotalCountAndUnmodifiableResults() {
    ResetBasicData.reset();

    PagedList<Customer> pagedList = DB.find(Customer.class)
      .setUnmodifiable(true)
      .select("id,name")
      .orderBy("id")
      .setFirstRow(0)
      .setMaxRows(2)
      .findPagedList();

    List<Customer> customers = pagedList.getList();
    assertThat(customers).hasSizeLessThanOrEqualTo(2);
    assertThat(pagedList.getTotalCount()).isGreaterThanOrEqualTo(customers.size());

    for (Customer customer : customers) {
      assertThat(DB.beanState(customer).isUnmodifiable()).isTrue();
    }
  }
}
