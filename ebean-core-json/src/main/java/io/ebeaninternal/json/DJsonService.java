package io.ebeaninternal.json;

import io.avaje.json.JsonReader;
import io.avaje.json.JsonReader.Token;
import io.avaje.json.JsonWriter;
import io.ebean.service.SpiJsonService;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Utility that converts between JSON content and simple java Maps/Lists.
 */
public final class DJsonService implements SpiJsonService {

  @Override
  public String write(Object object) throws IOException {
    return EJsonWriter.write(object);
  }

  @Override
  public void write(Object object, Writer writer) throws IOException {
    EJsonWriter.write(object, writer);
  }

  @Override
  public void write(Object object, JsonWriter jsonWriter) throws IOException {
    EJsonWriter.write(object, jsonWriter);
  }

  @Override
  public void writeCollection(Collection<Object> collection, JsonWriter jsonWriter) throws IOException {
    EJsonWriter.writeCollection(collection, jsonWriter);
  }

  @Override
  public Map<String, Object> parseObject(String json, boolean modifyAware) throws IOException {
    return EJsonReader.parseObject(json, modifyAware);
  }

  @Override
  public Map<String, Object> parseObject(String json) throws IOException {
    return EJsonReader.parseObject(json);
  }

  @Override
  public Map<String, Object> parseObject(Reader reader, boolean modifyAware) throws IOException {
    return EJsonReader.parseObject(reader, modifyAware);
  }

  @Override
  public Map<String, Object> parseObject(Reader reader) throws IOException {
    return EJsonReader.parseObject(reader);
  }

  @Override
  public Map<String, Object> parseObject(JsonReader parser) throws IOException {
    return EJsonReader.parseObject(parser);
  }

  @Override
  public Map<String, Object> parseObject(JsonReader parser, Token token) throws IOException {
    return EJsonReader.parseObject(parser, token);
  }

  @Override
  public <T> List<T> parseList(String json, boolean modifyAware) throws IOException {
    return EJsonReader.parseList(json, modifyAware);
  }

  @Override
  public List<Object> parseList(String json) throws IOException {
    return EJsonReader.parseList(json);
  }

  @Override
  public List<Object> parseList(Reader reader) throws IOException {
    return EJsonReader.parseList(reader);
  }

  @Override
  public List<Object> parseList(JsonReader parser) throws IOException {
    return EJsonReader.parseList(parser, false);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> List<T> parseList(JsonReader parser, Token currentToken) throws IOException {
    return (List<T>) EJsonReader.parse(parser, currentToken, false);
  }

  @Override
  public Object parse(String json) throws IOException {
    return EJsonReader.parse(json);
  }

  @Override
  public Object parse(Reader reader) throws IOException {
    return EJsonReader.parse(reader);
  }

  @Override
  public Object parse(JsonReader parser) throws IOException {
    return EJsonReader.parse(parser);
  }

  @Override
  public <T> Set<T> parseSet(String json, boolean modifyAware) throws IOException {
    List<T> list = parseList(json, modifyAware);
    if (list == null) {
      return null;
    }
    if (modifyAware) {
      return ((ModifyAwareList<T>) list).asSet();
    }
    return new LinkedHashSet<>(list);
  }

  @Override
  public <T> Set<T> parseSet(JsonReader parser, Token currentToken) throws IOException {
    return new LinkedHashSet<>(parseList(parser, currentToken));
  }
}
