package io.ebeaninternal.server.persist;

import io.ebeaninternal.api.BindParams;
import io.ebeaninternal.api.SpiSqlUpdate;
import io.ebeaninternal.api.SpiTransaction;
import io.ebeaninternal.server.core.PersistRequestUpdateSql;
import io.ebeaninternal.server.core.PersistRequestUpdateSql.SqlType;
import io.ebeaninternal.server.type.DataBind;
import io.ebeaninternal.server.util.BindParamsParser;

import javax.persistence.PersistenceException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Executes the UpdateSql requests.
 */
class ExeUpdateSql {

  private final Binder binder;

  private final PstmtFactory pstmtFactory;

  /**
   * Create with a given binder.
   */
  ExeUpdateSql(Binder binder, PstmtFactory pstmtFactory) {
    this.binder = binder;
    this.pstmtFactory = pstmtFactory;
  }

  /**
   * Execute the UpdateSql request.
   */
  public int execute(PersistRequestUpdateSql request) {

    boolean batchThisRequest = request.isBatchThisRequest();

    try (DataBind dataBind = bindStmt(request, batchThisRequest)){
      if (batchThisRequest) {
        dataBind.getPstmt().addBatch();
        // return -1 to indicate batch mode
        return -1;
      } else {
        try (PreparedStatement pstmt = dataBind.getPstmt()) {
          int rowCount = dataBind.executeUpdate();
          request.checkRowCount(rowCount);
          if (request.isGetGeneratedKeys()) {
            readGeneratedKeys(dataBind.getPstmt(), request);
          }
          request.postExecute();
          return rowCount;
        }
      }
    } catch (SQLException ex) {
      throw new PersistenceException(ex);
    }
  }

  private void readGeneratedKeys(PreparedStatement stmt, PersistRequestUpdateSql request) {

      try (ResultSet resultSet = stmt.getGeneratedKeys()){
        if (resultSet.next()) {
          request.setGeneratedKey(resultSet.getObject(1));
        }
      } catch (SQLException ex) {
        throw new PersistenceException(ex);
      }
  }

  private DataBind bindStmt(PersistRequestUpdateSql request, boolean batchThisRequest) throws SQLException {

    request.startBind(batchThisRequest);
    SpiSqlUpdate updateSql = request.getUpdateSql();
    SpiTransaction t = request.getTransaction();

    String sql = updateSql.getSql();

    BindParams bindParams = updateSql.getBindParams();

    // process named parameters if required
    sql = BindParamsParser.parse(bindParams, sql);
    updateSql.setGeneratedSql(sql);

    boolean logSql = request.isLogSql();

    DataBind dataBind;
    if (batchThisRequest) {
      dataBind = pstmtFactory.getBatchedPDataBind(t, logSql, sql, request);
    } else {
      dataBind = pstmtFactory.getPDataBind(t, logSql, sql, request.isGetGeneratedKeys());
    }

    if (updateSql.getTimeout() > 0) {
      dataBind.getPstmt().setQueryTimeout(updateSql.getTimeout());
    }

    String bindLog = null;
    if (!bindParams.isEmpty()) {
      bindLog = binder.bind(bindParams, dataBind);
    }

    request.setBindLog(bindLog);

    // derive the statement type (for TransactionEvent)
    parseUpdate(sql, request);
    return dataBind;
  }


  private void determineType(String word1, String word2, String word3, PersistRequestUpdateSql request) {

    if (word1.equalsIgnoreCase("UPDATE")) {
      request.setType(SqlType.SQL_UPDATE, word2, "UpdateSql");

    } else if (word1.equalsIgnoreCase("DELETE")) {
      request.setType(SqlType.SQL_DELETE, word3, "DeleteSql");

    } else if (word1.equalsIgnoreCase("INSERT")) {
      request.setType(SqlType.SQL_INSERT, word3, "InsertSql");

    } else {
      request.setType(SqlType.SQL_UNKNOWN, null, "UnknownSql");
    }
  }

  private void parseUpdate(String sql, PersistRequestUpdateSql request) {

    int[] pos = new int[3];
    int spaceCount = 0;

    int len = sql.length();
    for (int i = 0; i < len; i++) {
      char c = sql.charAt(i);
      if (Character.isWhitespace(c)) {
        pos[spaceCount] = i;
        spaceCount++;
        if (spaceCount > 2) {
          break;
        }
      }
    }

    if (spaceCount < 2) {
      // unknown so no automatic L2 cache invalidation performed (so it should instead)
      // be done explicitly via the server.externalModification() method
      request.setType(SqlType.SQL_UNKNOWN, null, "UnknownSql");

    } else {
      // try to determine if it is insert, update or delete and the table involved
      // such that we can automatically manage L2 cache
      String firstWord = sql.substring(0, pos[0]);
      String secWord = sql.substring(pos[0] + 1, pos[1]);
      String thirdWord;
      if (pos[2] == 0) {
        // there is nothing after the table name
        thirdWord = sql.substring(pos[1] + 1);
      } else {
        thirdWord = sql.substring(pos[1] + 1, pos[2]);
      }

      determineType(firstWord, secWord, thirdWord, request);
    }
  }

}
