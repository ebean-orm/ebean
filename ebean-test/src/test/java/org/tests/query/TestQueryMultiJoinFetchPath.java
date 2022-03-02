package org.tests.query;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.tests.model.basic.Customer;
import org.tests.model.basic.Customer.Status;
import org.tests.model.basic.ResetBasicData;
import org.tests.model.join.Access;
import org.tests.model.join.Account;
import org.tests.model.join.AccountAccess;
import org.tests.model.join.BankAccount;
import org.tests.model.join.CustomerAccess;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.Query;

public class TestQueryMultiJoinFetchPath extends BaseTestCase {

  @Test
  public void test() {

    ResetBasicData.reset();

    Customer c1 = new Customer();
    c1.setName("c1");
    c1.setStatus(Status.ACTIVE);
    DB.save(c1);
    
    Customer c2 = new Customer();
    c2.setName("c2");
    c2.setStatus(Status.ACTIVE);
    DB.save(c2);
    
    Customer c3 = new Customer();
    c3.setName("c3");
    c3.setStatus(Status.ACTIVE);
    DB.save(c3);
    
    Account a1 = new BankAccount();
    a1.setAccountNumber("a1");
    a1.setOwner(c1);
    DB.save(a1);
    
    CustomerAccess ca = new CustomerAccess();
    ca.setAccessor(c3);
    ca.setPrincipal(c1);
    DB.save(ca);
    
    AccountAccess aa = new AccountAccess();
    aa.setAccessor(c2);
    aa.setAccount(a1);
    DB.save(aa);
    
    List<Object> ids = DB.find(Access.class)
      .where()
      .eq("principal.status", Status.ACTIVE)
      .eq("accessor.status", Status.ACTIVE)
      .findIds();
    
    assertThat(ids).hasSize(2);
   
    Query<Access> query = DB.find(Access.class)
      .fetch("account","accountNumber")
      .fetch("accessor","name")
      .where()
      .eq("accessor.status", Status.ACTIVE)
      .eq("principal.status", Status.ACTIVE)
      .idIn(ids)
      .query();
    
    List<Access> accesses = query.findList();
   
    assertThat(accesses).hasSize(2);
  }
  
}
