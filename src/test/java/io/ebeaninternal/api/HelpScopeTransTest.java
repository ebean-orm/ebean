package io.ebeaninternal.api;

import io.ebean.Ebean;
import io.ebean.TxScope;
import org.junit.Test;
import org.tests.model.basic.Contact;
import org.tests.model.basic.Customer;
import org.tests.model.basic.ResetBasicData;
import org.tests.model.basic.UUOne;

public class HelpScopeTransTest {

  @Test
  public void begin() throws Exception {

    ResetBasicData.reset();

    HelpScopeTrans.enter(TxScope.required());
    HelpScopeTrans.enter(TxScope.required());

    Ebean.find(Customer.class).findList();

    HelpScopeTrans.enter(TxScope.required());
    Ebean.find(Contact.class).findList();

    HelpScopeTrans.exit(null, 1);

    UUOne one = new UUOne();
    one.setName("junk");
    Ebean.save(one);

    HelpScopeTrans.exit(null, 1);
    HelpScopeTrans.exit(null, 1);
  }

}
