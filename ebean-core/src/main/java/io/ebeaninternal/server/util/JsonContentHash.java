package io.ebeaninternal.server.util;

import io.avaje.json.JsonReader;
import io.avaje.json.stream.JsonStream;

/**
 * Compute an order-independent structural hash of JSON content.
 * <p>
 * Object key ordering does not affect the hash value, while array element ordering does.
 */
public final class JsonContentHash {

  private static final JsonStream STREAM = JsonStream.builder().build();

  // Type markers to distinguish empty object {}, empty array [], and null
  private static final long OBJECT_SEED = 0x7A5662B4E8B10FA3L;
  private static final long ARRAY_SEED = 0x3C6EF372FE94F82BL;
  private static final long TRUE_HASH = 0x9E3779B97F4A7C15L;
  private static final long FALSE_HASH = 0x517CC1B727220A95L;
  private static final long NULL_HASH = 0x6C62272E07BB0142L;

  private JsonContentHash() {
  }

  /**
   * Compute an order-independent hash of JSON content.
   */
  public static long hash(String json) {
    if (json == null || json.isEmpty()) {
      return 0L;
    }
    try (JsonReader reader = STREAM.reader(json)) {
      return hash(reader, firstNonWhitespace(json));
    } catch (RuntimeException e) {
      // Fallback to regular string hash if JSON is malformed.
      return stringHash(json);
    }
  }

  private static long hash(JsonReader reader, char firstToken) {
    switch (firstToken) {
      case '{':
        reader.beginObject();
        return hashObject(reader);
      case '[':
        reader.beginArray();
        return hashArray(reader);
      case '"':
        return mix(stringHash(reader.readString()));
      case 't':
      case 'f':
        return reader.readBoolean() ? TRUE_HASH : FALSE_HASH;
      case 'n':
        if (reader.isNullValue()) {
          return NULL_HASH;
        }
        throw new IllegalStateException("Invalid null token");
      default:
        return mix(stringHash(reader.readDecimal().toString()));
    }
  }

  private static long hashValue(JsonReader reader) {
    JsonReader.Token token = reader.currentToken();
    switch (token) {
      case BEGIN_OBJECT:
        reader.beginObject();
        return hashObject(reader);
      case BEGIN_ARRAY:
        reader.beginArray();
        return hashArray(reader);
      case STRING:
        return mix(stringHash(reader.readString()));
      case NUMBER:
        return mix(stringHash(reader.readDecimal().toString()));
      case BOOLEAN:
        return reader.readBoolean() ? TRUE_HASH : FALSE_HASH;
      case NULL:
        if (reader.isNullValue()) {
          return NULL_HASH;
        }
        throw new IllegalStateException("Invalid null token");
      default:
        throw new IllegalStateException("Unhandled token " + token);
    }
  }

  private static long hashObject(JsonReader reader) {
    long hash = OBJECT_SEED;
    while (reader.hasNextField()) {
      String key = reader.nextField();
      long keyHash = stringHash(key);
      long valueHash = hashValue(reader);
      hash += mix(keyHash * 0x9E3779B97F4A7C15L + valueHash);
    }
    reader.endObject();
    return hash;
  }

  private static long hashArray(JsonReader reader) {
    long hash = ARRAY_SEED;
    while (reader.hasNextElement()) {
      hash = hash * 31 + hashValue(reader);
    }
    reader.endArray();
    return mix(hash);
  }

  private static char firstNonWhitespace(String json) {
    for (int i = 0; i < json.length(); i++) {
      char c = json.charAt(i);
      if (!Character.isWhitespace(c)) {
        return c;
      }
    }
    return 0;
  }

  /**
   * 64-bit FNV-1a inspired string hash for better distribution than String.hashCode().
   */
  private static long stringHash(String s) {
    long h = 0xcbf29ce484222325L;
    for (int i = 0; i < s.length(); i++) {
      h ^= s.charAt(i);
      h *= 0x100000001b3L;
    }
    return h;
  }

  /**
   * Mixing/finalizer function to improve hash distribution and break additive symmetry.
   */
  private static long mix(long h) {
    h ^= (h >>> 33);
    h *= 0xff51afd7ed558ccdL;
    h ^= (h >>> 33);
    h *= 0xc4ceb9fe1a85ec53L;
    h ^= (h >>> 33);
    return h;
  }
}
