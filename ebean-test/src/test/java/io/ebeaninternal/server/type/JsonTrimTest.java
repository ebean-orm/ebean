package io.ebeaninternal.server.type;


import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class JsonTrimTest {

  @Test
  public void trim_array() {
    final String trimmed = JsonTrim.trim("[{\"name\": \"one\", \"along\": 1, \"timestamp\": 1629609021559}, {\"name\": \"two\", \"along\": 2, \"timestamp\": 1629609021559}]");
    assertThat(trimmed).isEqualTo("[{\"name\":\"one\",\"along\":1,\"timestamp\":1629609021559},{\"name\":\"two\",\"along\":2,\"timestamp\":1629609021559}]");
  }

  @Test
  public void trim_object() {
    final String trimmed = JsonTrim.trim("{\"name\":  \"one\",\t \t \"along\": 1, \"timestamp\": 1629609021559}");
    assertThat(trimmed).isEqualTo("{\"name\":\"one\",\"along\":1,\"timestamp\":1629609021559}");
  }
}
