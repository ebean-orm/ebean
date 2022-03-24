package io.ebean.xtest.internal.api;

import io.ebean.DB;
import io.ebean.TxScope;
import io.ebeaninternal.api.HelpScopeTrans;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Contact;
import org.tests.model.basic.Customer;
import org.tests.model.basic.ResetBasicData;
import org.tests.model.basic.UUOne;

import static org.junit.jupiter.api.Assertions.assertNull;

public class HelpScopeTransTest {

  @Test
  public void begin() throws Exception {

    ResetBasicData.reset();

    HelpScopeTrans.enter(TxScope.required());
    HelpScopeTrans.enter(TxScope.required());

    DB.find(Customer.class).findList();

    HelpScopeTrans.enter(TxScope.required());
    DB.find(Contact.class).findList();

    HelpScopeTrans.exit(null, 1);

    UUOne one = new UUOne();
    one.setName("junk");
    DB.save(one);

    HelpScopeTrans.exit(null, 1);
    HelpScopeTrans.exit(null, 1);
  }

  @Test
  public void disableTransaction() {
    HelpScopeTrans.setEnabled(false);

    try {
      HelpScopeTrans.enter(TxScope.required());
      assertNull(DB.getDefault().currentTransaction());
      HelpScopeTrans.exit(null, 1);
    } finally {
      HelpScopeTrans.setEnabled(true);
    }
  }

}
