package com.avaje.ebeaninternal.server.changelog;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.event.changelog.ChangeLogFilter;
import com.avaje.tests.model.basic.Address;
import com.avaje.tests.model.basic.Contact;
import com.avaje.tests.model.basic.Country;
import com.avaje.tests.model.basic.Customer;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class DefaultChangeLogRegisterTest extends BaseTestCase {


  @Test
  public void test_defaultInsertTrue() {

    DefaultChangeLogRegister register = new DefaultChangeLogRegister(true);

    assertNull(register.getChangeFilter(Address.class));

    ChangeLogFilter changeFilter = register.getChangeFilter(Customer.class);
    DefaultChangeLogRegister.UpdateFilter updateFilter = (DefaultChangeLogRegister.UpdateFilter)changeFilter;
    assertFalse(updateFilter.includeInserts);
    assertThat(updateFilter.updateProperties).containsExactly("name", "status");

    changeFilter = register.getChangeFilter(Country.class);
    DefaultChangeLogRegister.BasicFilter countryFilter = (DefaultChangeLogRegister.BasicFilter)changeFilter;
    assertTrue(countryFilter.includeInserts);

    // use default setting
    changeFilter = register.getChangeFilter(Contact.class);
    DefaultChangeLogRegister.BasicFilter contactFilter = (DefaultChangeLogRegister.BasicFilter)changeFilter;
    assertTrue(contactFilter.includeInserts);

  }

  @Test
  public void test_defaultInsertFalse() {

    DefaultChangeLogRegister register = new DefaultChangeLogRegister(false);

    assertNull(register.getChangeFilter(Address.class));

    ChangeLogFilter changeFilter = register.getChangeFilter(Customer.class);
    DefaultChangeLogRegister.UpdateFilter updateFilter = (DefaultChangeLogRegister.UpdateFilter)changeFilter;
    assertFalse(updateFilter.includeInserts);
    assertThat(updateFilter.updateProperties).containsExactly("name", "status");

    changeFilter = register.getChangeFilter(Country.class);
    DefaultChangeLogRegister.BasicFilter countryFilter = (DefaultChangeLogRegister.BasicFilter)changeFilter;
    assertTrue(countryFilter.includeInserts);

    // use default setting
    changeFilter = register.getChangeFilter(Contact.class);
    DefaultChangeLogRegister.BasicFilter contactFilter = (DefaultChangeLogRegister.BasicFilter)changeFilter;
    assertFalse(contactFilter.includeInserts);

  }

}
