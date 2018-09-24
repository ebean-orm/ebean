package org.tests.repository;

import io.ebean.BaseTestCase;
import org.junit.Test;
import org.tests.model.basic.Customer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class TestBeanRepository extends BaseTestCase {

  @Test
  public void test() {

    CustomerRepository repository = new CustomerRepository(server());

    Customer customer = new Customer();
    customer.setName("RepoCustomer");

    repository.save(customer);

    Customer fetchCustomer = repository.findById(customer.getId());
    fetchCustomer.setSmallnote("yeah maybe");

    repository.update(fetchCustomer);
    repository.delete(fetchCustomer);

    List<Customer> custs = new ArrayList<>();
    custs.add(newCustomer("c0"));
    custs.add(newCustomer("c1"));

    int count = repository.saveAll(custs);
    assertThat(count).isEqualTo(2);

    count = repository.deleteAll(custs);
    assertThat(count).isEqualTo(2);
  }

  private Customer newCustomer(String name) {
    Customer customer = new Customer();
    customer.setName(name);
    return customer;
  }

  @Test
  public void findByName() {

    CustomerRepository repository = new CustomerRepository(server());

    Customer blah = new Customer();
    blah.setName("Blah");

    repository.markAsDirty(blah);
    repository.markPropertyUnset(blah, "smallnote");
    repository.save(blah);

    Optional<Customer> maybe = repository.findByIdOrEmpty(blah.getId());
    assertThat(maybe.isPresent()).isTrue();

    List<Customer> names = repository.findByName("bla");
    assertThat(names).hasSize(1);

    Customer matchName = repository.findMatchName("Blah");
    assertThat(matchName).isNotNull();

    int rows = repository.updateNotes("Blah", "whot");
    assertThat(rows).isEqualTo(1);

    repository.deletePermanent(blah);

    repository.deleteById(1099);
    repository.findAll();

  }
}
