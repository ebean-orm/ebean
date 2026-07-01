package io.ebeaninternal.server.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JsonContentHashTest {

  @Test
  void sameContent_sameHash() {
    String json = "{\"name\":\"Alice\",\"age\":30}";
    assertThat(JsonContentHash.hash(json)).isEqualTo(JsonContentHash.hash(json));
  }

  @Test
  void reorderedKeys_sameHash() {
    // The core scenario: PostgreSQL JSONB reorders keys
    String jackson = "{\"status\":\"ACTIVE\",\"type\":\"ADMIN\"}";
    String postgres = "{\"type\":\"ADMIN\",\"status\":\"ACTIVE\"}";
    assertThat(JsonContentHash.hash(jackson)).isEqualTo(JsonContentHash.hash(postgres));
  }

  @Test
  void reorderedKeys_multipleFields() {
    String a = "{\"zebra\":1,\"apple\":2,\"mango\":3}";
    String b = "{\"apple\":2,\"mango\":3,\"zebra\":1}";
    String c = "{\"mango\":3,\"zebra\":1,\"apple\":2}";
    long hashA = JsonContentHash.hash(a);
    long hashB = JsonContentHash.hash(b);
    long hashC = JsonContentHash.hash(c);
    assertThat(hashA).isEqualTo(hashB);
    assertThat(hashA).isEqualTo(hashC);
  }

  @Test
  void differentValues_differentHash() {
    String a = "{\"status\":\"ACTIVE\",\"type\":\"ADMIN\"}";
    String b = "{\"status\":\"INACTIVE\",\"type\":\"ADMIN\"}";
    assertThat(JsonContentHash.hash(a)).isNotEqualTo(JsonContentHash.hash(b));
  }

  @Test
  void differentKeys_differentHash() {
    String a = "{\"name\":\"Alice\"}";
    String b = "{\"nome\":\"Alice\"}";
    assertThat(JsonContentHash.hash(a)).isNotEqualTo(JsonContentHash.hash(b));
  }

  @Test
  void nestedObjects_reorderedKeys() {
    String a = "{\"user\":{\"first\":\"Alice\",\"last\":\"Smith\"},\"active\":true}";
    String b = "{\"active\":true,\"user\":{\"last\":\"Smith\",\"first\":\"Alice\"}}";
    assertThat(JsonContentHash.hash(a)).isEqualTo(JsonContentHash.hash(b));
  }

  @Test
  void nestedObjects_differentValues() {
    String a = "{\"user\":{\"first\":\"Alice\",\"last\":\"Smith\"}}";
    String b = "{\"user\":{\"first\":\"Bob\",\"last\":\"Smith\"}}";
    assertThat(JsonContentHash.hash(a)).isNotEqualTo(JsonContentHash.hash(b));
  }

  @Test
  void arrayOrder_matters() {
    // Array element order IS semantically significant
    String a = "[1,2,3]";
    String b = "[3,2,1]";
    assertThat(JsonContentHash.hash(a)).isNotEqualTo(JsonContentHash.hash(b));
  }

  @Test
  void arrayOrder_sameOrder_sameHash() {
    String a = "[1,2,3]";
    String b = "[1,2,3]";
    assertThat(JsonContentHash.hash(a)).isEqualTo(JsonContentHash.hash(b));
  }

  @Test
  void enumValues_reorderedKeys() {
    // The exact scenario from issue #3129: POJO with multiple enum fields
    String jackson = "{\"status\":\"ACTIVE\",\"role\":\"ADMIN\",\"priority\":\"HIGH\"}";
    String postgres = "{\"role\":\"ADMIN\",\"priority\":\"HIGH\",\"status\":\"ACTIVE\"}";
    assertThat(JsonContentHash.hash(jackson)).isEqualTo(JsonContentHash.hash(postgres));
  }

  @Test
  void swappedValues_differentHash() {
    // Swapping values between keys must produce different hashes
    String a = "{\"a\":1,\"b\":2}";
    String b = "{\"a\":2,\"b\":1}";
    assertThat(JsonContentHash.hash(a)).isNotEqualTo(JsonContentHash.hash(b));
  }

  @Test
  void emptyObject() {
    assertThat(JsonContentHash.hash("{}")).isNotEqualTo(0L);
  }

  @Test
  void emptyArray() {
    assertThat(JsonContentHash.hash("[]")).isNotEqualTo(0L);
  }

  @Test
  void emptyObject_vs_emptyArray() {
    assertThat(JsonContentHash.hash("{}")).isNotEqualTo(JsonContentHash.hash("[]"));
  }

  @Test
  void nullInput() {
    assertThat(JsonContentHash.hash(null)).isEqualTo(0L);
  }

  @Test
  void emptyString() {
    assertThat(JsonContentHash.hash("")).isEqualTo(0L);
  }

  @Test
  void booleanValues() {
    String a = "{\"flag\":true}";
    String b = "{\"flag\":false}";
    assertThat(JsonContentHash.hash(a)).isNotEqualTo(JsonContentHash.hash(b));
  }

  @Test
  void nullValues() {
    String a = "{\"value\":null}";
    String b = "{\"value\":\"text\"}";
    assertThat(JsonContentHash.hash(a)).isNotEqualTo(JsonContentHash.hash(b));
  }

  @Test
  void numericTypes() {
    String a = "{\"count\":42}";
    String b = "{\"count\":43}";
    assertThat(JsonContentHash.hash(a)).isNotEqualTo(JsonContentHash.hash(b));
  }

  @Test
  void whitespaceVariations() {
    // Whitespace in JSON structure (not in values) should not matter
    String compact = "{\"a\":1,\"b\":2}";
    String spaced = "{ \"a\" : 1 , \"b\" : 2 }";
    assertThat(JsonContentHash.hash(compact)).isEqualTo(JsonContentHash.hash(spaced));
  }

  @Test
  void complexNestedStructure() {
    String a = "{\"users\":[{\"name\":\"Alice\"},{\"name\":\"Bob\"}],\"count\":2,\"active\":true}";
    String b = "{\"active\":true,\"count\":2,\"users\":[{\"name\":\"Alice\"},{\"name\":\"Bob\"}]}";
    assertThat(JsonContentHash.hash(a)).isEqualTo(JsonContentHash.hash(b));
  }

  @Test
  void postgresJsonbKeyReordering_realistic() {
    // Simulates PostgreSQL JSONB storage which reorders by key length, then alphabetically
    String javaOrder = "{\"status\":\"ACTIVE\",\"type\":\"STANDARD\",\"createdAt\":\"2024-01-01\",\"id\":123}";
    String pgOrder = "{\"id\":123,\"type\":\"STANDARD\",\"status\":\"ACTIVE\",\"createdAt\":\"2024-01-01\"}";
    assertThat(JsonContentHash.hash(javaOrder)).isEqualTo(JsonContentHash.hash(pgOrder));
  }
}
