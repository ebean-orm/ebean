package com.avaje.ebean;

/**
 * Query by Example expression.
 * <p>
 * Pass in an example entity and for each non-null scalar properties an
 * expression is added.
 * </p>
 * <p>
 * By Default this case sensitive, will ignore numeric zero values and will use
 * a Like for string values (you must put in your own wildcards).
 * </p>
 * <p>
 * To get control over the options you can create an ExampleExpression and set
 * those options such as case insensitive etc.
 * </p>
 * 
 * <pre class="code">
 * // create an example bean and set the properties
 * // with the query parameters you want
 * Customer example = new Customer();
 * example.setName(&quot;Rob%&quot;);
 * example.setNotes(&quot;%something%&quot;);
 * 
 * List&lt;Customer&gt; list =
 *     Ebean.find(Customer.class)
 *         .where()
 *         // pass the bean into the where() clause
 *         .exampleLike(example)
 *         // you can add other expressions to the same query
 *         .gt(&quot;id&quot;, 2)
 *         .findList();
 * 
 * </pre>
 * 
 * Similarly you can create an ExampleExpression
 * 
 * <pre>
 * Customer example = new Customer();
 * example.setName(&quot;Rob%&quot;);
 * example.setNotes(&quot;%something%&quot;);
 * 
 * // create a ExampleExpression with more control
 * ExampleExpression qbe = new ExampleExpression(example, true, LikeType.EQUAL_TO)
 *     .includeZeros();
 * 
 * List&lt;Customer&gt; list =
 *     Ebean.find(Customer.class)
 *         .where()
 *         .add(qbe)
 *         .findList();
 * </pre>
 * 
 * @author Rob Bygrave
 */
public interface ExampleExpression extends Expression {

  /**
   * By calling this method zero value properties are going to be included in
   * the expression.
   * <p>
   * By default numeric zero values are excluded as they can result from
   * primitive int and long types.
   * </p>
   */
  ExampleExpression includeZeros();

  /**
   * Set case insensitive to true.
   */
  ExampleExpression caseInsensitive();

  /**
   * Use startsWith expression for string properties.
   */
  ExampleExpression useStartsWith();

  /**
   * Use contains expression for string properties.
   */
  ExampleExpression useContains();

  /**
   * Use endsWith expression for string properties.
   */
  ExampleExpression useEndsWith();

  /**
   * Use equal to expression for string properties.
   */
  ExampleExpression useEqualTo();

}