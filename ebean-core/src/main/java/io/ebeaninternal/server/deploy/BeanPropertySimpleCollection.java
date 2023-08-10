package io.ebeaninternal.server.deploy;

import io.ebean.SqlUpdate;
import io.ebean.bean.EntityBean;
import io.ebeaninternal.api.json.SpiJsonReader;
import io.ebeaninternal.server.deploy.meta.DeployBeanPropertySimpleCollection;

import java.io.IOException;

public final class BeanPropertySimpleCollection<T> extends BeanPropertyAssocMany<T> {

  public BeanPropertySimpleCollection(BeanDescriptor<?> descriptor, DeployBeanPropertySimpleCollection<T> deploy) {
    super(descriptor, deploy);
  }

  @Override
  public void bindElementValue(SqlUpdate insert, Object value) {
    insert.setParameter(value);
  }

  @Override
  public Object jsonReadCollection(SpiJsonReader readJson, EntityBean parentBean) throws IOException {
    return elementDescriptor.jsonReadCollection(readJson, parentBean);
  }
}
