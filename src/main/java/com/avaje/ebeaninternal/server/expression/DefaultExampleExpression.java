package com.avaje.ebeaninternal.server.expression;

import java.util.ArrayList;

import com.avaje.ebean.ExampleExpression;
import com.avaje.ebean.LikeType;
import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebean.event.BeanQueryRequest;
import com.avaje.ebeaninternal.api.HashQueryPlanBuilder;
import com.avaje.ebeaninternal.api.ManyWhereJoins;
import com.avaje.ebeaninternal.api.SpiExpression;
import com.avaje.ebeaninternal.api.SpiExpressionRequest;
import com.avaje.ebeaninternal.server.core.OrmQueryRequest;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.deploy.BeanProperty;

/**
 * A "Query By Example" type of expression.
 * <p>
 * Pass in an example entity and for each non-null scalar properties an
 * expression is added.
 * </p>
 * 
 * <pre class="code">
 * // create an example bean and set the properties
 * // with the query parameters you want
 * Customer example = new Customer();
 * example.setName(&quot;Rob%&quot;);
 * example.setNotes(&quot;%something%&quot;);
 * 
 * List&lt;Customer&gt; list = Ebean.find(Customer.class).where()
 * // pass the bean into the where() clause
 *     .exampleLike(example)
 *     // you can add other expressions to the same query
 *     .gt(&quot;id&quot;, 2).findList();
 * 
 * </pre>
 */
public class DefaultExampleExpression implements SpiExpression, ExampleExpression {

  private static final long serialVersionUID = 1L;

  /**
   * The example bean containing the properties.
   */
  private final EntityBean entity;

  /**
   * Set to true to use case insensitive expressions.
   */
  private boolean caseInsensitive;

  /**
   * The type of like (RAW, STARTS_WITH, ENDS_WITH etc)
   */
  private LikeType likeType;

  /**
   * By default zeros are excluded.
   */
  private boolean includeZeros;

  /**
   * The non null bean properties and found and together added as a list of
   * expressions (like or equal to expressions).
   */
  private ArrayList<SpiExpression> list;


  /**
   * Construct the query by example expression.
   * 
   * @param entity
   *          the example entity with non null property values
   * @param caseInsensitive
   *          if true use case insensitive expressions
   * @param likeType
   *          the type of Like wild card used
   */
  public DefaultExampleExpression(EntityBean entity, boolean caseInsensitive, LikeType likeType) {
    this.entity = entity;
    this.caseInsensitive = caseInsensitive;
    this.likeType = likeType;
  }

  public void containsMany(BeanDescriptor<?> desc, ManyWhereJoins whereManyJoins) {
    if (list != null) {
      for (int i = 0; i < list.size(); i++) {
        list.get(i).containsMany(desc, whereManyJoins);
      }
    }
  }

  public ExampleExpression includeZeros() {
    includeZeros = true;
    return this;
  }

  public ExampleExpression caseInsensitive() {
    caseInsensitive = true;
    return this;
  }

  public ExampleExpression useStartsWith() {
    likeType = LikeType.STARTS_WITH;
    return this;
  }

  public ExampleExpression useContains() {
    likeType = LikeType.CONTAINS;
    return this;
  }

  public ExampleExpression useEndsWith() {
    likeType = LikeType.ENDS_WITH;
    return this;
  }

  public ExampleExpression useEqualTo() {
    likeType = LikeType.EQUAL_TO;
    return this;
  }

  /**
   * This will return null for this example expression.
   */
  public String getPropertyName() {
    return null;
  }

  /**
   * Adds bind values to the request.
   */
  public void addBindValues(SpiExpressionRequest request) {

    for (int i = 0; i < list.size(); i++) {
      SpiExpression item = list.get(i);
      item.addBindValues(request);
    }
  }

  /**
   * Generates and adds the sql to the request.
   */
  public void addSql(SpiExpressionRequest request) {

    if (!list.isEmpty()) {
      request.append("(");

      for (int i = 0; i < list.size(); i++) {
        SpiExpression item = list.get(i);
        if (i > 0) {
          request.append(" and ");
        }
        item.addSql(request);
      }

      request.append(") ");
    }
  }

  /**
   * Return a hash for autoFetch query identification.
   */
  public void queryAutoFetchHash(HashQueryPlanBuilder builder) {
    // we have not yet built the list of expressions
    // so just based on the class name
    builder.add(DefaultExampleExpression.class);
  }

  /**
   * Return a hash for query plan identification.
   */
  public void queryPlanHash(BeanQueryRequest<?> request, HashQueryPlanBuilder builder) {

    // this is always called once, and always called before
    // addSql() and addBindValues() methods
    list = buildExpressions(request);

    builder.add(DefaultExampleExpression.class);

    for (int i = 0; i < list.size(); i++) {
      list.get(i).queryPlanHash(request, builder);
    }
  }

  /**
   * Return a hash for the actual bind values used.
   */
  public int queryBindHash() {
    int hc = DefaultExampleExpression.class.getName().hashCode();
    for (int i = 0; i < list.size(); i++) {
      hc = hc * 31 + list.get(i).queryBindHash();
    }

    return hc;
  }

  /**
   * Build the List of expressions.
   */
  private ArrayList<SpiExpression> buildExpressions(BeanQueryRequest<?> request) {

    ArrayList<SpiExpression> list = new ArrayList<SpiExpression>();

    OrmQueryRequest<?> r = (OrmQueryRequest<?>) request;
    BeanDescriptor<?> beanDescriptor = r.getBeanDescriptor();

    for (BeanProperty beanProperty : beanDescriptor.propertiesAll()) {
      
      String propName = beanProperty.getName();
      Object value = beanProperty.getValue(entity);

      if (beanProperty.isScalar() && value != null) {
        if (value instanceof String) {
          list.add(new LikeExpression(propName, (String) value, caseInsensitive, likeType));
        } else {
          if (!includeZeros && isZero(value)) {
            // exclude the zero values typically to weed out
            // primitive int and long that initialise to 0
          } else {
            list.add(new SimpleExpression(propName, SimpleExpression.Op.EQ, value));
          }
        }
      }
    }

    return list;
  }

  /**
   * Return true if the value is a numeric zero.
   */
  private boolean isZero(Object value) {
    if (value instanceof Number) {
      Number num = (Number) value;
      double doubleValue = num.doubleValue();
      if (doubleValue == 0) {
        return true;
      }
    }
    return false;
  }
}
