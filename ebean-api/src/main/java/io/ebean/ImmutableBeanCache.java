package io.ebean;

import org.jspecify.annotations.NullMarked;
import java.util.Map;
import java.util.Set;

/**
 * Query-scoped immutable bean cache.
 *
 * <p>Typical use is to attach an immutable cache to a query and let Ebean use it when
 * resolving assoc-one references.
 *
 * <pre>{@code
 * FetchGroup<Customer> customerGroup = FetchGroup.of(Customer.class)
 *   .select("name,version")
 *   .fetch("billingAddress", "line1,city")
 *   .fetch("shippingAddress", "line1,city")
 *   .build();
 *
 * ImmutableBeanCache<Customer> customerCache = ImmutableBeanCaches.builder(Customer.class)
 *   .loading(database, customerGroup)
 *   .build();
 *
 * Order order = database.find(Order.class)
 *   .setId(id)
 *   .setUnmodifiable(true)
 *   .using(customerCache)
 *   .findOne();
 * }</pre>
 *
 * @param <T> The bean type.
 *
 * @see ImmutableBeanCaches#builder(Class)
 */
@NullMarked
public interface ImmutableBeanCache<T> {

  /**
   * Return the bean type this cache provides values for.
   */
  Class<T> type();

  /**
   * Return immutable cached beans by id (loading and populating misses as needed).
   */
  Map<Object, T> getAll(Set<Object> ids);
}
