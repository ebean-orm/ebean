package org.tests.dtomapping;

import io.ebean.DB;
import io.ebean.LazyInitialisationException;
import io.ebean.PagedList;
import io.ebean.Transaction;
import io.ebean.test.LoggedSql;
import jakarta.persistence.PersistenceException;
import org.junit.jupiter.api.Test;
import org.tests.dtomapping.model.Address;
import org.tests.dtomapping.model.Contact;
import org.tests.dtomapping.model.Customer;
import org.tests.dtomapping.model.query.QCustomer;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * End-to-end validation of {@code query.mapTo(Dto.class)} - the full-scope runtime wiring for
 * issue #2540. Unlike {@link TestCustomerDtoGraphMapping} (which calls the generated mapper
 * directly against a manually fetched entity graph), these tests exercise the entire pipeline:
 * resolving the generated {@code DtoMapper} via {@code ServiceLoader}, automatically applying its
 * {@code fetchGroup()} (derived from the DTO's declared shape - no manual {@code .select()}/
 * {@code .fetch()} needed), forcing {@code setUnmodifiable(true)}, then mapping the result.
 */
class TestQueryMapTo {

  @Test
  void mapTo_findList_expectAutoFetchSpecAppliedAndPopulatedDtoGraph() {
    Address address = new Address("12 Test Street", "Auckland");
    address.save();

    Customer customer = new Customer("Acme");
    customer.setBillingAddress(address);
    customer.save();
    new Contact("Jane", "Doe", customer).save();
    new Contact("John", "Doe", customer).save();

    // no .select()/.fetch() at all - the fetch spec is derived from CustomerDto's shape
    List<CustomerDto> dtos = DB.find(Customer.class)
      .where().idEq(customer.getId())
      .mapTo(CustomerDto.class)
      .findList();

    assertThat(dtos).hasSize(1);
    CustomerDto dto = dtos.get(0);
    assertThat(dto.getId()).isEqualTo(customer.getId());
    assertThat(dto.getName()).isEqualTo("Acme");
    assertThat(dto.getBillingAddress().getLine1()).isEqualTo("12 Test Street");
    assertThat(dto.getBillingAddress().getCity()).isEqualTo("Auckland");
    assertThat(dto.getContacts()).hasSize(2);
    assertThat(dto.getContacts().get(0).getCustomer()).isSameAs(dto.getContacts().get(1).getCustomer());
    // @DtoRef (id-only) and @DtoPath (multi-hop) properties on ContactDto
    assertThat(dto.getContacts().get(0).getCustomerId()).isEqualTo(customer.getId());
    assertThat(dto.getContacts().get(0).getCustomerCity()).isEqualTo("Auckland");
  }

  @Test
  void mapTo_findOne_expectPopulatedDto() {
    Customer customer = new Customer("Beta");
    customer.save();

    CustomerDto dto = DB.find(Customer.class)
      .where().idEq(customer.getId())
      .mapTo(CustomerDto.class)
      .findOne();

    assertThat(dto).isNotNull();
    assertThat(dto.getName()).isEqualTo("Beta");
  }

  @Test
  void mapTo_findOneOrEmpty_whenNoMatch_expectEmpty() {
    Optional<CustomerDto> dto = new QCustomer()
      .id.eq(-1L)
      .mapTo(CustomerDto.class)
      .findOneOrEmpty();

    assertThat(dto).isEmpty();
  }

  @Test
  void mapTo_whenPairNotRegistered_expectPersistenceException() {
    assertThatThrownBy(() -> DB.find(Contact.class).mapTo(CustomerDto.class).findList())
      .isInstanceOf(PersistenceException.class)
      .hasMessageContaining("No DtoMapper registered");
  }

  @Test
  void mapTo_findPagedList_expectPagedMappedDtos() {
    new Customer("PageA").save();
    new Customer("PageB").save();
    new Customer("PageC").save();

    PagedList<CustomerDto> paged = DB.find(Customer.class)
      .where().startsWith("name", "Page")
      .orderBy().asc("name")
      .setFirstRow(0)
      .setMaxRows(2)
      .mapTo(CustomerDto.class)
      .findPagedList();

    assertThat(paged.getTotalCount()).isEqualTo(3);
    assertThat(paged.hasNext()).isTrue();
    assertThat(paged.hasPrev()).isFalse();
    List<CustomerDto> page1 = paged.getList();
    assertThat(page1).extracting(CustomerDto::getName).containsExactly("PageA", "PageB");
    // getList() is cached - same instance on repeated calls
    assertThat(paged.getList()).isSameAs(page1);
  }

  @Test
  void mapTo_whenSelectAlreadySet_expectManualFetchSpecPreserved() {
    Address address = new Address("1 Manual Street", "Wellington");
    address.save();

    Customer customer = new Customer("ManualSelect");
    customer.setBillingAddress(address);
    customer.save();
    new Contact("Jane", "Doe", customer).save();

    LoggedSql.start();
    // manually tuned select() - deliberately narrower than CustomerDto's own fetch spec
    // (excludes billingAddress and contacts) - this should be left untouched by mapTo()
    var query = DB.find(Customer.class)
      .select("name")
      .where().idEq(customer.getId())
      .mapTo(CustomerDto.class);

    // mapper.fetchGroup() was NOT merged over the top of the manual select() - so mapping
    // billingAddress (not fetched) fails fast rather than silently lazy loading it
    assertThatThrownBy(query::findList)
      .isInstanceOf(LazyInitialisationException.class)
      .hasMessageContaining("billingAddress");

    List<String> sql = LoggedSql.stop();
    // only the manually selected properties were fetched - no join to address
    assertThat(sql).hasSize(1);
    assertThat(sql.get(0)).doesNotContain("o_address");
  }

  @Test
  void mapTo_usingMaster_expectFluentReturnAndNoError() {
    Customer customer = new Customer("MasterCo");
    customer.save();

    // usingMaster() can be called on the MappedQuery itself - e.g. on retry after a read-replica
    // failure - without needing to rebuild the underlying query and call mapTo() again
    var mapped = DB.find(Customer.class)
      .where().idEq(customer.getId())
      .mapTo(CustomerDto.class);

    assertThat(mapped.usingMaster(false)).isSameAs(mapped);
    assertThat(mapped.usingMaster(true)).isSameAs(mapped);

    List<CustomerDto> dtos = mapped.findList();
    assertThat(dtos).hasSize(1);
    assertThat(dtos.get(0).getName()).isEqualTo("MasterCo");
  }

  @Test
  void mapTo_usingTransaction_expectQueryRunsOnExplicitTransaction() {
    Customer customer = new Customer("TxnCo");
    customer.save();

    try (Transaction txn = DB.beginTransaction()) {
      List<CustomerDto> dtos = DB.find(Customer.class)
        .where().idEq(customer.getId())
        .mapTo(CustomerDto.class)
        .usingTransaction(txn)
        .findList();

      assertThat(dtos).hasSize(1);
      assertThat(dtos.get(0).getName()).isEqualTo("TxnCo");
      txn.commit();
    }
  }

  @Test
  void mapTo_usingConnection_expectFluentReturn() {
    Customer customer = new Customer("ConnCo");
    customer.save();

    try (Transaction txn = DB.beginTransaction()) {
      var mapped = DB.find(Customer.class)
        .where().idEq(customer.getId())
        .mapTo(CustomerDto.class);

      assertThat(mapped.usingConnection(txn.connection())).isSameAs(mapped);

      List<CustomerDto> dtos = mapped.findList();
      assertThat(dtos).hasSize(1);
      assertThat(dtos.get(0).getName()).isEqualTo("ConnCo");
    }
  }

  @Test
  void mapTo_withFilterMany_expectFilteredContactsInDtoGraph() {
    Customer customer = new Customer("FilterManyCo");
    customer.save();
    new Contact("Jane", "Doe", customer).save();
    Contact inactive = new Contact("John", "Doe", customer);
    inactive.setActive(false);
    inactive.save();

    // filterMany() combined with the DTO mapper's auto-applied fetchGroup() - contacts is part
    // of CustomerDto's own fetch spec, filterMany() narrows the rows fetched for that relationship
    List<CustomerDto> dtos = DB.find(Customer.class)
      .filterMany("contacts").eq("active", true)
      .where().idEq(customer.getId())
      .mapTo(CustomerDto.class)
      .findList();

    assertThat(dtos).hasSize(1);
    CustomerDto dto = dtos.get(0);
    assertThat(dto.getContacts()).hasSize(1);
    assertThat(dto.getContacts().get(0).getFirstName()).isEqualTo("Jane");
  }
}
