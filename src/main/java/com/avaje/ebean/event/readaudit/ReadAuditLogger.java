package com.avaje.ebean.event.readaudit;

/**
 * Log that the query was executed
 */
public interface ReadAuditLogger {

  /**
   * Called when a new query plan is created.
   * <p>
   * The query plan has the full sql and logging the query plan separately means that each of
   * the bean and many read events can log the query plan key and not the full sql (reducing the
   * bulk size of the read audit logs).
   * </p>
   */
  void queryPlan(ReadAuditQueryPlan queryPlan);

  /**
   * Audit a find bean query that returned a bean.
   * <p>
   * Finds that did not return a bean are excluded.
   * </p>
   */
  void auditBean(ReadEvent readBean);

  /**
   * Audit a find many query that returned some beans.
   * <p>
   * Finds that did not return any beans are excluded.
   * </p>
   * <p>
   * For large queries executed via findEach() etc the ids are collected in batches
   * and logged. Hence the ids list has a maximum size of the batch size.
   * </p>
   */
  void auditMany(ReadEvent readMany);
}
