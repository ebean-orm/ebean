package io.ebeaninternal.server.core;

import io.ebean.*;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.api.SpiSqlQuery;

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

  RelationalQueryRequest(SpiEbeanServer server, RelationalQueryEngine engine, SpiSqlQuery q) {
    super(server, q);
    this.queryEngine = engine;
  }

  @Override
  protected void setResultSet(ResultSet resultSet, Object planKey) throws SQLException {
    this.resultSet = resultSet;
    this.propertyNames = propertyNames();
    // calculate the initialCapacity of the Map to reduce rehashing
    float initCap = (propertyNames.length) / 0.7f;
    this.estimateCapacity = (int) initCap + 1;
  }

  @Override
  protected void requestComplete() {
    String label = query.getLabel();
    if (label != null) {
      long exeMicros = (System.nanoTime() - startNano) / 1000L;
      queryEngine.collect(label, exeMicros);
    }
  }

  boolean findEachRow(RowConsumer mapper) {
    flushJdbcBatchOnQuery();
    queryEngine.findEach(this, mapper);
    return true;
  }

  <T> List<T> findListMapper(RowMapper<T> mapper) {
    flushJdbcBatchOnQuery();
    return queryEngine.findList(this, () -> mapper.map(resultSet, rows++));
  }

  <T> T findOneMapper(RowMapper<T> mapper) {
    flushJdbcBatchOnQuery();
    return queryEngine.findOne(this, mapper);
  }

  public <T> boolean findSingleAttributeEach(Class<T> cls, Consumer<T> consumer) {
    flushJdbcBatchOnQuery();
    queryEngine.findSingleAttributeEach(this, cls, consumer);
    return true;
  }

  public <T> List<T> findSingleAttributeList(Class<T> cls) {
    flushJdbcBatchOnQuery();
    return queryEngine.findSingleAttributeList(this, cls);
  }

  public <T> T findSingleAttribute(Class<T> cls) {
    flushJdbcBatchOnQuery();
    return queryEngine.findSingleAttribute(this, cls);
  }

  public void findEach(Consumer<SqlRow> consumer) {
    flushJdbcBatchOnQuery();
    queryEngine.findEach(this, (resultSet, rowNum) -> consumer.accept(createNewRow()));
  }

  public void findEachWhile(Predicate<SqlRow> consumer) {
    flushJdbcBatchOnQuery();
    queryEngine.findEach(this, this::createNewRow, consumer);
  }

  public List<SqlRow> findList() {
    flushJdbcBatchOnQuery();
    return queryEngine.findList(this, this::createNewRow);
  }

  /**
   * Build the list of property names.
   */
  private String[] propertyNames() throws SQLException {
    ResultSetMetaData metaData = resultSet.getMetaData();
    int columnsPlusOne = metaData.getColumnCount() + 1;
    ArrayList<String> propNames = new ArrayList<>(columnsPlusOne - 1);
    for (int i = 1; i < columnsPlusOne; i++) {
      propNames.add(metaData.getColumnLabel(i));
    }
    return propNames.toArray(new String[0]);
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
    if (transaction.isLogSummary()) {
      long micros = (System.nanoTime() - startNano) / 1000L;
      transaction.logSummary("SqlQuery  rows[{0}] micros[{1}] bind[{2}]", rows, micros, bindLog);
    }
  }

  public ResultSet resultSet() {
    return resultSet;
  }

  @Override
  public boolean next() throws SQLException {
    query.checkCancelled();
    if (!resultSet.next()) {
      return false;
    } else {
      rows++;
      return true;
    }
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
