package io.ebeaninternal.json;

import io.avaje.json.JsonReader;
import io.avaje.json.JsonReader.Token;
import io.avaje.json.JsonWriter;
import io.avaje.json.mapper.JsonMapper;
import io.avaje.json.stream.JsonStream;
import io.ebean.service.SpiJsonService;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Utility that converts between JSON content and simple java Maps/Lists.
 * <p>
 * Backed by avaje {@link JsonMapper} using {@link EbeanJsonAdapter} which
 * preserves Ebean's modify-aware collection and number semantics.
 */
public final class DJsonService implements SpiJsonService {

  private static final JsonStream JSON_STREAM = JsonStream.builder().build();
  private static final JsonMapper MAPPER = JsonMapper.builder().jsonStream(JSON_STREAM).build();

  private static final JsonMapper.Type<Object> PLAIN = MAPPER.type(EbeanJsonAdapter.PLAIN);
  private static final JsonMapper.Type<Object> MODIFY_AWARE = MAPPER.type(EbeanJsonAdapter.MODIFY_AWARE);

  private static JsonMapper.Type<Object> type(boolean modifyAware) {
    return modifyAware ? MODIFY_AWARE : PLAIN;
  }

  private static boolean blank(String content) {
    return content == null || content.trim().isEmpty();
  }

  private static String readAll(Reader reader) throws IOException {
    StringBuilder builder = new StringBuilder();
    char[] buffer = new char[2048];
    int len;
    while ((len = reader.read(buffer)) != -1) {
      builder.append(buffer, 0, len);
    }
    return builder.toString();
  }

  @Override
  public String write(Object object) throws IOException {
    StringWriter writer = new StringWriter();
    write(object, writer);
    return writer.toString();
  }

  @Override
  public void write(Object object, Writer writer) throws IOException {
    JsonWriter jsonWriter = JSON_STREAM.writer(writer);
    jsonWriter.serializeNulls(true);
    PLAIN.toJson(object, jsonWriter);
    jsonWriter.flush();
  }

  @Override
  public void write(Object object, JsonWriter jsonWriter) throws IOException {
    PLAIN.toJson(object, jsonWriter);
  }

  @Override
  public void writeCollection(Collection<Object> collection, JsonWriter jsonWriter) throws IOException {
    EbeanJsonAdapter.writeCollection(jsonWriter, collection);
  }

  @Override
  @SuppressWarnings("unchecked")
  public Map<String, Object> parseObject(String json, boolean modifyAware) throws IOException {
    return blank(json) ? null : (Map<String, Object>) type(modifyAware).fromJson(json);
  }

  @Override
  public Map<String, Object> parseObject(String json) throws IOException {
    return parseObject(json, false);
  }

  @Override
  public Map<String, Object> parseObject(Reader reader, boolean modifyAware) throws IOException {
    return parseObject(readAll(reader), modifyAware);
  }

  @Override
  public Map<String, Object> parseObject(Reader reader) throws IOException {
    return parseObject(reader, false);
  }

  @Override
  @SuppressWarnings("unchecked")
  public Map<String, Object> parseObject(JsonReader parser) throws IOException {
    return (Map<String, Object>) PLAIN.fromJson(parser);
  }

  @Override
  @SuppressWarnings("unchecked")
  public Map<String, Object> parseObject(JsonReader parser, Token token) throws IOException {
    return (Map<String, Object>) EbeanJsonAdapter.read(parser, token, false);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> List<T> parseList(String json, boolean modifyAware) throws IOException {
    return blank(json) ? null : (List<T>) type(modifyAware).fromJson(json);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<Object> parseList(String json) throws IOException {
    return (List<Object>) parseList(json, false);
  }

  @Override
  public List<Object> parseList(Reader reader) throws IOException {
    return parseList(readAll(reader));
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<Object> parseList(JsonReader parser) throws IOException {
    return (List<Object>) PLAIN.fromJson(parser);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> List<T> parseList(JsonReader parser, Token currentToken) throws IOException {
    return (List<T>) EbeanJsonAdapter.read(parser, currentToken, false);
  }

  @Override
  public Object parse(String json) throws IOException {
    return blank(json) ? null : PLAIN.fromJson(json);
  }

  @Override
  public Object parse(Reader reader) throws IOException {
    return parse(readAll(reader));
  }

  @Override
  public Object parse(JsonReader parser) throws IOException {
    return PLAIN.fromJson(parser);
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
