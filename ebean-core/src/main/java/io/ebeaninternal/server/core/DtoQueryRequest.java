package io.ebeaninternal.server.core;

import io.ebean.QueryIterator;
import io.ebean.core.type.DataReader;
import io.ebeaninternal.api.SpiDtoQuery;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.api.SpiQuery;
import io.ebeaninternal.server.dto.DtoColumn;
import io.ebeaninternal.server.dto.DtoMappingRequest;
import io.ebeaninternal.server.dto.DtoQueryPlan;
import io.ebeaninternal.server.persist.Binder;
import io.ebeaninternal.server.query.DtoQueryEngine;
import io.ebeaninternal.server.type.RsetDataReader;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Wraps the objects involved in executing a DtoQuery.
 */
public final class DtoQueryRequest<T> extends AbstractSqlQueryRequest {

  private static final String ENC_PREFIX = EncryptAlias.PREFIX;
  private static final String ENC_PREFIX_UPPER = EncryptAlias.PREFIX.toUpperCase();

  private final SpiDtoQuery<T> query;
  private final DtoQueryEngine queryEngine;
  private DtoQueryPlan plan;
  private DataReader dataReader;

  DtoQueryRequest(SpiEbeanServer server, DtoQueryEngine engine, SpiDtoQuery<T> query) {
    super(server, query);
    this.queryEngine = engine;
    this.query = query;
    query.obtainLocation();
  }

  /**
   * Prepare and execute the SQL using the Binder.
   */
  @Override
  public void executeSql(Binder binder, SpiQuery.Type type) throws SQLException {
    startNano = System.nanoTime();
    SpiQuery<?> ormQuery = query.ormQuery();
    if (ormQuery != null) {
      ormQuery.setType(type);
      ormQuery.setManualId();

      query.setCancelableQuery(ormQuery);
      // execute the underlying ORM query returning the ResultSet
      ormQuery.usingTransaction(transaction);
      SpiResultSet result = server.findResultSet(ormQuery);
      this.pstmt = result.statement();
      this.sql = ormQuery.getGeneratedSql();
      setResultSet(result.resultSet(), ormQuery.queryPlanKey());

    } else {
      // native SQL query execution
      executeAsSql(binder);
    }
  }

  @Override
  protected void setResultSet(ResultSet resultSet, Object queryPlanKey) throws SQLException {
    this.resultSet = resultSet;
    this.dataReader = new RsetDataReader(false, server.dataTimeZone(), resultSet);
    obtainPlan(queryPlanKey);
  }

  private void obtainPlan(Object planKey) throws SQLException {
    if (planKey == null) {
      planKey = query.planKey();
    }
    plan = query.queryPlan(planKey);
    if (plan == null) {
      plan = query.buildPlan(mappingRequest());
      query.putQueryPlan(planKey, plan);
    }
  }

  @Override
  protected void requestComplete() {
    if (plan != null) {
      long exeMicros = (System.nanoTime() - startNano) / 1000L;
      plan.collect(exeMicros);
    }
  }

  public QueryIterator<T> findIterate() {
    flushJdbcBatchOnQuery();
    return queryEngine.findIterate(this);
  }

  public void findEach(Consumer<T> consumer) {
    flushJdbcBatchOnQuery();
    queryEngine.findEach(this, consumer);
  }

  public void findEach(int batch, Consumer<List<T>> consumer) {
    flushJdbcBatchOnQuery();
    queryEngine.findEach(this, batch, consumer);
  }

  public void findEachWhile(Predicate<T> consumer) {
    flushJdbcBatchOnQuery();
    queryEngine.findEachWhile(this, consumer);
  }

  public List<T> findList() {
    flushJdbcBatchOnQuery();
    return queryEngine.findList(this);
  }

  @Override
  public boolean next() throws SQLException {
    query.checkCancelled();
    return dataReader.next();
  }

  @SuppressWarnings("unchecked")
  public T readNextBean() throws SQLException {
    return (T) plan.readRow(dataReader);
  }

  private DtoMappingRequest mappingRequest() throws SQLException {
    return new DtoMappingRequest(query, sql, readMeta());
  }

  private DtoColumn[] readMeta() throws SQLException {
    ResultSetMetaData metaData = resultSet.getMetaData();
    int cols = metaData.getColumnCount();
    DtoColumn[] meta = new DtoColumn[cols];
    for (int i = 0; i < cols; i++) {
      int pos = i + 1;
      String columnLabel = metaData.getColumnLabel(pos);
      if (columnLabel == null) {
        columnLabel = metaData.getColumnName(pos);
      }
      meta[i] = new DtoColumn(parseColumn(columnLabel));
    }
    return meta;
  }

  static String parseColumn(String columnLabel) {
    if (columnLabel.startsWith(ENC_PREFIX) || columnLabel.startsWith(ENC_PREFIX_UPPER)) {
      // encrypted column alias in the form _e_<tableAlias>_<column>
      final int pos = columnLabel.indexOf("_", 4);
      if (pos > -1) {
        return columnLabel.substring(pos + 1);
      }
    }
    return columnLabel;
  }

}
