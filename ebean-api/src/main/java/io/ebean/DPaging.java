package io.ebean;

final class DPaging implements Paging {

  static final Paging NONE = new DPaging(0, 0, null);

  static Paging build(int pgIndex, int pgSize, OrderBy<?> orderBy) {
    return new DPaging(pgIndex, pgSize, orderBy);
  }

  static Paging build(int pgIndex, int pgSize) {
    return new DPaging(pgIndex, pgSize, null);
  }

  private final int pageNumber;
  private final int pageSize;
  private final OrderBy<?> orderBy;

  DPaging(int pageNumber, int pageSize, OrderBy<?> orderBy) {
    this.pageNumber = pageNumber;
    this.pageSize = pageSize;
    this.orderBy = orderBy;
  }

  @Override
  public int pageIndex() {
    return pageNumber;
  }

  @Override
  public int pageSize() {
    return pageSize;
  }

  @Override
  public OrderBy<?> orderBy() {
    return orderBy;
  }

  @Override
  public Paging withPage(int pageNumber) {
    return new DPaging(pageNumber, pageSize, orderBy);
  }

  @Override
  public Paging withOrderBy(String orderByClause) {
    return new DPaging(pageNumber, pageSize, OrderBy.of(orderByClause));
  }

}
