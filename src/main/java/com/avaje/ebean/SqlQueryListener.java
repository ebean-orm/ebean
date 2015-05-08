package com.avaje.ebean;

/**
 * Provides a mechanism for processing a SqlQuery one SqlRow at a time.
 * <p>
 * This is useful when the query will return a large number of results and you
 * want to process the beans one at a time rather than have all of the beans in
 * memory at once.
 * </p>
 * 
 * <pre class="code">
 * SqlQueryListener listener = ...;
 *    
 * SqlQuery query  = Ebean.createSqlQuery(&quot;my.large.query&quot;);
 *    
 * // set the listener that will process each row one at a time
 * query.setListener(listener);
 *    
 * // execute the query. Note that the returned
 * // list will be empty ... so don't bother assigning it...
 * query.findList();
 * </pre>
 */
public interface SqlQueryListener {

  /**
   * Process the bean that has just been read.
   * <p>
   * Note this bean will not be added to the List Set or Map.
   * </p>
   */
  void process(SqlRow bean);
}
