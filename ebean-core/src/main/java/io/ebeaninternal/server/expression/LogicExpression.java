package io.ebeaninternal.server.expression;

import io.ebean.Expression;
import io.ebean.event.BeanQueryRequest;
import io.ebeaninternal.api.BindValuesKey;
import io.ebeaninternal.api.ManyWhereJoins;
import io.ebeaninternal.api.NaturalKeyQueryData;
import io.ebeaninternal.api.SpiExpression;
import io.ebeaninternal.api.SpiExpressionRequest;
import io.ebeaninternal.api.SpiExpressionValidation;
import io.ebeaninternal.server.deploy.BeanDescriptor;

/**
 * A logical And or, Or for joining two expressions.
 */
abstract class LogicExpression implements SpiExpression {

  static final String AND = " and ";
  static final String OR = " or ";

  static final class And extends LogicExpression {

    And(Expression expOne, Expression expTwo) {
      super(true, expOne, expTwo);
    }

    @Override
    public SpiExpression copyForPlanKey() {
      return new And(expOne.copyForPlanKey(), expTwo.copyForPlanKey());
    }

  }

  static final class Or extends LogicExpression {

    Or(Expression expOne, Expression expTwo) {
      super(false, expOne, expTwo);
    }

    @Override
    public SpiExpression copyForPlanKey() {
      return new Or(expOne.copyForPlanKey(), expTwo.copyForPlanKey());
    }
  }

  SpiExpression expOne;
  SpiExpression expTwo;
  private final boolean conjunction;

  LogicExpression(boolean conjunction, Expression expOne, Expression expTwo) {
    this.conjunction = conjunction;
    this.expOne = (SpiExpression) expOne;
    this.expTwo = (SpiExpression) expTwo;
  }

  @Override
  public void prefixProperty(String path) {
    expOne.prefixProperty(path);
    expTwo.prefixProperty(path);
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
  public Object getIdEqualTo(String idName) {
    // always return null for this expression
    return null;
  }

  @Override
  public void containsMany(BeanDescriptor<?> desc, ManyWhereJoins manyWhereJoin) {

    // get the current state for 'require outer joins'
    boolean parentOuterJoins = manyWhereJoin.isRequireOuterJoins();
    if (!conjunction) {
      // turn on outer joins required for disjunction expressions
      manyWhereJoin.setRequireOuterJoins(true);
    }
    expOne.containsMany(desc, manyWhereJoin);
    expTwo.containsMany(desc, manyWhereJoin);
    if (!conjunction && !parentOuterJoins) {
      // restore state to not forcing outer joins
      manyWhereJoin.setRequireOuterJoins(false);
    }
  }

  @Override
  public void validate(SpiExpressionValidation validation) {
    expOne.validate(validation);
    expTwo.validate(validation);
  }

  @Override
  public void addBindValues(SpiExpressionRequest request) {
    expOne.addBindValues(request);
    expTwo.addBindValues(request);
  }

  @Override
  public void addSql(SpiExpressionRequest request) {

    request.append('(');
    expOne.addSql(request);
    request.append(conjunction ? AND : OR);
    expTwo.addSql(request);
    request.append(')');
  }

  @Override
  public void prepareExpression(BeanQueryRequest<?> request) {
    expOne.prepareExpression(request);
    expTwo.prepareExpression(request);
  }

  /**
   * Based on the joinType plus the two expressions.
   */
  @Override
  public void queryPlanHash(StringBuilder builder) {
    builder.append("Logic").append(conjunction ? AND : OR).append('[');
    expOne.queryPlanHash(builder);
    builder.append(',');
    expTwo.queryPlanHash(builder);
    builder.append(']');
  }

  @Override
  public void queryBindKey(BindValuesKey key) {
    key.add(expOne).add(expTwo);
  }

  @Override
  public boolean isSameByBind(SpiExpression other) {
    LogicExpression that = (LogicExpression) other;
    return this.expOne.isSameByBind(that.expOne)
      && this.expTwo.isSameByBind(that.expTwo);
  }

}
