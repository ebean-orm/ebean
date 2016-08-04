package com.avaje.ebeaninternal.server.query;

import com.avaje.ebeaninternal.api.BeanIdList;
import com.avaje.ebeaninternal.server.core.OrmQueryRequest;
import com.avaje.ebeaninternal.server.deploy.DbReadContext;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Executes the select row count query.
 */
public class CQueryFetchIds extends CQueryFetchBase {

  /**
   * Create the Sql select based on the request.
   */
  public CQueryFetchIds(OrmQueryRequest<?> request, CQueryPredicates predicates, String sql) {
    super(request, predicates, sql);
  }

  /**
   * Return a summary description of this query.
   */
  public String getSummary() {
    StringBuilder sb = new StringBuilder(80);
    sb.append("FindIds exeMicros[").append(executionTimeMicros)
        .append("] rows[").append(rowCount)
        .append("] type[").append(desc.getName())
        .append("] predicates[").append(predicates.getLogWhereSql())
        .append("] bind[").append(bindLog).append("]");

    return sb.toString();
  }

  /**
   * Execute the query returning the row count.
   */
  public BeanIdList findIds() throws SQLException {

    long startNano = System.nanoTime();

    try {
      // get the list that we are going to put the id's into.
      // This was already set so that it is available to be
      // read by other threads (it is a synchronised list)
      List<Object> idList = query.getIdList();
      if (idList == null) {
        // running in foreground thread (not FutureIds query)
        idList = Collections.synchronizedList(new ArrayList<Object>());
        query.setIdList(idList);
      }

      BeanIdList result = new BeanIdList(idList);

      ResultSet rset = prepareExecute();

      boolean hitMaxRows = false;
      boolean hasMoreRows = false;
      rowCount = 0;

      DbReadContext ctx = new DbContext();

      while (rset.next()) {
        Object idValue = desc.getIdBinder().read(ctx);
        idList.add(idValue);
        // reset back to 0
        dataReader.resetColumnPosition();
        rowCount++;

        if (maxRows > 0 && rowCount == maxRows) {
          hitMaxRows = true;
          hasMoreRows = rset.next();
          break;

        }
      }

      if (hitMaxRows) {
        result.setHasMore(hasMoreRows);
      }

      long exeNano = System.nanoTime() - startNano;
      executionTimeMicros = (int) exeNano / 1000;

      return result;

    } finally {
      close();
    }
  }

}
