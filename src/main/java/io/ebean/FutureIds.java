package io.ebean;

import java.util.List;
import java.util.concurrent.Future;

/**
 * FutureIds represents the result of a background query execution for the Id's.
 * <p>
 * It extends the java.util.concurrent.Future with the ability to get the Id's
 * while the query is still executing in the background.
 * </p>
 */
public interface FutureIds<T> extends Future<List<Object>> {

  /**
   * Returns the original query used to fetch the Id's.
   */
  Query<T> getQuery();

}
