package com.avaje.ebean.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.avaje.ebean.Query;

/**
 * Specify an explicit sql select statement to use for querying an entity bean.
 * <p>
 * The reason for using explicit sql is that you want better control over the
 * exact sql or sql that Ebean does not generate for you (such as group by,
 * union, intersection, window functions, recursive queries).
 * </p>
 * <p>
 * An example of two sql select queries deployed on the ReportTopic entity bean.
 * The first one has no name specified so it becomes the default query. The
 * second query extends the first adding a where clause with a named parameter.
 * </p>
 * 
 * <pre class="code">
 * ...
 * &#064;Entity
 *   &#064;Sql(select = {
 *     &#064;SqlSelect(query = 
 *       &quot;select t.id, t.title, count(p.id) as score &quot;+
 *       &quot;from f_topic t &quot;+
 *       &quot;join f_topic_post p on p.topic_id = t.id &quot;+
 *       &quot;group by t.id, t.title&quot;),
 *     &#064;SqlSelect(
 *       name = &quot;with.title&quot;,
 *       extend = &quot;default&quot;,
 *       debug = true,
 *       where = &quot;title like :likeTitle&quot;)
 *  })
 *  public class ReportTopic
 *    &#064;Id Integer id;
 *    String title;
 *    Double score;
 *    ...
 * </pre>
 * 
 * <p>
 * An example using the first "default" query.
 * </p>
 * 
 * <pre class="code">
 * 
 * List&lt;ReportTopic&gt; list =
 *     Ebean.find(ReportTopic.class)
 *         .having().gt(&quot;score&quot;, 0)
 *         .findList();
 * 
 * </pre>
 * 
 * <p>
 * The resulting sql, note the having clause has been added.
 * </p>
 * 
 * <pre class="code">
 * select t.id, t.title, count(p.id) as score 
 * from f_topic t join f_topic_post p on p.topic_id = t.id 
 * group by t.id, t.title  
 * having count(p.id) &gt; ?
 * </pre>
 * 
 * <p>
 * An example using the second query. Note the named parameter "likeTitle" must
 * be set.
 * </p>
 * 
 * <pre class="code">
 * List&lt;ReportTopic&gt; list =
 *     Ebean.find(ReportTopic.class, &quot;with.title&quot;)
 *         .set(&quot;likeTitle&quot;, &quot;a%&quot;)
 *         .findList();
 * </pre>
 * 
 * <p>
 * Ebean tries to parse the sql in the query to determine 4 things
 * <li>Location for inserting WHERE expressions (if required)</li>
 * <li>Location for inserting HAVING expressions (if required)</li>
 * <li>Mapping of columns to bean properties</li>
 * <li>The order by clause</li>
 * </p>
 * <p>
 * If Ebean is unable to parse out this information (perhaps because the sql
 * contains multiple select from keywords etc) then you need to manually specify
 * it.
 * </p>
 * <p>
 * Insert ${where} or ${andWhere} into the location where Ebean can insert any
 * expressions added to the where clause. Use ${andWhere} if the sql already has
 * the WHERE keyword and Ebean will instead start with a AND keyword.
 * </p>
 * <p>
 * Insert ${having} or ${andHaving} into the location where Ebean can insert any
 * expressions added to the having clause. Use ${andHaving} if the sql already
 * has a HAVING keyword and Ebean will instead start with a AND keyword.
 * </p>
 * <p>
 * Use the columnMapping property if Ebean is unable to determine the columns
 * and map them to bean properties.
 * </p>
 * <p>
 * Example with ${andWhere} & ${having}.
 * </p>
 * 
 * <pre class="code">
 *    &#064;SqlSelect(
 *          name = &quot;explicit.where&quot;,
 *          query = 
 *              &quot;select t.id, t.title, count(p.id) as score &quot;+
 *              &quot;from f_topic t, f_topic_post p &quot;+
 *              &quot;where p.topic_id = t.id ${andWhere} &quot;+
 *              &quot;group by t.id, t.title ${having}&quot;),
 * </pre>
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Deprecated
public @interface SqlSelect {

  /**
   * The name of the query. If left blank this is assumed to be the default
   * query for this bean type.
   * <p>
   * This will default to "default" and in that case becomes the default query
   * used for the bean.
   * </p>
   */
  String name() default "default";

  /**
   * The tableAlias used when adding where expressions to the query.
   */
  String tableAlias() default "";

  /**
   * The sql select statement.
   * <p>
   * If this query <em>extends</em> another then this string is appended to the
   * parent query string. Often when using <em>extend</em> you will leave the
   * query part blank and just specify a where and/or having clauses.
   * </p>
   * <p>
   * This sql <em>CAN NOT</em> contain named parameters. You have to put these
   * in the separate where and/or having sections.
   * </p>
   * <p>
   * Ebean automatically tries to determine the location in the sql string for
   * putting in additional where or having clauses. If Ebean is unable to
   * successfully determine this then you have to explicitly specify these
   * locations by including
   * <em>${where} or ${andWhere} and  ${having} or ${andHaving}</em> in the sql.
   * </p>
   * <p>
   * <b>${where}</b> location of where clause (and will add WHERE ... ) <br/>
   * Use this when there is no where clause in the sql. If expressions are added
   * to the where clause Ebean will put them in at this location starting with
   * the WHERE keyword.
   * <p>
   * <p>
   * <b>${andWhere}</b> <br/>
   * Use this instead of ${where} if there IS an existing where clause in the
   * sql. Ebean will add the expressions starting with the AND keyword.
   * <p>
   * <b>${having}</b> location of having clause (and will add HAVING... ) <br/>
   * </p>
   * <p>
   * <b>${andHaving}</b> <br/>
   * Use this instead of ${having} when there IS an existing HAVING clause.
   * Ebean will add the expressions starting with the AND keyword.
   * </p>
   * <p>
   * You can include one of ${where} OR ${andWhere} but not both.
   * </p>
   * <p>
   * You can include one of ${having} OR ${andHaving} but not both.
   * </p>
   */
  String query() default "";

  /**
   * Specify the name of a sql-select query that this one 'extends'.
   * <p>
   * When a query is extended the sql query contents are appended together. The
   * where and having clauses are NOT appended but overridden.
   * </p>
   */
  String extend() default "";

  /**
   * Specify a where clause typically containing named parameters.
   * <p>
   * If a where clause is specified with named parameters then they will need to
   * be set on the query via {@link Query#setParameter(String, Object)}.
   * </p>
   * <p>
   * In the example below the query specifies a where clause that includes a
   * named parameter "likeTitle".
   * </p>
   * 
   * <pre class="code">
   * ...
   * &#064;Entity
   * &#064;Sql(select = {
   *  ...
   *  &#064;SqlSelect(
   *  name = &quot;with.title&quot;,
   *  extend = &quot;default&quot;,
   *  debug = true,
   *  where = &quot;title like :likeTitle&quot;)
   *  })
   *  public class ReportTopic
   *  ...
   * </pre>
   * 
   * <p>
   * Example use of the above named query.
   * </p>
   * 
   * <pre class="code">
   * 
   * Query&lt;ReportTopic&gt; query0 = Ebean.createQuery(ReportTopic.class, &quot;with.title&quot;);
   * 
   * query0.set(&quot;likeTitle&quot;, &quot;Bana%&quot;);
   * 
   * List&lt;ReportTopic&gt; list0 = query0.findList();
   * </pre>
   * 
   */
  String where() default "";

  /**
   * Specify a having clause typically containing named parameters.
   * <p>
   * If a having clause is specified with named parameters then they will need
   * to be set on the query via {@link Query#setParameter(String, Object)}.
   * </p>
   */
  String having() default "";

  /**
   * (Optional) Explicitly specify column to property mapping.
   * <p>
   * This is required when Ebean is unable to parse the sql. This could occur if
   * the sql contains multiple select keywords etc.
   * </p>
   * <p>
   * Specify the columns and property names they map to in the format.
   * </p>
   * 
   * <pre class="code">
   *  column1 propertyName1, column2 propertyName2, ...
   * </pre>
   * 
   * <p>
   * Optionally put a AS keyword between the column and property.
   * </p>
   * 
   * <pre class="code">
   *   // the AS keyword is optional
   *  column1 AS propertyName1, column2 propertyName2, ...
   * </pre>
   * 
   * <p>
   * <b>column</b> should contain the table alias if there is one
   * </p>
   * <p>
   * <b>propertyName</b> should match the property name.
   * </p>
   * 
   * <p>
   * Example mapping 5 columns to properties.
   * </p>
   * 
   * <pre class="code">
   * columnMapping=&quot;t.id, t.bug_body description, t.bug_title as title, count(p.id) as scoreValue&quot;,
   * </pre>
   * 
   * <p>
   * Without this set Ebean will parse the sql looking for the select clause and
   * try to map the columns to property names. It is expected that Ebean will
   * not be able to successfully parse some sql and for those cases you should
   * specify the column to property mapping explicitly.
   * </p>
   * 
   */
  String columnMapping() default "";

  /**
   * Set this to true to have debug output when Ebean parses the sql-select.
   */
  boolean debug() default false;
}
