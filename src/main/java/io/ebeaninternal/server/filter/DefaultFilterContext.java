package io.ebeaninternal.server.filter;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

import io.ebeaninternal.api.filter.FilterContext;
import io.ebeaninternal.api.filter.FilterPermutations;

/**
 * The FilterContext is the root of the SQL-Tree and holds valueholders to support permutation checking.
 * <p>
 * It manges internally a state of all collections and knows, if there are any permutations, that are not
 * yet checked.
 * <p>
 * So if you have a customer, the customer has orders, the orders itself have orderDetails
 * <p>
 *
 * If you query for a product and perform the joins "customer" x "order" x "orderDetail" x "product",
 * you will get the following result on the BasicDataSet
 *
 * <pre>Ebean.filter(Customer.class)
 *   .eq("orders.details.product.name", "x2") will check that 'table'
 *
 * #  | CustomerName | OrderId | DetailId | ProductName
 * ---+--------------+---------+----------+------------
 *  1 | Rob          |       1 |        1 | Chair
 *  2 | Rob          |       1 |        2 | Desk
 *  2 | Rob          |       1 |        3 | Computer
 *  4 | Rob          |       3 |        5 | Chair
 *  5 | Rob          |       3 |        6 | Computer
 *  6 | Rob          |       3 |        7 | Chair
 *  7 | Rob          |       4 |  -null-  | -null-
 * </pre>
 *
 * So we have to perform 7 permutations in the worst case if we query for a certain product name.
 * <p>
 * If you check a second property, e.g. "contacts.firstName", you will get the cross product of
 * contacts x products. (there are 3 contacts assigned to  Rob: Jim1, Fred1, Bugs1, which means,
 * that there are 21 permutations)
 *
 * <pre>Ebean.filter(Customer.class)
 *   .eq("orders.details.product.name", "x2")
 *   .eq("contacts.firstName", "x3") will check that 'table'
 *
 * #  | CustomerName | OrderId | DetailId | ProductName | ContactName
 * ---+--------------+---------+----------+-------------+------------
 *  1 | Rob          |       1 |        1 | Chair       | Jim1
 *  2 | Rob          |       1 |        2 | Desk        | Jim1
 *  2 | Rob          |       1 |        3 | Computer    | Jim1
 *  4 | Rob          |       3 |        5 | Chair       | Jim1
 *  5 | Rob          |       3 |        6 | Computer    | Jim1
 *  6 | Rob          |       3 |        7 | Chair       | Jim1
 *  7 | Rob          |       4 |  -null-  | -null-      | Jim1
 *  8 | Rob          |       1 |        1 | Chair       | Fred1
 *                         .......
 * 14 | Rob          |       4 |  -null-  | -null-      | Fred1
 * 15 | Rob          |       1 |        1 | Chair       | Bugs1
 *                         .......
 * 21 | Rob          |       4 |  -null-  | -null-      | Bugs1
 *
 * </pre>
 *
 * <b>NOTE: This code does NO optimizations (yet), it will really check all permutations,
 * if you filter over OneToMany properties.
 *
 *
 *
 * @author Roland Praml, FOCONIS AG
 *
 */
public class DefaultFilterContext implements FilterContext {

  /**
   * Holds intermediate results when filtering a list. (e.g. results of subQueries)
   */
  private final Map<Object, Object> cache = new HashMap<>();

  /**
   * Each property has a valueHolder. in the example above, there would be one for "orders" and "contacts"
   */
  private final Map<String, DefaultFilterValueHolder> valueHolders = new LinkedHashMap<>();

  @Override
  @SuppressWarnings({ "unchecked" })
  public <C> C computeIfAbsent(Object key, Supplier<C> supplier) {
    return (C) cache.computeIfAbsent(key, k -> supplier.get());
  }

  /**
   * moves to the next permutation. Returns true, as long as there are more permutations to check.
   */
  public boolean nextPermutation() {
    for (DefaultFilterValueHolder vh : valueHolders.values()) {
      if (vh.nextPermutation()) {
        return true;
      }
    }
    return false;
  }

  public void reset() {
    valueHolders.clear();
  }

  @Override
  public FilterPermutations getFilterPermutations(String name, Collection<?> src) {
    DefaultFilterValueHolder vh = valueHolders.computeIfAbsent(name, k -> new DefaultFilterValueHolder(src));
    assert vh.src == src; // assert, that we got the correct holder
    return vh;
  }
}
