package com.avaje.tests.insert;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.tests.model.orderentity.OrderEntity;
import com.avaje.tests.model.orderentity.OrderItemEntity;

public class TestInsertOnStringKey extends BaseTestCase {

  @Test
  public void test() {
    
    
    OrderEntity orderEntity = new OrderEntity();
    orderEntity.setId("anyOrderId");
    
    OrderItemEntity orderItemEntity = new OrderItemEntity();
    orderItemEntity.setId("anyOrderItemId");
    orderItemEntity.setVariantId("anyVariantId");
    orderItemEntity.setAmount(BigDecimal.ONE);
    
    orderEntity.setItems(toList(orderItemEntity));
    
    Ebean.save(orderEntity);
    
  }

  private List<OrderItemEntity> toList(OrderItemEntity orderItemEntity) {
    List<OrderItemEntity> list = new ArrayList<OrderItemEntity>();
    list.add(orderItemEntity);
    return list;
  }
  
}
