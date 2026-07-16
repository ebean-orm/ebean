package org.tests.dtomapping;

import io.ebean.DB;
import org.junit.jupiter.api.Test;
import org.tests.dtomapping.model.Contact;
import org.tests.dtomapping.model.ContactSummary;
import org.tests.dtomapping.model.Customer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Worked example: a Blaze-Persistence-style computed DTO property ({@code fullName}) achieved
 * via a {@code @View}-mapped read entity ({@link ContactSummary}, pointed at the same table as
 * {@link Contact}) carrying a {@code @Formula2} computed property, mapped into a plain DTO
 * ({@link ContactSummaryDto}) with the existing {@code @DtoMapping} machinery - no new
 * ad-hoc-SQL-on-DTO annotation needed. See docs/dto-mapping-design.md, "Ad-hoc computed/formula
 * properties: model as {@code @Entity @View}/{@code @Sql}, not ad-hoc SQL-on-DTO".
 */
class TestContactSummaryDtoMapping {

  private final ContactSummaryDtoMapper mapper = new ContactSummaryDtoMapper();

  @Test
  void mapContactSummary_expectComputedFullName() {
    Customer customer = new Customer("Acme");
    customer.save();
    new Contact("Zenith", "Zolton", customer).save();

    ContactSummary summary = DB.find(ContactSummary.class)
      .setUnmodifiable(true)
      .where().eq("firstName", "Zenith").eq("lastName", "Zolton")
      .findOne();

    ContactSummaryDto dto = mapper.map(summary);

    assertThat(dto.getId()).isEqualTo(summary.getId());
    assertThat(dto.getFullName()).isEqualTo("Zenith Zolton");
  }

  @Test
  void mapContactSummary_whenNull_expectNull() {
    assertThat(mapper.map(null)).isNull();
  }
}
