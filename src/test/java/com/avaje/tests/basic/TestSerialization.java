package com.avaje.tests.basic;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;
import junit.framework.TestCase;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebean.bean.EntityBeanIntercept;
import com.avaje.ebean.bean.SerializeControl;
import com.avaje.ebean.common.BeanList;
import com.avaje.tests.model.basic.Customer;
import com.avaje.tests.model.basic.Order;
import com.avaje.tests.model.basic.OrderDetail;
import com.avaje.tests.model.basic.Order.Status;

public class TestSerialization extends TestCase {

	public void testSerialization() {
		
		EbeanServer server = Ebean.getServer(null);
		
		Customer customer = server.getReference(Customer.class, 1);
		
		Order o = server.createEntityBean(Order.class);
		o.setOrderDate(new Date(System.currentTimeMillis()));
		o.setStatus(Status.NEW);
		o.setCustomer(customer);
		
		BeanList<OrderDetail> details = new BeanList<OrderDetail>();
		o.setDetails(details);
		
		EntityBean eb = (EntityBean)o;
		
		Order orderCopy = (Order)eb._ebean_createCopy();
		
		Assert.assertNotNull(orderCopy.getDetails());
		Assert.assertNotNull(orderCopy.getCustomer());
		
		EntityBeanIntercept ebi = eb._ebean_getIntercept();
		o.setStatus(Status.APPROVED);
		
		ebi.setReadOnly(true);
		ebi.setLoaded();
		
		try {
			o.setStatus(Status.COMPLETE);
			Assert.assertTrue("dont get here",false);
		} catch (IllegalStateException e){
			Assert.assertTrue("throws exception",true);
		}
		
		SerializeControl.setVanilla(true);
		Assert.assertTrue(SerializeControl.isVanillaBeans());
		Assert.assertTrue(SerializeControl.isVanillaCollections());
		
		Order testUsingSubclassing = new Order();
		if (testUsingSubclassing instanceof EntityBean){
			System.out.println("Need to run serialisation test with 'subclassing/proxies'");
			
		} else {
			System.out.println("Testing serialisation of 'subclassing/proxies'");
			Object vanillaOrder = serialWriteRead(o, true);
			Assert.assertFalse("should be an EntityBean", (vanillaOrder instanceof EntityBean));			
			Assert.assertTrue("should be an Order", (vanillaOrder instanceof Order));			
	
			Order vanOrder = (Order)vanillaOrder;
			Customer vanCustomer = vanOrder.getCustomer();
			List<OrderDetail> vanDetails = vanOrder.getDetails();
			
			Assert.assertFalse("should NOT be an EntityBean", (vanCustomer instanceof EntityBean));			
			Assert.assertFalse("should NOT be an BeanList", (vanDetails instanceof BeanList<?>));			
			Assert.assertTrue("should be an ArrayList", (vanDetails instanceof ArrayList<?>));			
			Assert.assertTrue("should be an Customer", (vanCustomer instanceof Customer));			
		}
		
		SerializeControl.setVanilla(false);
		
		Object subclassOrder = serialWriteRead(o, false);
		Assert.assertTrue("should be an Order", (subclassOrder instanceof Order));			
		Assert.assertTrue("should be an EntityBean", (subclassOrder instanceof EntityBean));			

		SerializeControl.setVanilla(true);
		
		File serTestFile = new File("serTest");
		if (serTestFile.exists()){
			serTestFile.delete();
		}
	}
	
	private Object serialWriteRead(Object inputObject, boolean vanilla){

		try {

			
			
			File serTestFile = new File("serTest");
			FileOutputStream fout = new FileOutputStream(serTestFile);
			ObjectOutputStream oos = new ObjectOutputStream(fout);
			
			oos.writeObject(inputObject);
			oos.close();

			FileInputStream fin = new FileInputStream(serTestFile);
			
			ObjectInputStream ois;
			if (vanilla){
				ois = new ObjectInputStream(fin);
			} else {
				ois = Ebean.getServer(null).createProxyObjectInputStream(fin);
			}
			Object readObject = ois.readObject();
			ois.close();
			return readObject;
			
		} catch (Exception e){
			e.printStackTrace();
			Assert.assertTrue(false);
			return null;
		}

	}
	
}
