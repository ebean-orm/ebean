package io.ebeaninternal.server.core;

import io.ebeaninternal.api.BindParams;
import io.ebeaninternal.api.SpiExpressionBind;
import io.ebeaninternal.api.SpiExpressionList;
import io.ebeaninternal.api.SpiQuery;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.persist.MultiValueWrapper;

import java.util.ArrayList;
import java.util.List;

final class SlowQueryBindCapture implements SpiExpressionBind {

  private final SpiQuery<?> query;
  private final List<Object> bindParams = new ArrayList<>();

  SlowQueryBindCapture(SpiQuery<?> query) {
    this.query = query;
  }

  List<Object> capture() {
    var params = query.bindParams();
    if (params != null) {
      var positionedParameters = params.positionedParameters();
      for (BindParams.Param param : positionedParameters) {
        if (param.isInParam()) {
          add(param.inValue());
        }
      }
    }
    Object id = query.getId();
    if (id != null) {
      add(id);
    }
    SpiExpressionList<?> spiExpressionList = query.whereExpressions();
    if (spiExpressionList != null) {
      spiExpressionList.addBindValues(this);
    }
    return bindParams;
  }

  private void add(Object value) {
    if (value instanceof MultiValueWrapper) {
      var mvw = (MultiValueWrapper) value;
      bindParams.add(mvw.getValues());
    } else {
      bindParams.add(value);
    }
  }

  @Override
  public BeanDescriptor<?> descriptor() {
    return query.descriptor();
  }

  @Override
  public void addBindValue(Object bindValue) {
    add(bindValue);
  }

  @Override
  public void addBindEncryptKey(Object encryptKey) {
    bindParams.add("*");
  }

  @Override
  public String escapeLikeString(String value) {
    return query.descriptor().ebeanServer().databasePlatform().escapeLikeString(value);
  }
}
