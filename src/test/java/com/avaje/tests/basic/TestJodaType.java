package com.avaje.tests.basic;

import junit.framework.Assert;
import junit.framework.TestCase;

import com.avaje.ebean.Ebean;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.deploy.BeanProperty;
import com.avaje.ebeaninternal.server.type.ScalarType;
import com.avaje.tests.model.basic.TJodaEntity;

public class TestJodaType extends TestCase {

	public void test() {
		
		SpiEbeanServer server = (SpiEbeanServer)Ebean.getServer(null);
		BeanDescriptor<TJodaEntity> beanDescriptor = server.getBeanDescriptor(TJodaEntity.class);
		BeanProperty beanProperty = beanDescriptor.getBeanProperty("localTime");
		ScalarType<?> scalarType = beanProperty.getScalarType();
		
		Assert.assertNotNull(scalarType);
	}
	
}
