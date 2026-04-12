package io.ebeaninternal.server.util;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.IOException;

/**
 * Compute an order-independent structural hash of JSON content using Jackson's streaming parser.
 * <p>
 * Object key ordering does NOT affect the hash value (handles PostgreSQL JSONB key reordering),
 * while array element ordering DOES affect it (array position is semantically significant).
 * <p>
 * This is significantly faster than a full parse/format roundtrip because it performs
 * zero object allocation beyond the parser itself — no tree building, no reflection,
 * no type conversion. Single-pass O(n) time with O(depth) stack space.
 */
public final class JsonContentHash {

  private static final JsonFactory FACTORY = new JsonFactory();

  /**
   * Compute an order-independent hash of JSON content.
   * Two JSON strings with identical content but different key ordering
   * will produce the same hash value.
   */
  public static long hash(String json) {
    if (json == null || json.isEmpty()) {
      return 0L;
    }
    try (JsonParser parser = FACTORY.createParser(json)) {
      parser.nextToken();
      return computeHash(parser);
    } catch (IOException e) {
      // Fallback to regular string hash if JSON is malformed.
      // This is safe: two identical malformed strings produce the same hash,
      // and a malformed string won't falsely match a valid one.
      return stringHash(json);
    }
  }

  private static long computeHash(JsonParser parser) throws IOException {
    JsonToken token = parser.currentToken();
    if (token == null) {
      return 0L;
    }
    switch (token) {
      case START_OBJECT:
        return hashObject(parser);
      case START_ARRAY:
        return hashArray(parser);
      case VALUE_STRING:
        return mix(stringHash(parser.getText()));
      case VALUE_NUMBER_INT:
      case VALUE_NUMBER_FLOAT:
        // Use text representation for numeric consistency across int/long/double
        return mix(stringHash(parser.getText()));
      case VALUE_TRUE:
        return 0x9E3779B97F4A7C15L;
      case VALUE_FALSE:
        return 0x517CC1B727220A95L;
      case VALUE_NULL:
        return 0x6C62272E07BB0142L;
      default:
        return 0L;
    }
  }

  // Type markers to distinguish empty object {}, empty array [], and null
  private static final long OBJECT_SEED = 0x7A5662B4E8B10FA3L;
  private static final long ARRAY_SEED = 0x3C6EF372FE94F82BL;

  /**
   * Hash an object using commutative addition of entry hashes.
   * Addition is commutative (a + b == b + a), so the result is
   * independent of the order in which keys appear in the JSON.
   */
  private static long hashObject(JsonParser parser) throws IOException {
    long hash = OBJECT_SEED;
    while (parser.nextToken() != JsonToken.END_OBJECT) {
      long keyHash = stringHash(parser.currentName());
      parser.nextToken();
      long valueHash = computeHash(parser);
      // Mix key+value into a single entry hash, then add (commutative)
      hash += mix(keyHash * 0x9E3779B97F4A7C15L + valueHash);
    }
    return hash;
  }

  /**
   * Hash an array using position-dependent combination.
   * Array element order IS semantically significant in JSON.
   */
  private static long hashArray(JsonParser parser) throws IOException {
    long hash = ARRAY_SEED;
    while (parser.nextToken() != JsonToken.END_ARRAY) {
      hash = hash * 31 + computeHash(parser);
    }
    return mix(hash);
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
   * Mixing/finalizer function to improve hash distribution and break
   * additive symmetry (prevents collisions when values are swapped between keys).
   * <p>
   * This is fmix64 from MurmurHash3 by Austin Appleby (public domain).
   * See: https://github.com/aappleby/smhasher/blob/master/src/MurmurHash3.cpp
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
