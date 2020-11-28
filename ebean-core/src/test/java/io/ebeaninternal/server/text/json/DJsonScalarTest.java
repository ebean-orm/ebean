package io.ebeaninternal.server.text.json;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import io.ebean.config.ServerConfig;
import io.ebean.config.dbplatform.h2.H2Platform;
import io.ebeaninternal.server.core.bootup.BootupClasses;
import io.ebeaninternal.server.type.DefaultTypeManager;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class DJsonScalarTest {

  private final DJsonScalar jsonScalar;

  public DJsonScalarTest() {

    ServerConfig serverConfig = new ServerConfig();
    serverConfig.setDatabasePlatform(new H2Platform());
    DefaultTypeManager typeManager = new DefaultTypeManager(serverConfig, new BootupClasses());
    jsonScalar = new DJsonScalar(typeManager);
  }

  @Test
  public void writeBasicTypes() throws IOException {

    StringWriter writer = new StringWriter();
    JsonGenerator generator = createGenerator(writer);

    UUID uuid = UUID.randomUUID();
    LocalDate today = LocalDate.now();

    generator.writeRaw("[");
    jsonScalar.write(generator, "hello");
    generator.writeRaw(",");
    jsonScalar.write(generator, uuid);
    generator.writeRaw(",");
    jsonScalar.write(generator, today);
    generator.writeRaw("]");

    generator.flush();
    generator.close();

    String json = writer.toString();
    assertThat(json).contains("hello");
    assertThat(json).contains(uuid.toString());
  }

  private JsonGenerator createGenerator(StringWriter writer) throws IOException {
    JsonFactory factory = new JsonFactory();
    return factory.createGenerator(writer);
  }

  @Test
  public void writeDbArrayTypes() throws IOException {

    StringWriter writer = new StringWriter();
    JsonGenerator generator = createGenerator(writer);

    List<UUID> list = new ArrayList<>();
    list.add(UUID.randomUUID());
    list.add(UUID.randomUUID());

    jsonScalar.write(generator, list);

    generator.flush();
    generator.close();

    String json = writer.toString();
    assertThat(json).isEqualTo("[\""+list.get(0)+"\", \""+list.get(1)+"\"]");
  }

}
