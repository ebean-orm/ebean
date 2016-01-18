package com.avaje.ebeaninternal.server.expression;

import com.avaje.ebeaninternal.api.SpiExpressionRequest;
import com.avaje.ebeaninternal.server.core.JsonExpressionHandler;
import com.avaje.ebeaninternal.server.core.SpiOrmQueryRequest;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;

import java.util.ArrayList;
import java.util.List;

/**
 * Test double for testing with SpiExpressionRequest.
 */
public class TDSpiExpressionRequest implements SpiExpressionRequest {

  List<Object> bindValues = new ArrayList<Object>();

  final BeanDescriptor<?> descriptor;

  public TDSpiExpressionRequest(BeanDescriptor<?> descriptor) {
    this.descriptor = descriptor;
  }

  @Override
  public JsonExpressionHandler getJsonHandler() {
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
  public SpiExpressionRequest append(String sql) {
    return null;
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
    return null;
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
  public void appendLike() {

  }
}
