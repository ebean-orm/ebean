package com.avaje.tests.basic;

import org.joda.time.LocalTime;
import org.junit.Assert;
import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.deploy.BeanProperty;
import com.avaje.ebeaninternal.server.type.ScalarType;
import com.avaje.tests.model.basic.TJodaEntity;

import static org.assertj.core.api.Assertions.assertThat;

public class TestJodaType extends BaseTestCase {

  @Test
	public void test() {
		
		SpiEbeanServer server = (SpiEbeanServer)Ebean.getServer(null);
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
