package com.avaje.ebean;

/**
 * Represents a Conjunction or a Disjunction.
 * <p>
 * Basically with a Conjunction you join together many expressions with AND, and
 * with a Disjunction you join together many expressions with OR.
 * </p>
 * <p>
 * Note: where() always takes you to the top level WHERE expression list.
 * </p>
 * 
 * <pre class="code">
 * Query q =
 *     Ebean.find(Person.class)
 *         .where().disjunction()
 *         .like(&quot;name&quot;, &quot;Rob%&quot;)
 *         .eq(&quot;status&quot;, Status.NEW)
 * 
 *         // where() returns us to the top level expression list
 *         .where().gt(&quot;id&quot;, 10);
 * 
 * // read as...
 * // where ( ((name like Rob%) or (status = NEW)) AND (id &gt; 10) )
 * </pre>
 * 
 * <p>
 * Note: endJunction() takes you to the parent expression list
 * </p>
 * 
 * <pre class="code">
 * Query q =
 *     Ebean.find(Person.class)
 *         .where().disjunction()
 *         .like(&quot;name&quot;, &quot;Rob%&quot;)
 *         .eq(&quot;status&quot;, Status.NEW)
 *         .endJunction()
 * 
 *         // endJunction().. takes us to the 'parent' expression list
 *         // which in this case is the top level (same as where())
 * 
 *         .gt(&quot;id&quot;, 10);
 * 
 * // read as...
 * // where ( ((name like Rob%) or (status = NEW)) AND (id &gt; 10) )
 * </pre>
 * 
 * <p>
 * Example of a nested disjunction.
 * </p>
 * 
 * <pre class="code">
 * Query&lt;Customer&gt; q = 
 *  Ebean.find(Customer.class)
 *      .where()
 *          .disjunction()
 *              .conjunction()
 *                  .startsWith(&quot;name&quot;, &quot;r&quot;)
 *                  .eq(&quot;anniversary&quot;, onAfter)
 *                  .endJunction()
 *              .conjunction()
 *                  .eq(&quot;status&quot;, Customer.Status.ACTIVE)
 *                  .gt(&quot;id&quot;, 0)
 *                  .endJunction()
 *      .order().asc(&quot;name&quot;);
 * 
 * q.findList();
 * String s = q.getGeneratedSql();
 * 
 *  // this produces an expression like:
 *  
 *  ( name like ? and c.anniversary = ? ) or (c.status = ?  and c.id &gt; ? )
 * 
 * </pre>
 */
public interface Junction<T> extends Expression, ExpressionList<T> {

}
