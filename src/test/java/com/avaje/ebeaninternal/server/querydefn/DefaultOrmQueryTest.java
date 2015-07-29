package com.avaje.ebeaninternal.server.querydefn;


import com.avaje.ebean.Ebean;
import com.avaje.ebean.FetchConfig;
import com.avaje.ebeaninternal.api.HashQueryPlan;
import com.avaje.ebeaninternal.api.HashQueryPlanBuilder;
import com.avaje.ebeaninternal.api.SpiQuery;
import com.avaje.tests.model.basic.Order;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DefaultOrmQueryTest {

  @Test
  public void testQueryAutofetchHash() throws Exception {

    SpiQuery<?> query1 = (SpiQuery<?>)Ebean.find(Order.class)
        .select("status, shipDate")
        .fetch("details", "orderQty, unitPrice", new FetchConfig().query())
        .fetch("details.product", "sku, name");

    HashQueryPlanBuilder b1 = new HashQueryPlanBuilder();
    query1.queryAutofetchHash(b1);
    HashQueryPlan hash1 = b1.build();

    SpiQuery<?> query2 = (SpiQuery<?>)Ebean.find(Order.class)
        .select("status, shipDate")
        .fetch("details", "orderQty, unitPrice")
        .fetch("details.product", "sku, name");

    HashQueryPlanBuilder b2 = new HashQueryPlanBuilder();
    query2.queryAutofetchHash(b2);
    HashQueryPlan hash2 = b2.build();

    assertThat(hash1).isNotEqualTo(hash2);

  }
}