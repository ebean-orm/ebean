package org.tests.basic;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.core.type.ScalarType;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.deploy.BeanProperty;
import org.tests.model.basic.TJodaEntity;
import org.joda.time.LocalTime;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestJodaType extends BaseTestCase {

  @Test
  public void test() {

    SpiEbeanServer server = (SpiEbeanServer) DB.getDefault();
    BeanDescriptor<TJodaEntity> beanDescriptor = server.getBeanDescriptor(TJodaEntity.class);
    BeanProperty beanProperty = beanDescriptor.beanProperty("localTime");
    ScalarType<?> scalarType = beanProperty.scalarType();

    assertNotNull(scalarType);
  }

  @Test
  public void test_insert_find() {

    LocalTime now = new LocalTime().withMillisOfSecond(0);

    TJodaEntity bean = new TJodaEntity();
    bean.setLocalTime(now);
    DB.save(bean);

    TJodaEntity foundBean = DB.find(TJodaEntity.class, bean.getId());

    assertThat(foundBean.getLocalTime()).isEqualTo(bean.getLocalTime());
  }

}
