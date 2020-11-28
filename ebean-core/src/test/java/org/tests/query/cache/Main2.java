package org.tests.query.cache;

import io.ebean.Ebean;

public class Main2 {

  public static void main(String[] args) {
    TestDistinct test = new TestDistinct();
    Ebean.find(ContractCosts.class).where()
    .in("position.contract.aclEntries.aclEntry.id",1L, 2L, 3L).findList(); // <-- will fix some cache issue?
    test.testMissingUnique();

    // executed query:
    // select distinct t0.id, t0.position_id from contract_costs t0
    //   join position u1 on u1.id = t0.position_id
    //   join contract u2 on u2.id = u1.contract_id
    //   join acl_container_relation u3 on u3.container_id = u2.id
    // where u3.acl_entry_id in (?, ? ) ; --bind(Array[2]={1,2})

  }

}
