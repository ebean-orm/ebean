package com.avaje.ebeaninternal.server.querydefn;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.SqlRow;
import com.avaje.ebeaninternal.api.BindParams;
import com.avaje.ebeaninternal.api.SpiSqlQuery;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Default implementation of SQuery - SQL Query.
 */
public class DefaultRelationalQuery implements SpiSqlQuery {

  private static final long serialVersionUID = -1098305779779591068L;

  private final transient EbeanServer server;

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

  public List<SqlRow> findList() {
    return server.findList(this, null);
  }

  public SqlRow findUnique() {
    return server.findUnique(this, null);
  }

  public DefaultRelationalQuery setParameter(int position, Object value) {
    bindParams.setParameter(position, value);
    return this;
  }

  public DefaultRelationalQuery setParameter(String paramName, Object value) {
    bindParams.setParameter(paramName, value);
    return this;
  }

  public String toString() {
    return "SqlQuery [" + query + "]";
  }

  public int getFirstRow() {
    return firstRow;
  }

  public DefaultRelationalQuery setFirstRow(int firstRow) {
    this.firstRow = firstRow;
    return this;
  }

  public int getMaxRows() {
    return maxRows;
  }

  public DefaultRelationalQuery setMaxRows(int maxRows) {
    this.maxRows = maxRows;
    return this;
  }

  public int getTimeout() {
    return timeout;
  }

  public DefaultRelationalQuery setTimeout(int secs) {
    this.timeout = secs;
    return this;
  }

  public BindParams getBindParams() {
    return bindParams;
  }

  public DefaultRelationalQuery setBufferFetchSizeHint(int bufferFetchSizeHint) {
    this.bufferFetchSizeHint = bufferFetchSizeHint;
    return this;
  }

  public int getBufferFetchSizeHint() {
    return bufferFetchSizeHint;
  }

  public String getQuery() {
    return query;
  }

}
