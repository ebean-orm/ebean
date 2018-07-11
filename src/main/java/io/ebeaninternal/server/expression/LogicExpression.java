package io.ebeaninternal.server.expression;

import io.ebean.Expression;
import io.ebean.Junction;
import io.ebean.event.BeanQueryRequest;
import io.ebeaninternal.api.ManyWhereJoins;
import io.ebeaninternal.api.SpiExpression;
import io.ebeaninternal.api.SpiExpressionRequest;
import io.ebeaninternal.api.SpiExpressionValidation;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.api.NaturalKeyQueryData;

import java.io.IOException;

/**
 * A logical And or Or for joining two expressions.
 */
abstract class LogicExpression implements SpiExpression {

  static final String AND = " and ";
  static final String OR = " or ";

  static class And extends LogicExpression {

    And(Expression expOne, Expression expTwo) {
      super(true, expOne, expTwo);
    }

    @Override
    public SpiExpression copyForPlanKey() {
      return new And(expOne.copyForPlanKey(), expTwo.copyForPlanKey());
    }

  }

  static class Or extends LogicExpression {

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

    context.startBool(conjunction ? Junction.Type.AND : Junction.Type.OR);
    expOne.writeDocQuery(context);
    expTwo.writeDocQuery(context);
    context.endBool();
  }

  @Override
  public String nestedPath(BeanDescriptor<?> desc) {

    String pathOne = expOne.nestedPath(desc);
    String pathTwo = expTwo.nestedPath(desc);

    if (pathOne == null && pathTwo == null) {
      return null;
    }
    if (pathOne != null && pathOne.equals(pathTwo)) {
      return pathOne;
    }
    if (pathOne != null) {
      expOne = new NestedPathWrapperExpression(pathOne, expOne);
    }
    if (pathTwo != null) {
      expTwo = new NestedPathWrapperExpression(pathTwo, expTwo);
    }
    return null;
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

    request.append("(");
    expOne.addSql(request);
    request.append(conjunction ? AND : OR);
    expTwo.addSql(request);
    request.append(") ");
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
    builder.append("Logic").append(conjunction ? AND : OR).append("[");
    expOne.queryPlanHash(builder);
    builder.append(",");
    expTwo.queryPlanHash(builder);
    builder.append("]");
  }

  @Override
  public int queryBindHash() {
    int hc = expOne.queryBindHash();
    hc = hc * 92821 + expTwo.queryBindHash();
    return hc;
  }

  @Override
  public boolean isSameByBind(SpiExpression other) {
    LogicExpression that = (LogicExpression) other;
    return this.expOne.isSameByBind(that.expOne)
      && this.expTwo.isSameByBind(that.expTwo);
  }

}
