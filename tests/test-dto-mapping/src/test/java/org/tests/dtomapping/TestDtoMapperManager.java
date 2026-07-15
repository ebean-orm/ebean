package org.tests.dtomapping;

import io.ebean.DtoMapper;
import io.ebean.DtoMapperManager;
import io.ebean.DtoConverterManager;
import jakarta.persistence.PersistenceException;
import org.junit.jupiter.api.Test;
import org.tests.dtomapping.model.Customer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link DtoMapperManager} has no dependency on {@code Database} - its constructor only does
 * {@code ServiceLoader} discovery of generated {@code DtoMapperRegister}s - so it can be
 * constructed standalone, independent of (or before) any {@code Database}.
 * <p>
 * A standalone construction like this bypasses the {@code DatabaseConfigProvider} hook (that
 * hook is specifically about {@code Database} startup ordering) - so any {@code @DtoConvert}
 * instance-dispatch converter this module's mappers need (here, {@link SecretCipher}) must
 * already be registered via {@link DtoConverterManager#put} by the application itself before
 * constructing its own {@code DtoMapperManager}, exactly as it would before building a
 * {@code Database}.
 */
class TestDtoMapperManager {

  private final DtoMapperManager manager = manager();

  private static DtoMapperManager manager() {
    DtoConverterManager.put(SecretCipher.class, new ReverseSecretCipher());
    return new DtoMapperManager();
  }

  @Test
  void get_byMapperType_returnsTheGeneratedMapperInstance() {
    CustomerDtoMapper mapper = manager.get(CustomerDtoMapper.class);

    assertThat(mapper).isNotNull();
    // repeated lookups return the exact same cached instance
    assertThat(manager.get(CustomerDtoMapper.class)).isSameAs(mapper);
  }

  @Test
  void get_andMapperFor_resolveToTheSameUnderlyingMapperInstance() {
    CustomerDtoMapper byType = manager.get(CustomerDtoMapper.class);
    DtoMapper<Customer, CustomerDto> byPair = manager.mapperFor(Customer.class, CustomerDto.class);

    assertThat(byType).isSameAs(byPair);
  }

  @Test
  void get_whenMapperTypeNotRegistered_expectPersistenceException() {
    assertThatThrownBy(() -> manager.get(UnregisteredMapper.class))
      .isInstanceOf(PersistenceException.class)
      .hasMessageContaining("No DtoMapper of type " + UnregisteredMapper.class.getName() + " registered");
  }

  /** Deliberately never generated - used to prove {@link DtoMapperManager#get} fails fast. */
  private interface UnregisteredMapper {
  }
}
