package com.avaje.ebeaninternal.server.query;

import com.avaje.ebeaninternal.server.core.OrmQueryRequest;
import com.avaje.ebeaninternal.server.deploy.BeanProperty;
import com.avaje.ebeaninternal.server.type.ScalarType;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Executes the select row count query.
 */
public class CQueryFetchSingleAttribute extends CQueryFetchBase {

  private final BeanProperty property;

  private final ScalarType<Object> scalarType;

  /**
   * Create the Sql select based on the request.
   */
  public CQueryFetchSingleAttribute(OrmQueryRequest<?> request, CQueryPredicates predicates, CQueryPlan plan) {
    super(request, predicates, plan.getSql());
    this.property = plan.getSingleProperty();
    this.scalarType = property.getScalarType();
  }

  /**
   * Return a summary description of this query.
   */
  public String getSummary() {
    StringBuilder sb = new StringBuilder(80);
    sb.append("FindAttr exeMicros[").append(executionTimeMicros)
        .append("] rows[").append(rowCount)
        .append("] type[").append(desc.getName())
        .append("] predicates[").append(predicates.getLogWhereSql())
        .append("] bind[").append(bindLog).append("]");

    return sb.toString();
  }

  /**
   * Execute the query returning the row count.
   */
  public List<Object> findList() throws SQLException {

    long startNano = System.nanoTime();
    try {

      List<Object> result = new ArrayList<Object>();

      ResultSet rset = prepareExecute();
      while (rset.next()) {
        result.add(scalarType.read(dataReader));
        dataReader.resetColumnPosition();
        rowCount++;
      }

      long exeNano = System.nanoTime() - startNano;
      executionTimeMicros = (int) exeNano / 1000;

      return result;

    } finally {
      close();
    }
  }

}
