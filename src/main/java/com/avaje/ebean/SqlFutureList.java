package com.avaje.ebean;

import java.util.List;
import java.util.concurrent.Future;

/**
 * The SqlFutureList represents the result of a background SQL query execution.
 * 
 * <p>
 * It extends the java.util.concurrent.Future.
 * </p>
 * 
 * <pre class="code">
 *  // create a query
 * String sql = ... ;
 * SqlQuery sqlQuery = Ebean.createSqlQuery(sql);
 * 
 *  // execute the query in a background thread
 * SqlFutureList sqlFuture = sqlQuery.findFutureList();
 * 
 *  // do something else ... we will sleep
 * Thread.sleep(3000);
 * System.out.println("end of sleep");
 * 
 * if (!futureList.isDone()){
 * 	// we can cancel the query execution
 * 	futureList.cancel(true);
 * }
 * 
 * System.out.println("and... done:"+futureList.isDone());
 * 
 * if (!futureList.isCancelled()){
 * 	// wait for the query to finish and return the list
 * 	List&lt;SqlRow&gt; list = futureList.get();
 * 	System.out.println("list:"+list);
 * }
 * 
 * </pre>
 * 
 * @author rob
 * 
 */
public interface SqlFutureList extends Future<List<SqlRow>> {

  SqlQuery getQuery();

}
