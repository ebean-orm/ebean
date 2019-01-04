package org.tests.model.docstore;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import io.ebean.Ebean;
import io.ebean.FetchPath;
import io.ebean.Query;
import io.ebean.config.JsonConfig.Include;
import io.ebean.text.json.JsonWriteOptions;

public class EbeanJsonSerializer<T> extends JsonSerializer<T> {

  @Override
  public void serialize(T value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
    JsonWriteOptions options = new JsonWriteOptions();
    options.setPathProperties(new FocPathProperties());
    options.setForceReference(true);
    options.setInclude(Include.NON_EMPTY);
    Ebean.json().toJson(value, gen, options);
  }

  public static class FocPathProperties implements FetchPath {

    @Override
    public boolean hasPath(final String path) {
        return true; // wir haben alles
    }

    @Override
    public Set<String> getProperties(final String path) {
        return new HashSet<String>(Arrays.asList("*")); // und wir erzwingen so immer "ExplicitAllProperties"
    }

    @Override
    public <T> void apply(final Query<T> query) {
        throw new UnsupportedOperationException();

    }

}

}
