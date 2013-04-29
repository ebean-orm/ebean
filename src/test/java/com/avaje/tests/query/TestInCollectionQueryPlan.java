package com.avaje.tests.query;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.tests.model.basic.Customer;
import com.avaje.tests.model.basic.ResetBasicData;

public class TestInCollectionQueryPlan extends BaseTestCase {

  @Test
  public void test() {

    ResetBasicData.reset();

    List<Object> idList1 = new ArrayList<Object>();
    idList1.add(1);

    String oq = "find customer where id in (:idList)";
    Ebean.createQuery(Customer.class, oq).setParameter("idList", idList1).findList();

    List<Object> idList2 = new ArrayList<Object>();
    idList2.add(1);
    idList2.add(2);

    Ebean.createQuery(Customer.class, oq).setParameter("idList", idList2).findList();

  }
}
