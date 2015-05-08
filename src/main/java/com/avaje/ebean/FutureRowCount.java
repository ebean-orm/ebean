package com.avaje.ebean;

import java.util.concurrent.Future;

/**
 * Represents the result of a background query execution for the total row count
 * for a query.
 * <p>
 * It extends the java.util.concurrent.Future.
 * </p>
 * 
 * @author rbygrave
 */
public interface FutureRowCount<T> extends Future<Integer> {
}
