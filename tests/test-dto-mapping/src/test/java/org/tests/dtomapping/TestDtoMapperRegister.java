package org.tests.dtomapping;

import io.ebean.DtoMapper;
import io.ebean.config.DtoMapperRegister;
import org.junit.jupiter.api.Test;
import org.tests.dtomapping.model.Address;
import org.tests.dtomapping.model.Contact;
import org.tests.dtomapping.model.Customer;

import java.util.ServiceLoader;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Validates the generated {@code EbeanDtoMapperRegister} is discoverable via
 * {@code META-INF/services/io.ebean.config.DtoMapperRegister} and correctly dispatches on the
 * (source, target) pair - see docs/dto-mapping-design.md (requirement dto-codegen-mapper).
 */
class TestDtoMapperRegister {

  private final DtoMapperRegister register = ServiceLoader.load(DtoMapperRegister.class)
    .findFirst()
    .orElseThrow(() -> new IllegalStateException("No DtoMapperRegister found via ServiceLoader"));

  @Test
  void mapperFor_whenSourceAndTargetMatch_expectMapper() {
    DtoMapper<Customer, CustomerDto> mapper = register.mapperFor(Customer.class, CustomerDto.class);

    assertThat(mapper).isInstanceOf(CustomerDtoMapper.class);
  }

  @Test
  void mapperFor_whenDifferentTargetForSameSource_expectDifferentMapper() {
    DtoMapper<Customer, CustomerRefDto> refMapper = register.mapperFor(Customer.class, CustomerRefDto.class);

    assertThat(refMapper).isInstanceOf(CustomerRefDtoMapper.class);
  }

  @Test
  void mapperFor_whenSourceDoesNotMatchTarget_expectNull() {
    // CustomerDto only maps from Customer, not Contact - so this pair is unregistered.
    DtoMapper<Contact, CustomerDto> mapper = register.mapperFor(Contact.class, CustomerDto.class);

    assertThat(mapper).isNull();
  }

  @Test
  void mapperFor_whenUnregisteredPair_expectNull() {
    DtoMapper<Address, CustomerDto> mapper = register.mapperFor(Address.class, CustomerDto.class);

    assertThat(mapper).isNull();
  }
}
