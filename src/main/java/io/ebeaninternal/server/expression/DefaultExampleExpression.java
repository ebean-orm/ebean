package io.ebeaninternal.server.expression;

import io.ebean.ExampleExpression;
import io.ebean.LikeType;
import io.ebean.bean.EntityBean;
import io.ebean.event.BeanQueryRequest;
import io.ebean.util.SplitName;
import io.ebeaninternal.api.ManyWhereJoins;
import io.ebeaninternal.api.NaturalKeyQueryData;
import io.ebeaninternal.api.SpiExpression;
import io.ebeaninternal.api.SpiExpressionRequest;
import io.ebeaninternal.api.SpiExpressionValidation;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.deploy.BeanProperty;
import io.ebeaninternal.server.deploy.BeanPropertyAssocOne;

import java.io.IOException;
import java.util.ArrayList;

/**
 * A "Query By Example" type of expression.
 * <p>
 * Pass in an example entity and for each non-null scalar properties an
 * expression is added.
 * </p>
 * <p>
 * <pre>{@code
 * // create an example bean and set the properties
 * // with the query parameters you want
 * Customer example = new Customer();
 * example.setName("Rob%");
 * example.setNotes("%something%");
 *
 * List<Customer> list = Ebean.find(Customer.class)
 *   .where()
 *   .exampleLike(example)
 *   .findList();
 *
 * }</pre>
 */
public class DefaultExampleExpression implements SpiExpression, ExampleExpression {

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
   * @param entity          the example entity with non null property values
   * @param caseInsensitive if true use case insensitive expressions
   * @param likeType        the type of Like wild card used
   */
  public DefaultExampleExpression(EntityBean entity, boolean caseInsensitive, LikeType likeType) {
    this.entity = entity;
    this.caseInsensitive = caseInsensitive;
    this.likeType = likeType;
  }

  DefaultExampleExpression(ArrayList<SpiExpression> source) {
    this.entity = null;
    this.list = new ArrayList<>(source.size());
    for (SpiExpression expression : source) {
      list.add(expression.copyForPlanKey());
    }
  }

  @Override
  public boolean naturalKey(NaturalKeyQueryData<?> data) {
    // can't use naturalKey cache
    return false;
  }

  @Override
  public void simplify() {
    // do nothing
  }

  @Override
  public void writeDocQuery(DocQueryContext context) throws IOException {
    if (!list.isEmpty()) {
      context.startBoolMust();
      for (SpiExpression expr : list) {
        expr.writeDocQuery(context);
      }
      context.endBool();
    }
  }

  @Override
  public Object getIdEqualTo(String idName) {
    // always return null for this expression
    return null;
  }

  @Override
  public SpiExpression copyForPlanKey() {
    return new DefaultExampleExpression(list);
  }

  @Override
  public String nestedPath(BeanDescriptor<?> desc) {
    return null;
  }

  @Override
  public void containsMany(BeanDescriptor<?> desc, ManyWhereJoins whereManyJoins) {
    list = buildExpressions(desc);
    if (list != null) {
      for (SpiExpression aList : list) {
        aList.containsMany(desc, whereManyJoins);
      }
    }
  }

  @Override
  public void prepareExpression(BeanQueryRequest<?> request) {
    // do nothing
  }

  @Override
  public ExampleExpression includeZeros() {
    includeZeros = true;
    return this;
  }

  @Override
  public ExampleExpression caseInsensitive() {
    caseInsensitive = true;
    return this;
  }

  @Override
  public ExampleExpression useStartsWith() {
    likeType = LikeType.STARTS_WITH;
    return this;
  }

  @Override
  public ExampleExpression useContains() {
    likeType = LikeType.CONTAINS;
    return this;
  }

  @Override
  public ExampleExpression useEndsWith() {
    likeType = LikeType.ENDS_WITH;
    return this;
  }

  @Override
  public ExampleExpression useEqualTo() {
    likeType = LikeType.EQUAL_TO;
    return this;
  }

  @Override
  public void validate(SpiExpressionValidation validation) {
    for (SpiExpression aList : list) {
      aList.validate(validation);
    }
  }

  /**
   * Adds bind values to the request.
   */
  @Override
  public void addBindValues(SpiExpressionRequest request) {

    for (SpiExpression item : list) {
      item.addBindValues(request);
    }
  }

  /**
   * Generates and adds the sql to the request.
   */
  @Override
  public void addSql(SpiExpressionRequest request) {

    if (list.isEmpty()) {
      request.append("1=1");
    } else {
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
   * Return a hash for AutoTune query identification.
   */
  @Override
  public void queryPlanHash(StringBuilder builder) {

    builder.append("Example[");
    for (SpiExpression aList : list) {
      aList.queryPlanHash(builder);
      builder.append(",");
    }
    builder.append("]");
  }

  /**
   * Return a hash for the actual bind values used.
   */
  @Override
  public int queryBindHash() {
    int hc = DefaultExampleExpression.class.getName().hashCode();
    for (SpiExpression aList : list) {
      hc = hc * 92821 + aList.queryBindHash();
    }
    return hc;
  }

  @Override
  public boolean isSameByBind(SpiExpression other) {
    DefaultExampleExpression that = (DefaultExampleExpression) other;
    if (this.list.size() != that.list.size()) {
      return false;
    }
    for (int i = 0; i < list.size(); i++) {
      if (!list.get(i).isSameByBind(that.list.get(i))) {
        return false;
      }
    }
    return true;
  }

  /**
   * Build the List of expressions.
   */
  private ArrayList<SpiExpression> buildExpressions(BeanDescriptor<?> beanDescriptor) {

    ArrayList<SpiExpression> list = new ArrayList<>();
    addExpressions(list, beanDescriptor, entity, null);
    return list;
  }

  /**
   * Add expressions to the list for all the non-null properties (and do this recursively).
   */
  private void addExpressions(ArrayList<SpiExpression> list, BeanDescriptor<?> beanDescriptor, EntityBean bean, String prefix) {

    for (BeanProperty beanProperty : beanDescriptor.propertiesAll()) {

      if (!beanProperty.isTransient()) {
        Object value = beanProperty.getValue(bean);
        if (value != null) {
          String propName = SplitName.add(prefix, beanProperty.getName());
          if (beanProperty.isScalar()) {
            if (value instanceof String) {
              list.add(new LikeExpression(propName, value, caseInsensitive, likeType));
            } else {
              // exclude the zero values typically to weed out
              // primitive int and long that initialise to 0
              if (includeZeros || !isZero(value)) {
                list.add(new SimpleExpression(propName, Op.EQ, value));
              }
            }

          } else if ((beanProperty instanceof BeanPropertyAssocOne) && (value instanceof EntityBean)) {
            BeanPropertyAssocOne<?> assocOne = (BeanPropertyAssocOne<?>) beanProperty;
            BeanDescriptor<?> targetDescriptor = assocOne.getTargetDescriptor();
            addExpressions(list, targetDescriptor, (EntityBean) value, propName);
          }
        }
      }
    }
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
