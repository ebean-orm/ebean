package io.ebeaninternal.server.query;

import io.ebean.SqlRow;
import io.ebeaninternal.server.core.Message;
import io.ebeaninternal.server.core.RelationalQueryEngine;
import io.ebeaninternal.server.core.RelationalQueryRequest;
import io.ebeaninternal.server.persist.Binder;

import javax.persistence.PersistenceException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Perform native sql fetches.
 */
public class DefaultRelationalQueryEngine implements RelationalQueryEngine {

  private final Binder binder;

  private final String dbTrueValue;

  private final boolean binaryOptimizedUUID;

  public DefaultRelationalQueryEngine(Binder binder, String dbTrueValue, boolean binaryOptimizedUUID) {
    this.binder = binder;
    this.dbTrueValue = dbTrueValue == null ? "true" : dbTrueValue;
    this.binaryOptimizedUUID = binaryOptimizedUUID;
  }

  @Override
  public SqlRow createSqlRow(int estimateCapacity) {
    return new DefaultSqlRow(estimateCapacity, 0.75f, dbTrueValue, binaryOptimizedUUID);
  }

  @Override
  public void findEach(RelationalQueryRequest request, Predicate<SqlRow> consumer) {

    try {
      request.executeSql(binder);
      while (request.next()) {
        if (!consumer.test(readRow(request))) {
          break;
        }
      }
      request.logSummary();

    } catch (Exception e) {
      throw new PersistenceException(Message.msg("fetch.error", e.getMessage(), request.getSql()), e);

    } finally {
      request.close();
    }
  }

  @Override
  public void findEach(RelationalQueryRequest request, Consumer<SqlRow> consumer) {

    try {
      request.executeSql(binder);
      while (request.next()) {
        consumer.accept(readRow(request));
      }
      request.logSummary();

    } catch (Exception e) {
      throw new PersistenceException(Message.msg("fetch.error", e.getMessage(), request.getSql()), e);

    } finally {
      request.close();
    }
  }

  @Override
  public List<SqlRow> findList(RelationalQueryRequest request) {

    try {
      request.executeSql(binder);
      List<SqlRow> rows = new ArrayList<>();
      while (request.next()) {
        rows.add(readRow(request));
      }

      request.logSummary();
      return rows;

    } catch (Exception e) {
      throw new PersistenceException(Message.msg("fetch.error", e.getMessage(), request.getSql()), e);

    } finally {
      request.close();
    }
  }

  /**
   * Read the row from the ResultSet and return as a MapBean.
   */
  private SqlRow readRow(RelationalQueryRequest request) throws SQLException {
    return request.createNewRow();
  }

}
