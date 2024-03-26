package io.ebean.config;

import io.ebean.ProfileLocation;
import io.ebean.bean.ObjectGraphNode;

import java.util.List;

/**
 * The data for the slow query.
 */
public interface SlowQueryEvent {

  /**
   * Return the SQL for the slow query.
   */
  String getSql();

  /**
   * Return the execution time in millis.
   */
  long getTimeMillis();

  /**
   * Return the total row count associated with the query.
   */
  int getRowCount();

  /**
   * Return the origin point for the root query.
   * <p>
   * Typically the <code>originNode.getOriginQueryPoint().getFirstStackElement()</code> provides the stack line that
   * shows the code that invoked the query.
   * </p>
   */
  ObjectGraphNode getOriginNode();

  /**
   * Return the bind parameters.
   */
  List<Object> getBindParams();

  /**
   * Return the label.
   */
  String getLabel();

  /**
   * Return the profile location.
   */
  ProfileLocation getProfileLocation();
}
