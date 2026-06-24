package io.ebean.xtest.internal.server.text.json;

import io.avaje.json.JsonWriter;
import io.avaje.json.stream.JsonStream;
import io.ebean.DatabaseBuilder;
import io.ebean.config.DatabaseConfig;
import io.ebean.platform.h2.H2Platform;
import io.ebeaninternal.server.core.bootup.BootupClasses;
import io.ebeaninternal.server.json.DJsonScalar;
import io.ebeaninternal.server.type.DefaultTypeManager;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class DJsonScalarTest {

  private final DJsonScalar jsonScalar;
  private final JsonStream jsonStream = JsonStream.builder().build();

  public DJsonScalarTest() {
    var serverConfig = new DatabaseConfig();
    serverConfig.setDatabasePlatform(new H2Platform());
    DefaultTypeManager typeManager = new DefaultTypeManager(serverConfig, new BootupClasses());
    jsonScalar = new DJsonScalar(typeManager);
  }

  @Test
  public void writeBasicTypes() throws IOException {
    StringWriter writer = new StringWriter();
    JsonWriter generator = createGenerator(writer);

    UUID uuid = UUID.randomUUID();
    LocalDate today = LocalDate.now();

    generator.rawChunk('[');
    jsonScalar.write(generator, "hello");
    generator.rawChunk(',');
    jsonScalar.write(generator, uuid);
    generator.rawChunk(',');
    jsonScalar.write(generator, today);
    generator.rawChunk(']');

    generator.flush();

    String json = writer.toString();
    assertThat(json).contains("hello");
    assertThat(json).contains(uuid.toString());
  }

  private JsonWriter createGenerator(StringWriter writer) {
    return jsonStream.writer(writer);
  }

  @Test
  public void writeDbArrayTypes() throws IOException {

    StringWriter writer = new StringWriter();
    JsonWriter generator = createGenerator(writer);

    List<UUID> list = new ArrayList<>();
    list.add(UUID.randomUUID());
    list.add(UUID.randomUUID());

    jsonScalar.write(generator, list);

    generator.flush();

    String json = writer.toString();
    assertThat(json).isEqualTo("[\"" + list.get(0) + "\",\"" + list.get(1) + "\"]");
  }

}
