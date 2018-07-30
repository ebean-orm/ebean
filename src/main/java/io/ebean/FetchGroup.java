package io.ebean;

import javax.annotation.Nonnull;

/**
 * Defines what part of the object graph to load (select and fetch clauses).
 * <p>
 * Using a FetchGroup effectively sets the select() and fetch() clauses for a query. It is alternative
 * to specifying the select() and fetch() clauses on the query allowing for more re-use of "what to load"
 * that can be defined separately from the query and combined with other FetchGroups.
 * </p>
 *
 * <h3>Select example</h3>*
 * <pre>{@code
 *
 * FetchGroup fetchGroup = FetchGroup.of("name, status");
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
 * FetchGroup fetchGroup = FetchGroup
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
 *  FetchGroup FG_ADDRESS = FetchGroup
 *    .select("line1, line2, city")
 *    .fetch("country", "name")
 *    .build();
 *
 *  FetchGroup FG_CUSTOMER = FetchGroup
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
 */
public interface FetchGroup {

  /**
   * Return the FetchGroup with the given select clause.
   * <p>
   *   We use this for simple FetchGroup that only select() properties and do not have additional fetch() clause.
   * </p>
   * <pre>{@code
   *
   * FetchGroup fetchGroup = FetchGroup.of("name, status");
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
  @Nonnull
  static FetchGroup of(String select) {
    return XServiceProvider.fetchGroupOf(select);
  }

  /**
   * Return the FetchGroupBuilder with the given select clause that we can add fetch clauses to.
   * <p>
   * We chain select() with one or more fetch() clauses to define the object graph to load.
   * </p>
   * <pre>{@code
   *
   * FetchGroup fetchGroup = FetchGroup
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
   * @param select The select clause for the FetchGroup
   *
   * @return The FetchGroupBuilder with the given select clause which we will add fetch clauses to
   */
  @Nonnull
  static FetchGroupBuilder select(String select) {
    return XServiceProvider.fetchGroupSelect(select);
  }

}
