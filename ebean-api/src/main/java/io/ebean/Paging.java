package io.ebean;

import org.jspecify.annotations.Nullable;

/**
 * Used to specify Paging on a Query as an alternative to setting each of the
 * maxRows, firstRow and orderBy clause via:
 * {@link Query#setMaxRows(int)} + {@link Query#setFirstRow(int)} + {@link Query#setOrderBy(OrderBy)}.
 * <p>
 * Example use:
 *
 * <pre>{@code
 *
 *   var orderBy = OrderBy.of("lastName desc nulls first, firstName asc");
 *   var paging = Paging.of(0, 100, orderBy);
 *
 *   new QCustomer()
 *       .name.isNotNull()
 *       .setPaging(paging)
 *       .findList();
 *
 * }</pre>
 */
public interface Paging {

  /**
   * Create a Paging with the given page index size and orderBy.
   *
   * @param pageIndex the page index starting from zero
   * @param pageSize  the page size (effectively max rows)
   * @param orderBy   order by for the query result
   */
  static Paging of(int pageIndex, int pageSize, @Nullable OrderBy<?> orderBy) {
    return DPaging.build(pageIndex, pageSize, orderBy);
  }

  /**
   * Create a Paging with a raw order by clause.
   *
   * @param pageIndex     the page index starting from zero
   * @param pageSize      the page size (effectively max rows)
   * @param orderByClause raw order by clause for ordering the query result
   */
  static Paging of(int pageIndex, int pageSize, @Nullable String orderByClause) {
    return of(pageIndex, pageSize, OrderBy.of(orderByClause));
  }

  /**
   * Create a Paging that will use the id property for ordering.
   *
   * @param pageIndex the page index starting from zero
   * @param pageSize  the page size (effectively max rows)
   */
  static Paging of(int pageIndex, int pageSize) {
    return DPaging.build(pageIndex, pageSize);
  }

  /**
   * Return a Paging that will not apply any pagination to a query.
   */
  static Paging ofNone() {
    return DPaging.NONE;
  }

  /**
   * Return the page index.
   */
  int pageIndex();

  /**
   * Return the page size.
   */
  int pageSize();

  /**
   * Return the order by.
   */
  OrderBy<?> orderBy();

  /**
   * Return a Paging using the given page index.
   */
  Paging withPage(int pageIndex);

  /**
   * Return a Paging using the given order by clause.
   */
  Paging withOrderBy(String orderByClause);

}
