package org.tests.model.docstore;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import io.ebean.DB;
import io.ebean.FetchPath;
import io.ebean.Query;
import io.ebean.text.json.JsonWriteOptions;

import java.io.IOException;
import java.util.Set;

/**
 * Serializer, that advises Jackson to use ebean serialization instead.
 * <p>
 * It also handles the "id" serialization of referenced properties.
 * <p>
 * Note: This is a very simple class stripped down to provide a test case
 * (we use a more generic one in our code)
 */
public class ReportJsonSerializer extends JsonSerializer<Report> {

  private static final FetchPath SERIALIZE_TO_DISK = new FetchPath() {
    @Override
    public boolean hasPath(final String path) {
      return true;
    }

    @Override
    public Set<String> getProperties(final String path) {
      if (path == null) {
        return Set.of("*");  // for json model itself, serialize all properties
      } else {
        return Set.of("id"); // for referenced DB-beans (e.g. customers), serialize id only
      }
    }

    @Override
    public <T> void apply(final Query<T> query) {
      throw new UnsupportedOperationException();
    }
  };

  @Override
  public void serialize(final Report value, final JsonGenerator jgen, final SerializerProvider serializers) throws IOException {
    JsonWriteOptions options = new JsonWriteOptions();
    options.setPathProperties(SERIALIZE_TO_DISK);
    DB.json().toJson(value, jgen, options);
  }

}
