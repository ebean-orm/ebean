package io.ebeaninternal.server.querydefn;

import io.ebean.RowConsumer;
import io.ebean.RowMapper;
import io.ebean.SqlQuery;
import io.ebean.SqlRow;
import io.ebeaninternal.api.BindParams;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.api.SpiSqlQuery;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Default implementation of SQuery - SQL Query.
 */
public final class DefaultRelationalQuery extends AbstractQuery implements SpiSqlQuery {

  private static final long serialVersionUID = -1098305779779591068L;

  private final transient SpiEbeanServer server;
  private final String query;
  private String label;
  private int firstRow;
  private int maxRows;
  private int timeout;
  private int bufferFetchSizeHint;
  private final BindParams bindParams = new BindParams();

  /**
   * Additional supply a query detail object.
   */
  public DefaultRelationalQuery(SpiEbeanServer server, String query) {
    this.server = server;
    this.query = query;
  }

  @Override
  public void findEach(Consumer<SqlRow> consumer) {
    server.findEach(this, consumer, null);
  }

  @Override
  public void findEachWhile(Predicate<SqlRow> consumer) {
    server.findEachWhile(this, consumer, null);
  }

  @Override
  public List<SqlRow> findList() {
    return server.findList(this, null);
  }

  @Override
  public BigDecimal findSingleDecimal() {
    return server.findSingleAttribute(this, BigDecimal.class);
  }

  @Override
  public Long findSingleLong() {
    return server.findSingleAttribute(this, Long.class);
  }

  @Override
  public <T> T findSingleAttribute(Class<T> cls) {
    return server.findSingleAttribute(this, cls);
  }

  @Override
  public <T> List<T> findSingleAttributeList(Class<T> cls) {
    return server.findSingleAttributeList(this, cls);
  }

  @Override
  public <T> T findOne(RowMapper<T> mapper) {
    return server.findOneMapper(this, mapper);
  }

  @Override
  public <T> List<T> findList(RowMapper<T> mapper) {
    return server.findListMapper(this, mapper);
  }

  @Override
  public void findEachRow(RowConsumer consumer) {
    server.findEachRow(this, consumer);
  }

  @Override
  public SqlRow findOne() {
    return server.findOne(this, null);
  }

  @Override
  public Optional<SqlRow> findOneOrEmpty() {
    return Optional.ofNullable(findOne());
  }

  @Override
  @Deprecated
  public DefaultRelationalQuery setParams(Object... values) {
    return setParameters(values);
  }

  @Override
  public DefaultRelationalQuery setParameters(Object... values) {
    bindParams.setNextParameters(values);
    return this;
  }

  @Override
  public DefaultRelationalQuery setParameter(Object value) {
    bindParams.setNextParameter(value);
    return this;
  }

  @Override
  public DefaultRelationalQuery setParameter(int position, Object value) {
    bindParams.setParameter(position, value);
    return this;
  }

  @Override
  public DefaultRelationalQuery setParameter(String paramName, Object value) {
    bindParams.setParameter(paramName, value);
    return this;
  }

  @Override
  public String toString() {
    return "SqlQuery [" + query + "]";
  }

  @Override
  public int getFirstRow() {
    return firstRow;
  }

  @Override
  public DefaultRelationalQuery setFirstRow(int firstRow) {
    this.firstRow = firstRow;
    return this;
  }

  @Override
  public int getMaxRows() {
    return maxRows;
  }

  @Override
  public DefaultRelationalQuery setMaxRows(int maxRows) {
    this.maxRows = maxRows;
    return this;
  }

  @Override
  public int getTimeout() {
    return timeout;
  }

  @Override
  public DefaultRelationalQuery setTimeout(int secs) {
    this.timeout = secs;
    return this;
  }

  @Override
  public String getLabel() {
    return label;
  }

  @Override
  public DefaultRelationalQuery setLabel(String label) {
    this.label = label;
    return this;
  }

  @Override
  public BindParams getBindParams() {
    return bindParams;
  }

  @Override
  public DefaultRelationalQuery setBufferFetchSizeHint(int bufferFetchSizeHint) {
    this.bufferFetchSizeHint = bufferFetchSizeHint;
    return this;
  }

  @Override
  public int getBufferFetchSizeHint() {
    return bufferFetchSizeHint;
  }

  @Override
  public String getQuery() {
    return query;
  }

  private <T> T mapperFindOne(RowMapper<T> mapper) {
    return server.findOneMapper(this, mapper);
  }

  private <T> List<T> mapperFindList(RowMapper<T> mapper) {
    return server.findListMapper(this, mapper);
  }

  private <T> void mapperFindEach(RowMapper<T> mapper, Consumer<T> consumer) {
    server.findEachRow(this, (resultSet, rowNum) -> consumer.accept(mapper.map(resultSet, rowNum)));
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  @Override
  public <T> TypeQuery<T> mapToScalar(Class<T> attributeType) {
    return new Scalar(attributeType);
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  @Override
  public <T> TypeQuery<T> mapTo(RowMapper<T> mapper) {
    return new Mapper(mapper);
  }

  private class Scalar<T> implements SqlQuery.TypeQuery<T> {

    private final Class<T> type;

    Scalar(Class<T> type) {
      this.type = type;
    }

    @Override
    public T findOne() {
      return findSingleAttribute(type);
    }

    @Override
    public Optional<T> findOneOrEmpty() {
      return Optional.ofNullable(findOne());
    }

    @Override
    public List<T> findList() {
      return findSingleAttributeList(type);
    }

    @Override
    public void findEach(Consumer<T> consumer) {
      scalarFindEach(type, consumer);
    }
  }

  private <T> void scalarFindEach(Class<T> type, Consumer<T> consumer) {
    server.findSingleAttributeEach(this, type, consumer);
  }

  private class Mapper<T> implements SqlQuery.TypeQuery<T> {

    private final RowMapper<T> mapper;

    Mapper(RowMapper<T> mapper) {
      this.mapper = mapper;
    }

    @Override
    public T findOne() {
      return mapperFindOne(mapper);
    }

    @Override
    public Optional<T> findOneOrEmpty() {
      return Optional.ofNullable(findOne());
    }

    @Override
    public List<T> findList() {
      return mapperFindList(mapper);
    }

    @Override
    public void findEach(Consumer<T> consumer) {
      mapperFindEach(mapper, consumer);
    }
  }
}
