package org.tests.dtomapping;

import io.ebean.Database;
import io.ebean.DatabaseBuilder;
import io.ebean.DtoConverterManager;
import io.ebean.DtoMapperManager;
import org.junit.jupiter.api.Test;
import org.tests.dtomapping.model.Address;
import org.tests.dtomapping.model.Contact;
import org.tests.dtomapping.model.Customer;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Validates that {@link DtoMapperManager} can be constructed independently of any {@code
 * Database} (its constructor only does {@code ServiceLoader} discovery of generated {@code
 * DtoMapperRegister}s) and, once registered via {@code
 * DatabaseBuilder.putServiceObject(DtoMapperManager.class, ...)} before {@code build()}, is the
 * instance actually used internally by {@code query.mapTo(...)} - so the very same instance is
 * also directly usable elsewhere, e.g. wired up for constructor injection into application
 * services (replacing hand-written mapper wiring like {@code DriverMapper}/{@code DriverService}
 * in the motivating central-access example).
 */
class TestDtoMapperManagerSharing {

  @Test
  void putServiceObject_sharesSameManagerInstance_betweenMapToAndDirectInjection() {
    // a standalone construction bypasses the DatabaseConfigProvider hook, so this module's
    // @DtoConvert instance-dispatch converter must already be registered - see TestDtoMapperManager
    DtoConverterManager.put(SecretCipher.class, new ReverseSecretCipher());
    DtoMapperManager sharedManager = new DtoMapperManager();

    Database db = buildSecondaryDatabase(sharedManager);
    try {
      Customer customer = new Customer("Acme");
      db.save(customer);

      // query.mapTo(...) against this secondary Database resolves via the supplied manager
      CustomerDto dto = db.find(Customer.class)
        .where().idEq(customer.getId())
        .mapTo(CustomerDto.class)
        .findOne();

      assertThat(dto).isNotNull();
      assertThat(dto.getName()).isEqualTo("Acme");

      // the exact same manager instance we passed into putServiceObject resolves the same
      // generated mapper by its own concrete type - directly usable for e.g. constructor
      // injection into application services, independent of query.mapTo()
      CustomerDtoMapper mapper = sharedManager.get(CustomerDtoMapper.class);
      assertThat(mapper.map(customer).getName()).isEqualTo("Acme");
    } finally {
      db.shutdown();
    }
  }

  private static Database buildSecondaryDatabase(DtoMapperManager sharedManager) {
    DatabaseBuilder config = Database.builder();
    config.setName("dtoMapperManagerSharing");
    config.setRegister(false);
    config.setDefaultServer(false);
    config.setDdlGenerate(true);
    config.setDdlRun(true);
    config.setDdlExtra(false);

    Properties properties = new Properties();
    properties.setProperty("datasource.dtoMapperManagerSharing.username", "sa");
    properties.setProperty("datasource.dtoMapperManagerSharing.password", "");
    properties.setProperty("datasource.dtoMapperManagerSharing.databaseUrl", "jdbc:h2:mem:dtoMapperManagerSharing");
    properties.setProperty("datasource.dtoMapperManagerSharing.databaseDriver", "org.h2.Driver");
    config.loadFromProperties(properties);

    config.addClass(Customer.class);
    config.addClass(Contact.class);
    config.addClass(Address.class);

    config.putServiceObject(DtoMapperManager.class, sharedManager);
    return config.build();
  }
}
