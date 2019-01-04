package org.tests.query.cache;


public class Main1 {

  public static void main(String[] args) {
    TestDistinct test = new TestDistinct();
    test.testMissingUnique();
    // executed query:
    // select t0.id, t0.position_id from contract_costs t0
    //   join position t1 on t1.id = t0.position_id
    //   join contract t2 on t2.id = t1.contract_id
    //   left join acl_container_relation t3 on t3.container_id = t2.id
    // where t3.acl_entry_id in (?, ? ) ; --bind(Array[2]={1,2})

    // Test will fail:
    // Expected size:<1> but was:<2> in:
    //   <[org.tests.query.cache.ContractCosts@1, org.tests.query.cache.ContractCosts@1]>
    //    at org.tests.query.cache.TestDistinct.testMissingUnique(TestDistinct.java:49)
    //    at org.tests.query.cache.Main1.main(Main1.java:7)
  }

}
