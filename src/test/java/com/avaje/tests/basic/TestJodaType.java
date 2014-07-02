package com.avaje.tests.basic;

import org.junit.Assert;
import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.deploy.BeanProperty;
import com.avaje.ebeaninternal.server.type.ScalarType;
import com.avaje.tests.model.basic.TJodaEntity;

public class TestJodaType extends BaseTestCase {

  @Test
	public void test() {
		
		SpiEbeanServer server = (SpiEbeanServer)Ebean.getServer(null);
		BeanDescriptor<TJodaEntity> beanDescriptor = server.getBeanDescriptor(TJodaEntity.class);
		BeanProperty beanProperty = beanDescriptor.getBeanProperty("localTime");
		ScalarType<?> scalarType = beanProperty.getScalarType();
		
		Assert.assertNotNull(scalarType);
	}
	
}
