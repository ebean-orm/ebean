package org.tests.basic;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.deploy.BeanProperty;
import io.ebeaninternal.server.type.ScalarType;
import org.tests.model.basic.TJodaEntity;
import org.joda.time.LocalTime;
import org.junit.Assert;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TestJodaType extends BaseTestCase {

  @Test
  public void test() {

    SpiEbeanServer server = (SpiEbeanServer) Ebean.getServer(null);
    BeanDescriptor<TJodaEntity> beanDescriptor = server.getBeanDescriptor(TJodaEntity.class);
    BeanProperty beanProperty = beanDescriptor.getBeanProperty("localTime");
    ScalarType<?> scalarType = beanProperty.getScalarType();

    Assert.assertNotNull(scalarType);
  }

  @Test
  public void test_insert_find() {

    LocalTime now = new LocalTime().withMillisOfSecond(0);

    TJodaEntity bean = new TJodaEntity();
    bean.setLocalTime(now);
    Ebean.save(bean);

    TJodaEntity foundBean = Ebean.find(TJodaEntity.class, bean.getId());

    assertThat(foundBean.getLocalTime()).isEqualTo(bean.getLocalTime());
  }

}
