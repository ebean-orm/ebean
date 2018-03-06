package io.ebeaninternal.server.core;

import io.ebeaninternal.api.SpiDtoQuery;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.api.SpiQuery;
import io.ebeaninternal.server.dto.DtoColumn;
import io.ebeaninternal.server.dto.DtoMappingRequest;
import io.ebeaninternal.server.dto.DtoQueryPlan;
import io.ebeaninternal.server.persist.Binder;
import io.ebeaninternal.server.query.dto.DtoQueryEngine;
import io.ebeaninternal.server.type.DataReader;
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

  private final SpiDtoQuery<T> query;

  private final DtoQueryEngine queryEngine;

  private DtoQueryPlan plan;

  private DataReader dataReader;

  private int beanCount;

  DtoQueryRequest(SpiEbeanServer server, DtoQueryEngine engine, SpiDtoQuery<T> query) {
    super(server, query, null);
    this.queryEngine = engine;
    this.query = query;
  }

  /**
   * Prepare and execute the SQL using the Binder.
   */
  @Override
  public void executeSql(Binder binder, SpiQuery.Type type) throws SQLException {
    SpiQuery<?> ormQuery = query.getOrmQuery();
    if (ormQuery != null) {
      ormQuery.setType(type);
      ormQuery.setManualId(true);

      // execute the underlying ORM query returning the ResultSet
      SpiResultSet result = server.findResultSet(ormQuery, trans);
      this.pstmt = result.getStatement();
      this.sql = ormQuery.getGeneratedSql();
      setResultSet(result.getResultSet(), ormQuery.getQueryPlanKey());

    } else {
      // native SQL query execution
      executeAsSql(binder);
    }
  }

  @Override
  protected void setResultSet(ResultSet resultSet, Object queryPlanKey) throws SQLException {
    this.resultSet = resultSet;
    this.dataReader = new RsetDataReader(server.getDataTimeZone(), resultSet);
    obtainPlan(queryPlanKey);
  }

  private void obtainPlan(Object planKey) throws SQLException {
    if (planKey == null) {
      planKey = query.planKey();
    }
    plan = query.getQueryPlan(planKey);
    if (plan == null) {
      plan = query.buildPlan(mappingRequest());
      query.putQueryPlan(planKey, plan);
    }
  }

  @Override
  protected void requestComplete() {
    if (plan != null) {
      long exeMicros = (System.nanoTime() - startNano) / 1000L;
      plan.collect(exeMicros, beanCount);
    }
  }

  public void findEach(Consumer<T> consumer) {
    queryEngine.findEach(this, consumer);
  }

  public void findEachWhile(Predicate<T> consumer) {
    queryEngine.findEachWhile(this, consumer);
  }

  public List<T> findList() {
    return queryEngine.findList(this);
  }

  @SuppressWarnings("unchecked")
  public T readNextBean() throws SQLException {
    beanCount++;
    dataReader.resetColumnPosition();
    return (T)plan.readRow(dataReader);
  }

  private DtoMappingRequest mappingRequest() throws SQLException {
    return new DtoMappingRequest(query, sql, readMeta());
  }

  private DtoColumn[] readMeta() throws SQLException {

    ResultSetMetaData metaData = resultSet.getMetaData();
    int cols = metaData.getColumnCount();
    DtoColumn[] meta = new DtoColumn[cols];
    for (int i = 0; i < cols; i++) {
      int pos = i+1;
      String columnLabel = metaData.getColumnLabel(pos);
      if (columnLabel == null) {
        columnLabel = metaData.getColumnName(pos);
      }
      meta[i] = new DtoColumn(columnLabel);
    }
    return meta;
  }

}
