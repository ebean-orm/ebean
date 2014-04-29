package com.avaje.tests.query.other;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.api.SpiQuery;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.deploy.BeanPropertyAssocMany;
import com.avaje.tests.model.basic.Order;
import com.avaje.tests.model.basic.OrderDetail;
import com.avaje.tests.model.basic.ResetBasicData;

public class TestManyLazyLoadingQuery extends BaseTestCase {

  @Test
  public void test() {
    
    ResetBasicData.reset();
    
    SpiEbeanServer server = (SpiEbeanServer)Ebean.getServer(null);
    
    BeanDescriptor<Order> descOrder = server.getBeanDescriptor(Order.class);
    BeanPropertyAssocMany<?> beanProperty = (BeanPropertyAssocMany<?>)descOrder.getBeanProperty("details");
    
    List<Object> parentIds = new ArrayList<Object>();
    parentIds.add(1);
    
    
    List<Order> orders = 
        Ebean.find(Order.class)
          .where().lt("id", 4)
          .findList();
    
    for (Order order : orders) {
      List<OrderDetail> details = order.getDetails();
      System.out.println(details.size());
    }
    
    
    
    // start transaction to keep PC going to lazy query
    Ebean.beginTransaction();
    try {
      Ebean.find(Order.class, 1);
      
      SpiQuery<?> query0 = (SpiQuery<?>)Ebean.find(OrderDetail.class);
      
      query0.setLazyLoadForParents(parentIds, beanProperty);
      
      beanProperty.addWhereParentIdIn(query0, parentIds);
      
      query0.findList();

    } finally {
      Ebean.endTransaction();
    }
    
    List<OrderDetail> details = Ebean.find(OrderDetail.class)
      .where().eq("order.id", 1)
      .findList();
    
    for (OrderDetail orderDetail : details) {
      System.out.println(orderDetail);      
    }
    
  }
  
}
