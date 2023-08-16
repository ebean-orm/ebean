package io.ebeaninternal.server.expression;

import io.ebeaninternal.api.SpiExpressionRequest;
import io.ebeaninternal.server.core.SpiOrmQueryRequest;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.expression.platform.DbExpressionHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Test double for testing with SpiExpressionRequest.
 */
public class TDSpiExpressionRequest implements SpiExpressionRequest {

  List<Object> bindValues = new ArrayList<>();

  final BeanDescriptor<?> descriptor;

  StringBuilder sql = new StringBuilder();

  public TDSpiExpressionRequest(BeanDescriptor<?> descriptor) {
    this.descriptor = descriptor;
  }

  @Override
  public DbExpressionHandler platformHandler() {
    return null;
  }

  @Override
  public String parseDeploy(String logicalProp) {
    return null;
  }

  @Override
  public BeanDescriptor<?> descriptor() {
    return descriptor;
  }

  @Override
  public SpiOrmQueryRequest<?> queryRequest() {
    return null;
  }

  @Override
  public StringBuilder buffer() {
    return sql;
  }

  @Override
  public SpiExpressionRequest append(String expression) {
    sql.append(expression);
    return this;
  }

  @Override
  public SpiExpressionRequest property(String expression) {
    sql.append(expression);
    return this;
  }

  @Override
  public SpiExpressionRequest parse(String expression) {
    sql.append(expression);
    return this;
  }

  @Override
  public void addBindEncryptKey(Object encryptKey) {

  }

  @Override
  public void addBindValue(Object bindValue) {
    bindValues.add(bindValue);
  }

  @Override
  public String sql() {
    return sql.toString();
  }

  @Override
  public ArrayList<Object> bindValues() {
    return null;
  }

  @Override
  public int nextParameter() {
    return 0;
  }

  @Override
  public void appendLike(boolean rawLikeExpression) {

  }

  @Override
  public String escapeLikeString(String value) {
    return value;
  }

  @Override
  public void appendInExpression(boolean not, List<Object> bindValues) {

  }
}
