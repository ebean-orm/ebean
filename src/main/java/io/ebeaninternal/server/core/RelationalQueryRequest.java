package io.ebeaninternal.server.core;

import io.ebean.RowConsumer;
import io.ebean.RowMapper;
import io.ebean.SqlQuery;
import io.ebean.SqlRow;
import io.ebean.Transaction;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.api.SpiSqlBinding;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Wraps the objects involved in executing a SqlQuery.
 */
public final class RelationalQueryRequest extends AbstractSqlQueryRequest {

  private final RelationalQueryEngine queryEngine;

  private String[] propertyNames;

  private int estimateCapacity;

  private int rows;

  /**
   * Create the BeanFindRequest.
   */
  RelationalQueryRequest(SpiEbeanServer server, RelationalQueryEngine engine, SqlQuery q, Transaction t) {
    super(server, (SpiSqlBinding) q, t);
    this.queryEngine = engine;
  }

  @Override
  protected void setResultSet(ResultSet resultSet, Object planKey) throws SQLException {
    this.resultSet = resultSet;
    this.propertyNames = getPropertyNames();
    // calculate the initialCapacity of the Map to reduce rehashing
    float initCap = (propertyNames.length) / 0.7f;
    this.estimateCapacity = (int) initCap + 1;
  }

  @Override
  protected void requestComplete() {
    String label = query.getLabel();
    if (label != null) {
      long exeMicros = (System.nanoTime() - startNano) / 1000L;
      queryEngine.collect(label, exeMicros, rows);
    }
  }

  boolean findEachRow(RowConsumer mapper) {
    queryEngine.findEachRow(this, mapper);
    return true;
  }

  <T> List<T> findListMapper(RowMapper<T> mapper) {
    return queryEngine.findListMapper(this, mapper);
  }

  <T> T findOneMapper(RowMapper<T> mapper) {
    return queryEngine.findOneMapper(this, mapper);
  }

  public <T> List<T> findSingleAttributeList(Class<T> cls) {
    return queryEngine.findSingleAttributeList(this, cls);
  }

  public <T> T findSingleAttribute(Class<T> cls) {
    return queryEngine.findSingleAttribute(this, cls);
  }

  public void findEach(Consumer<SqlRow> consumer) {
    queryEngine.findEach(this, consumer);
  }

  public void findEachWhile(Predicate<SqlRow> consumer) {
    queryEngine.findEach(this, consumer);
  }

  public List<SqlRow> findList() {
    return queryEngine.findList(this);
  }

  /**
   * Build the list of property names.
   */
  private String[] getPropertyNames() throws SQLException {

    ResultSetMetaData metaData = resultSet.getMetaData();

    int columnsPlusOne = metaData.getColumnCount() + 1;
    ArrayList<String> propNames = new ArrayList<>(columnsPlusOne - 1);
    for (int i = 1; i < columnsPlusOne; i++) {
      propNames.add(metaData.getColumnLabel(i));
    }
    return propNames.toArray(new String[propNames.size()]);
  }

  /**
   * Read and return the next SqlRow.
   */
  public SqlRow createNewRow() throws SQLException {

    rows++;

    SqlRow sqlRow = queryEngine.createSqlRow(estimateCapacity);
    int index = 0;
    for (String propertyName : propertyNames) {
      index++;
      Object value = resultSet.getObject(index);
      sqlRow.set(propertyName, value);
    }
    return sqlRow;
  }

  public void logSummary() {
    if (trans.isLogSummary()) {
      long micros = (System.nanoTime() - startNano) / 1000L;
      trans.logSummary("SqlQuery  rows[" + rows + "] micros[" + micros + "] bind[" + bindLog + "]");
    }
  }

  public ResultSet getResultSet() {
    return resultSet;
  }

  public void incrementRows() {
    rows++;
  }

  public <T> List<T> mapList(RowMapper<T> mapper) throws SQLException {

    List<T> list = new ArrayList<>();
    while (next()) {
      list.add(mapper.map(resultSet, rows++));
    }
    return list;
  }

  public <T> T mapOne(RowMapper<T> mapper) throws SQLException {
    if (!next()) {
      return null;
    } else {
      return mapper.map(resultSet, rows++);
    }
  }

  public void mapEach(RowConsumer consumer) throws SQLException {
    while (next()) {
      consumer.accept(resultSet, rows++);
    }
  }

}
