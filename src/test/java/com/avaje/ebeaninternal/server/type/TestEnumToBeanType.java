package com.avaje.ebeaninternal.server.type;

import org.junit.Assert;
import org.junit.Test;

import com.avaje.ebeaninternal.server.type.ScalarTypeEnumStandard.OrdinalEnum;
import com.avaje.ebeaninternal.server.type.ScalarTypeEnumStandard.StringEnum;
import com.avaje.tests.model.basic.Customer;
import com.avaje.tests.model.basic.Order;

public class TestEnumToBeanType {

  @Test
  public void test() {
    
     StringEnum stringEnum = new ScalarTypeEnumStandard.StringEnum(Order.Status.class);
     
     OrdinalEnum ordinalEnum = new ScalarTypeEnumStandard.OrdinalEnum(Order.Status.class);
     
     EnumToDbValueMap<?> beanDbMap = EnumToDbValueMap.create(false);
     beanDbMap.add(Customer.Status.ACTIVE, "A", Customer.Status.ACTIVE.name());
     beanDbMap.add(Customer.Status.NEW, "N", Customer.Status.NEW.name());
     beanDbMap.add(Customer.Status.INACTIVE, "I", Customer.Status.INACTIVE.name());
     
     ScalarTypeEnumWithMapping withMapping = new ScalarTypeEnumWithMapping(beanDbMap, Customer.Status.class, 1);
     
     
     Object approved = stringEnum.toBeanType(Order.Status.APPROVED);
     Assert.assertTrue(approved == Order.Status.APPROVED);

     approved = ordinalEnum.toBeanType(Order.Status.APPROVED);
     Assert.assertTrue(approved == Order.Status.APPROVED);

     Object active = withMapping.toBeanType(Customer.Status.ACTIVE);
     Assert.assertTrue(active == Customer.Status.ACTIVE);

  }
  
}
