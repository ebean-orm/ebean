package io.ebeaninternal.server.type;


import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JsonTrimTest {

  @Test
  void trim_array() {
    final String trimmed = JsonTrim.trim("[{\"name\": \"one\", \"along\": 1, \"timestamp\": 1629609021559}, {\"name\": \"two\", \"along\": 2, \"timestamp\": 1629609021559}]");
    assertThat(trimmed).isEqualTo("[{\"name\":\"one\",\"along\":1,\"timestamp\":1629609021559},{\"name\":\"two\",\"along\":2,\"timestamp\":1629609021559}]");
  }

  @Test
  void trim_object() {
    final String trimmed = JsonTrim.trim("{\"name\":  \"one\",\t \t \"along\": 1, \"timestamp\": 1629609021559}");
    assertThat(trimmed).isEqualTo("{\"name\":\"one\",\"along\":1,\"timestamp\":1629609021559}");
  }

  @Test
  void trim_object_embeddedEscapeSlash() {
    final String trimmed = JsonTrim.trim("{\"a\":  \"o\\nf\",\t \t \n \"b\": 1}");
    assertThat(trimmed).isEqualTo("{\"a\":\"o\\nf\",\"b\":1}");
  }

  @Test
  void trim_escapedTabNewLine() {
    final String trimmed = JsonTrim.trim("{\"a\":  \"o\\t\\nf\",\t \t \n \"b\": 1}");
    assertThat(trimmed).isEqualTo("{\"a\":\"o\\t\\nf\",\"b\":1}");
  }

  @Test
  void trim_leadingEscapedNewLine() {
    final String trimmed = JsonTrim.trim("{\"a\":  \"\\no\\t\\nf\\n\",\t \t \n \"b\": 1}");
    assertThat(trimmed).isEqualTo("{\"a\":\"\\no\\t\\nf\\n\",\"b\":1}");
  }

  @Test
  void trim_trailingEscapedNewLine() {
    final String trimmed = JsonTrim.trim("{\"a\":  \"o\\t\\nf\\n\",\t \t \n \"b\": 1}");
    assertThat(trimmed).isEqualTo("{\"a\":\"o\\t\\nf\\n\",\"b\":1}");
  }

  @Test
  void trim_trailingEscapedSlash() {
    final String trimmed = JsonTrim.trim("{\"a\":  \"o\\t\\nf\\\\\",\t \t \n \"b\": 1}");
    assertThat(trimmed).isEqualTo("{\"a\":\"o\\t\\nf\\\\\",\"b\":1}");
  }

  @Test
  void trim_object_embeddedEscaped() {
    final String trimmed = JsonTrim.trim("{\"name\":  \"one\nfoo\nbar\\bazz\tboo\",\t \t \n \"along\": 1}");
    assertThat(trimmed).isEqualTo("{\"name\":\"one\nfoo\nbar\\bazz\tboo\",\"along\":1}");
  }

  @Test
  void trim_escaped() {
    final String trimmed = JsonTrim.trim(" \t \n {\"a\": \n \"\\t1\\t2\\n3\\\\\",\t \n \"b\": \"\\t1\\t2\\n3\\\\\" , \t \n \"c\": \"\\t1\\t2\\n3\\\\\" \t \n }");
    assertThat(trimmed).isEqualTo("{\"a\":\"\\t1\\t2\\n3\\\\\",\"b\":\"\\t1\\t2\\n3\\\\\",\"c\":\"\\t1\\t2\\n3\\\\\"}");
  }
}
