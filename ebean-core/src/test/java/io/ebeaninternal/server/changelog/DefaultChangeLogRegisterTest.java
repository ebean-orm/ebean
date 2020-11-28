package io.ebeaninternal.server.changelog;

import io.ebean.BaseTestCase;
import io.ebean.event.changelog.ChangeLogFilter;
import org.tests.inheritance.model.ProductConfiguration;
import org.tests.model.basic.Address;
import org.tests.model.basic.Contact;
import org.tests.model.basic.Country;
import org.tests.model.basic.Customer;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

public class DefaultChangeLogRegisterTest extends BaseTestCase {


  @Test
  public void test_defaultInsertTrue() {

    DefaultChangeLogRegister register = new DefaultChangeLogRegister(true);

    assertNull(register.getChangeFilter(Address.class));

    ChangeLogFilter changeFilter = register.getChangeFilter(Customer.class);
    DefaultChangeLogRegister.UpdateFilter updateFilter = (DefaultChangeLogRegister.UpdateFilter) changeFilter;
    assertFalse(updateFilter.includeInserts);
    assertThat(updateFilter.updateProperties).containsExactly("name", "status");

    changeFilter = register.getChangeFilter(Country.class);
    DefaultChangeLogRegister.BasicFilter countryFilter = (DefaultChangeLogRegister.BasicFilter) changeFilter;
    assertTrue(countryFilter.includeInserts);

    // use default setting
    changeFilter = register.getChangeFilter(Contact.class);
    DefaultChangeLogRegister.BasicFilter contactFilter = (DefaultChangeLogRegister.BasicFilter) changeFilter;
    assertTrue(contactFilter.includeInserts);

  }

  @Test
  public void test_defaultInsertFalse() {

    DefaultChangeLogRegister register = new DefaultChangeLogRegister(false);

    assertNull(register.getChangeFilter(Address.class));

    ChangeLogFilter changeFilter = register.getChangeFilter(Customer.class);
    DefaultChangeLogRegister.UpdateFilter updateFilter = (DefaultChangeLogRegister.UpdateFilter) changeFilter;
    assertFalse(updateFilter.includeInserts);
    assertThat(updateFilter.updateProperties).containsExactly("name", "status");

    changeFilter = register.getChangeFilter(Country.class);
    DefaultChangeLogRegister.BasicFilter countryFilter = (DefaultChangeLogRegister.BasicFilter) changeFilter;
    assertTrue(countryFilter.includeInserts);

    // use default setting
    changeFilter = register.getChangeFilter(Contact.class);
    DefaultChangeLogRegister.BasicFilter contactFilter = (DefaultChangeLogRegister.BasicFilter) changeFilter;
    assertFalse(contactFilter.includeInserts);

  }

  @Test
  public void test_inheritance() {

    DefaultChangeLogRegister register = new DefaultChangeLogRegister(true);
    ChangeLogFilter changeFilter = register.getChangeFilter(ProductConfiguration.class);
    assertNotNull(changeFilter);
    DefaultChangeLogRegister.BasicFilter basicFilter = (DefaultChangeLogRegister.BasicFilter) changeFilter;
    assertTrue(basicFilter.includeInserts);
  }

}
