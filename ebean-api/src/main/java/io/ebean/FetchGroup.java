package io.ebean;

import org.jspecify.annotations.NullMarked;
import io.ebean.service.SpiFetchGroupQuery;

/**
 * Defines what part of the object graph to load (select and fetch clauses).
 * <p>
 * Using a FetchGroup effectively sets the select() and fetch() clauses for a query. It is alternative
 * to specifying the select() and fetch() clauses on the query allowing for more re-use of "what to load"
 * that can be defined separately from the query and combined with other FetchGroups.
 * </p>
 *
 * <h3>Select example</h3>
 * <pre>{@code
 *
 * FetchGroup<Customer> fetchGroup = FetchGroup.of(Customer.class, "name, status");
 *
 * Customer.query()
 *   .select(fetchGroup)
 *   .findList();
 *
 * }</pre>
 *
 * <h3>Select and fetch example</h3>
 * <pre>{@code
 *
 * FetchGroup<Customer> fetchGroup = FetchGroup.of(Customer.class)
 *   .select("name, status")
 *   .fetch("contacts", "firstName, lastName, email")
 *   .build();
 *
 * Customer.query()
 *   .select(fetchGroup)
 *   .findList();
 *
 * }</pre>
 *
 * <h3>Combining FetchGroups</h3>
 * <p>
 *   FetchGroups can be combined together to form another FetchGroup.
 * </p>
 * <pre>{@code
 *
 *  FetchGroup<Address> FG_ADDRESS = FetchGroup.of(Address.class)
 *    .select("line1, line2, city")
 *    .fetch("country", "name")
 *    .build();
 *
 *  FetchGroup<Customer> FG_CUSTOMER = FetchGroup.of(Customer.class)
 *    .select("name, version")
 *    .fetch("billingAddress", FG_ADDRESS)
 *    .build();
 *
 *
 *  Customer.query()
 *    .select(FG_CUSTOMER)
 *    .findList();
 *
 * }</pre>
 *
 * @param <T> The bean type the Fetch group can be applied to
 */
@NullMarked
public interface FetchGroup<T> {

  /**
   * Return the FetchGroup with the given select clause.
   * <p>
   *   We use this for simple FetchGroup that only select() properties and do not have additional fetch() clause.
   * </p>
   * <pre>{@code
   *
   * FetchGroup<Customer> fetchGroup = FetchGroup.of(Customer.class, "name, status");
   *
   * Customer.query()
   *   .select(fetchGroup)
   *   .findList();
   *
   * }</pre>
   *
   * @param select The select clause of the FetchGroup
   *
   * @return The FetchGroup with the given select clause
   */
  static <T> FetchGroup<T> of(Class<T> cls, String select) {
    return XBootstrapService.fetchGroupOf(cls, select);
  }

  /**
   * Return the FetchGroupBuilder with the given select clause that we can add fetch clauses to.
   * <p>
   * We chain select() with one or more fetch() clauses to define the object graph to load.
   * </p>
   * <pre>{@code
   *
   * FetchGroup<Customer> fetchGroup = FetchGroup.of(Customer.class)
   *   .select("name, status")
   *   .fetch("contacts", "firstName, lastName, email")
   *   .build();
   *
   * Customer.query()
   *   .select(fetchGroup)
   *   .findList();
   *
   * }</pre>
   *
   * @return The FetchGroupBuilder with the given select clause which we will add fetch clauses to
   */
  static <T> FetchGroupBuilder<T> of(Class<T> cls) {
    return XBootstrapService.fetchGroupOf(cls);
  }

  /**
   * Return a query to be used by query beans for constructing FetchGroup.
   */
  static <T> SpiFetchGroupQuery<T> queryFor(Class<T> beanType) {
    return XBootstrapService.fetchGroupQueryFor(beanType);
  }

}
