package org.tests.query;

import io.ebean.DB;
import io.ebean.xtest.BaseTestCase;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Customer;
import org.tests.model.basic.ResetBasicData;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@code findOneOrThrow()} - a convenience alternative to
 * {@code findOneOrEmpty().orElseThrow(...)} that produces a decent default message
 * when the query is effectively a find-by-id or single natural/unique key lookup.
 */
class TestFindOneOrThrow extends BaseTestCase {

  @Test
  void findOneOrThrow_whenFound_returnsBean() {
    ResetBasicData.reset();
    Customer existing = DB.find(Customer.class).setMaxRows(1).findList().get(0);

    Customer found = DB.find(Customer.class).setId(existing.getId()).findOneOrThrow();

    assertThat(found.getId()).isEqualTo(existing.getId());
  }

  @Test
  void findOneOrThrow_whenNotFoundById_messageIncludesId() {
    ResetBasicData.reset();

    assertThatThrownBy(() -> DB.find(Customer.class).setId(999999).findOneOrThrow())
      .isInstanceOf(EntityNotFoundException.class)
      .hasMessage("Customer not found for id: 999999");
  }

  @Test
  void findOneOrThrow_whenNotFoundBySingleEqPredicate_messageIncludesPredicate() {
    ResetBasicData.reset();

    assertThatThrownBy(() -> DB.find(Customer.class)
        .where().eq("name", "NonExistentCustomerXYZ")
        .findOneOrThrow())
      .isInstanceOf(EntityNotFoundException.class)
      .hasMessage("Customer not found for name: NonExistentCustomerXYZ");
  }

  @Test
  void findOneOrThrow_whenNotFoundByMultiplePredicates_fallsBackToGenericMessage() {
    ResetBasicData.reset();

    assertThatThrownBy(() -> DB.find(Customer.class)
        .where().eq("name", "NonExistentCustomerXYZ").eq("id", 999999)
        .findOneOrThrow())
      .isInstanceOf(EntityNotFoundException.class)
      .hasMessage("Customer not found");
  }

  @Test
  void findOneOrThrow_withSupplier_usesSuppliedException() {
    ResetBasicData.reset();

    assertThatThrownBy(() -> DB.find(Customer.class).setId(999999)
        .findOneOrThrow(() -> new IllegalStateException("custom message")))
      .isInstanceOf(IllegalStateException.class)
      .hasMessage("custom message");
  }
}
