package org.tests.query;

import io.ebean.DB;
import io.ebean.test.LoggedSql;
import io.ebean.xtest.BaseTestCase;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Contact;
import org.tests.model.basic.Customer;
import org.tests.model.basic.ResetBasicData;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Reproduces a bug where filterMany() on a property of the many-root itself is misapplied
 * to the ON clause of a deeper, unrelated nested fetch join instead of the many-root's own join
 * clause - when an additional fetch path exists beneath the many property that the filterMany
 * expression itself does not reference.
 */
public class TestQueryFilterManyWithDeeperNestedFetch extends BaseTestCase {

  @Test
  void filterMany_onRootProperty_withUnrelatedDeeperNestedFetch_expectFilterOnOwnJoin() {
    ResetBasicData.reset();

    Customer customer = DB.find(Customer.class).where().ieq("name", "Rob").findOne();
    assertThat(customer).isNotNull();

    List<Contact> allContacts = DB.find(Contact.class).where().eq("customer", customer).findList();
    assertThat(allContacts).isNotEmpty();

    // ensure at least one contact isMember=true and one isMember=false
    Contact memberContact = allContacts.get(0);
    memberContact.setMember(true);
    DB.save(memberContact);
    Contact nonMemberContact;
    if (allContacts.size() > 1) {
      nonMemberContact = allContacts.get(1);
    } else {
      nonMemberContact = new Contact();
      nonMemberContact.setFirstName("Extra");
      nonMemberContact.setLastName("NonMember");
      nonMemberContact.setCustomer(customer);
    }
    nonMemberContact.setMember(false);
    DB.save(nonMemberContact);

    LoggedSql.start();
    // filterMany only references "isMember" (a property of Contact - the many-root itself) but
    // the query ALSO fetches a further nested path beneath "contacts" (contacts.group) that the
    // filterMany expression does NOT reference at all.
    List<Customer> found = DB.find(Customer.class)
      .setBeanCacheMode(io.ebean.CacheMode.OFF)
      .setPersistenceContextScope(io.ebean.PersistenceContextScope.QUERY)
      .fetch("contacts", "id,firstName,lastName,isMember")
      .fetch("contacts.group", "id,name")
      .filterMany("contacts").eq("isMember", true)
      .where().idEq(customer.getId())
      .findList();

    List<String> sql = LoggedSql.stop();
    assertThat(found).isNotEmpty();
    assertThat(sql).hasSize(1);
    // the filterMany predicate must be on contact's own join (t1), not misapplied to the
    // unrelated deeper contact_group join (t2)
    assertThat(sql.get(0)).contains("left join contact t1 on t1.customer_id = t0.id and t1.is_member = ? left join contact_group t2 on t2.id = t1.group_id");

    // every contact returned must be a member - the filter must actually exclude non-members
    assertThat(found.get(0).getContacts()).isNotEmpty();
    assertThat(found.get(0).getContacts()).allMatch(Contact::isMember);
  }
}
