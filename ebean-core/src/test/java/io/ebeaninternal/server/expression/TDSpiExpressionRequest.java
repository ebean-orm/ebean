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
  public DbExpressionHandler getDbPlatformHandler() {
    return null;
  }

  @Override
  public String parseDeploy(String logicalProp) {
    return null;
  }

  @Override
  public BeanDescriptor<?> getBeanDescriptor() {
    return descriptor;
  }

  @Override
  public SpiOrmQueryRequest<?> getQueryRequest() {
    return null;
  }

  @Override
  public SpiExpressionRequest append(String sqlExpression) {
    sql.append(sqlExpression);
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
  public String getSql() {
    return sql.toString();
  }

  @Override
  public ArrayList<Object> getBindValues() {
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
