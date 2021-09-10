package org.tests.insert;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import org.junit.jupiter.api.Test;
import org.tests.model.orderentity.OrderEntity;
import org.tests.model.orderentity.OrderItemEntity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TestInsertOnStringKey extends BaseTestCase {

  @Test
  public void test() {


    OrderEntity orderEntity = new OrderEntity();
    orderEntity.setId("anyOrderId" + new Random().nextInt());

    OrderItemEntity orderItemEntity = new OrderItemEntity();
    orderItemEntity.setId("anyOrderItemId" + new Random().nextInt());
    orderItemEntity.setVariantId("anyVariantId");
    orderItemEntity.setAmount(BigDecimal.ONE);

    orderEntity.setItems(toList(orderItemEntity));

    DB.save(orderEntity);

  }

  private List<OrderItemEntity> toList(OrderItemEntity orderItemEntity) {
    List<OrderItemEntity> list = new ArrayList<>();
    list.add(orderItemEntity);
    return list;
  }

}
