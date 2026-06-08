package io.ebeaninternal.server.dto;

import io.ebean.core.type.DataReader;
import io.ebean.meta.MetricVisitor;
import io.ebeaninternal.server.bind.capture.BindCapture;

import java.sql.SQLException;

/**
 * Knows how to read and map rows into a Bean.
 */
public interface DtoQueryPlan {

  /**
   * Read the row data and return the DTO bean.
   */
  Object readRow(DataReader dataReader) throws SQLException;

  /**
   * Add an event to the query execution statistics.
   */
  void collect(long exeMicros);

  /**
   * Return true if the bind values for this (native SQL) query should be
   * captured in order to later collect the database query plan.
   */
  boolean collectFor(long exeMicros);

  /**
   * Set the captured bind values used to later collect the database query plan.
   */
  void setBind(BindCapture bindCapture, long exeMicros, long startNanos);

  /**
   * Visit the metric (if not empty).
   */
  void visit(MetricVisitor visitor);
}
