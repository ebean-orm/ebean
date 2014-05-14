package com.avaje.tests.model.basic;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.TxRunnable;
import com.avaje.tests.model.basic.Order.Status;

public class ResetBasicData {

	private static boolean runOnce;
	
	private static EbeanServer server = Ebean.getServer(null);
	
	public static synchronized void reset() {
		
		if (runOnce){
			return;
		}
		
		final ResetBasicData me = new ResetBasicData();
		
		server.execute(new TxRunnable() {
			public void run() {
				me.deleteAll();
				me.insertCountries();
				me.insertProducts();
				me.insertTestCustAndOrders();
			}
		});
		runOnce = true;
	}
	
	
	
	
	public void deleteAll() {
		Ebean.execute(new TxRunnable() {
			public void run() {
				
				//Ebean.currentTransaction().setBatchMode(false);
				
				// orm update use bean name and bean properties
			  server.createUpdate(OrderShipment.class, "delete from orderShipment")
					.execute();
			
			  server.createUpdate(OrderDetail.class, "delete from orderDetail")
					.execute();
				
			  server.createUpdate(Order.class,"delete from order")
					.execute();

			  server.createUpdate(Contact.class,"delete from contact")
					.execute();
	
			  server.createUpdate(Customer.class,"delete from Customer")
					.execute();

			  server.createUpdate(Address.class,"delete from address")
					.execute();
	
				// sql update uses table and column names
			  server.createSqlUpdate("delete from o_country")
					.execute();
	
			  server.createSqlUpdate("delete from o_product")
					.execute();
			
			}
		});
	}
	
	
	public void insertCountries() {
		
	  server.execute(new TxRunnable() {
			public void run() {
				Country c = new Country();
				c.setCode("NZ");
				c.setName("New Zealand");
				server.save(c);
				
				Country au = new Country();
				au.setCode("AU");
				au.setName("Australia");
				server.save(au);				
			}
		});
	}
	

	public void insertProducts() {
		
	  server.execute(new TxRunnable() {
			public void run() {
				Product p = new Product();
				p.setId(1);
				p.setName("Chair");
				p.setSku("C001");
				server.save(p);
		
				p = new Product();
				p.setId(2);
				p.setName("Desk");
				p.setSku("DSK1");
				server.save(p);
		
				p = new Product();
				p.setId(3);
				p.setName("Computer");
				p.setSku("C002");
				server.save(p);
		
				p = new Product();
				p.setId(4);
				p.setName("Printer");
				p.setSku("C003");
				server.save(p);
			}
		});
	}
	
	public void insertTestCustAndOrders() {

		Ebean.execute(new TxRunnable() {
			public void run() {
				Customer cust1 = insertCustomer("Rob");
				Customer cust2 = insertCustomerNoAddress();
				insertCustomerFiona();
				insertCustomerNoContacts("NocCust");
				
				createOrder1(cust1);
				createOrder2(cust2);
				createOrder3(cust1);
				createOrder4(cust1);
			}
		});	
	}
	
	public static Customer createCustAndOrder(String custName) {
	    
	    ResetBasicData me = new ResetBasicData();
	    Customer cust1 = insertCustomer(custName);
	    me.createOrder1(cust1);
	    return cust1;
	}
	
    public static Order createOrderCustAndOrder(String custName) {

        ResetBasicData me = new ResetBasicData();
        Customer cust1 = insertCustomer(custName);
        Order o = me.createOrder1(cust1);
        return o;
    }

    private static int contactEmailNum = 1;
    
	private Customer insertCustomerFiona() {
		
		Customer c = createCustomer("Fiona", "12 Apple St", "West Coast Rd", 1, "2009-08-31");
		c.setStatus(Customer.Status.ACTIVE);		
		
		c.addContact(createContact("Fiona","Black"));
		c.addContact(createContact("Tracy","Red"));

		Ebean.save(c);
		return c;
	}
	
	public static Contact createContact(String firstName, String lastName) {
		Contact contact = new Contact(firstName,lastName);
		String email = contact.getLastName()+(contactEmailNum++)+"@test.com";
		contact.setEmail(email.toLowerCase());
		return contact;
	}
	
	private Customer insertCustomerNoContacts(String name) {

		Customer c = createCustomer("Roger", "15 Kumera Way", "Bos town", 1, "2010-04-10");
		c.setName(name);
		c.setStatus(Customer.Status.ACTIVE);

		Ebean.save(c);
		return c;
	}
	
	private Customer insertCustomerNoAddress() {
		
		Customer c = new Customer();
		c.setName("Cust NoAddress");
		c.setStatus(Customer.Status.NEW);
		c.addContact(createContact("Jack","Black"));

		Ebean.save(c);
		return c;
	}

    private static Customer insertCustomer(String name) {
        Customer c = createCustomer(name, "1 Banana St", "P.O.Box 1234", 1, null);
        Ebean.save(c);
        return c;
    }

    public static Customer createCustomer(String name, String shippingStreet, String billingStreet, int contactSuffix) {
        return createCustomer(name, shippingStreet, billingStreet, contactSuffix, null);
    }
    
	public static Customer createCustomer(String name, String shippingStreet, String billingStreet, int contactSuffix, String annDate) {
		
		Customer c = new Customer();
		c.setName(name);
		c.setStatus(Customer.Status.NEW);
		if (annDate == null){
		    annDate = "2010-04-14";
		}
		c.setAnniversary(Date.valueOf(annDate));
		if (contactSuffix > 0){
    		c.addContact(new Contact("Jim"+contactSuffix,"Cricket"));
    		c.addContact(new Contact("Fred"+contactSuffix,"Blue"));
    		c.addContact(new Contact("Bugs"+contactSuffix,"Bunny"));
		}
		
		if (shippingStreet != null){
    		Address shippingAddr = new Address();
    		shippingAddr.setLine1(shippingStreet);
    		shippingAddr.setLine2("Sandringham");
    		shippingAddr.setCity("Auckland");
    		shippingAddr.setCountry(Ebean.getReference(Country.class, "NZ"));
    		
    		c.setShippingAddress(shippingAddr);
		}
		
		if (billingStreet != null){
    		Address billingAddr = new Address();
    		billingAddr.setLine1(billingStreet);
    		billingAddr.setLine2("St Lukes");
    		billingAddr.setCity("Auckland");
    		billingAddr.setCountry(Ebean.getReference(Country.class, "NZ"));
    		
    		c.setBillingAddress(billingAddr);
		}
		
		return c;
	}
	
	private Order createOrder1(Customer customer) {
		
		Product product1 = Ebean.getReference(Product.class, 1);
		Product product2 = Ebean.getReference(Product.class, 2);
		Product product3 = Ebean.getReference(Product.class, 3);
			
		
		Order order = new Order();
		order.setCustomer(customer);
		
		List<OrderDetail> details = new ArrayList<OrderDetail>();
		details.add(new OrderDetail(product1, 5, 10.50));
		details.add(new OrderDetail(product2, 3, 1.10));
		details.add(new OrderDetail(product3, 1, 2.00));
		order.setDetails(details);
		
		
		order.addShipment(new OrderShipment());
		
		Ebean.save(order);
		return order;
	}

	private void createOrder2(Customer customer) {
		
		Product product1 = Ebean.getReference(Product.class, 1);
					
		Order order = new Order();
    order.setStatus(Status.SHIPPED);
		order.setCustomer(customer);
		
		List<OrderDetail> details = new ArrayList<OrderDetail>();
		details.add(new OrderDetail(product1, 4, 10.50));
		order.setDetails(details);
		
		order.addShipment(new OrderShipment());

		Ebean.save(order);
	}

	private void createOrder3(Customer customer) {
		
		Product product1 = Ebean.getReference(Product.class, 1);
		Product product3 = Ebean.getReference(Product.class, 3);
					
		Order order = new Order();
		order.setStatus(Status.COMPLETE);
		order.setCustomer(customer);
		
		List<OrderDetail> details = new ArrayList<OrderDetail>();
		details.add(new OrderDetail(product1, 3, 10.50));
		details.add(new OrderDetail(product3, 40, 2.10));
		order.setDetails(details);
		
		order.addShipment(new OrderShipment());

		Ebean.save(order);
	}

  private void createOrder4(Customer customer) {

    Order order = new Order();
    order.setCustomer(customer);

    order.addShipment(new OrderShipment());

    Ebean.save(order);
  }
}
