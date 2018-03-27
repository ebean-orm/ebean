package io.ebeaninternal.server.querydefn;

import io.ebean.EbeanServer;
import io.ebean.SqlRow;
import io.ebeaninternal.api.BindParams;
import io.ebeaninternal.api.SpiSqlQuery;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Default implementation of SQuery - SQL Query.
 */
public class DefaultRelationalQuery implements SpiSqlQuery {

  private static final long serialVersionUID = -1098305779779591068L;

  private final transient EbeanServer server;

  private String label;

  private String query;

  private int firstRow;

  private int maxRows;

  private int timeout;

  private int bufferFetchSizeHint;

  /**
   * Bind parameters when using the query language.
   */
  private final BindParams bindParams = new BindParams();

  /**
   * Additional supply a query detail object.
   */
  public DefaultRelationalQuery(EbeanServer server, String query) {
    this.server = server;
    this.query = query;
  }

  public DefaultRelationalQuery setQuery(String query) {
    this.query = query;
    return this;
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
  public SqlRow findOne() {
    return server.findOne(this, null);
  }

  @Override
  public Optional<SqlRow> findOneOrEmpty() {
    return Optional.ofNullable(findOne());
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

}
