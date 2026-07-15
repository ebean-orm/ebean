package org.tests.dtomapping;

import io.ebean.DB;
import org.junit.jupiter.api.Test;
import org.tests.dtomapping.model.Contact;
import org.tests.dtomapping.model.ContactStats;
import org.tests.dtomapping.model.Customer;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Worked example: {@code @Sum}/{@code @Aggregation} group-by formulas modelled as a second
 * {@code @Entity @View} read entity over the same {@code contact} table, mapped to a flat DTO.
 * See docs/dto-mapping-design.md, "Aggregate/group-by computed properties".
 */
class TestContactStatsDtoMapping {

  @Test
  void mapContactStats_expectCountAndSumGroupedByCustomer() {
    Customer customerA = new Customer("Quill Quarry");
    customerA.save();
    contact(customerA, "Ann", "Anders", 10);
    contact(customerA, "Bert", "Baxter", 20);
    contact(customerA, "Cleo", "Cross", 30);

    Customer customerB = new Customer("Rill Ridge");
    customerB.save();
    contact(customerB, "Dom", "Dune", 5);
    contact(customerB, "Eve", "Ellis", 15);

    ContactStatsDto statsA = DB.find(ContactStats.class)
      .where().eq("customer.id", customerA.getId())
      .mapTo(ContactStatsDto.class)
      .findOne();

    assertThat(statsA).isNotNull();
    assertThat(statsA.getCustomerId()).isEqualTo(customerA.getId());
    assertThat(statsA.getContactCount()).isEqualTo(3);
    assertThat(statsA.getEngagementScore()).isEqualTo(60);

    ContactStatsDto statsB = DB.find(ContactStats.class)
      .where().eq("customer.id", customerB.getId())
      .mapTo(ContactStatsDto.class)
      .findOne();

    assertThat(statsB).isNotNull();
    assertThat(statsB.getCustomerId()).isEqualTo(customerB.getId());
    assertThat(statsB.getContactCount()).isEqualTo(2);
    assertThat(statsB.getEngagementScore()).isEqualTo(20);
  }

  @Test
  void mapContactStats_findList_expectOneRowPerCustomer() {
    Customer customer = new Customer("Sable Springs");
    customer.save();
    contact(customer, "Fay", "Finch", 7);
    contact(customer, "Gus", "Gray", 13);

    List<ContactStatsDto> list = DB.find(ContactStats.class)
      .where().eq("customer.id", customer.getId())
      .mapTo(ContactStatsDto.class)
      .findList();

    assertThat(list).hasSize(1);
    ContactStatsDto dto = list.get(0);
    assertThat(dto.getCustomerId()).isEqualTo(customer.getId());
    assertThat(dto.getContactCount()).isEqualTo(2);
    assertThat(dto.getEngagementScore()).isEqualTo(20);
  }

  private void contact(Customer customer, String firstName, String lastName, int engagementScore) {
    Contact contact = new Contact(firstName, lastName, customer);
    contact.setEngagementScore(engagementScore);
    contact.save();
  }
}
