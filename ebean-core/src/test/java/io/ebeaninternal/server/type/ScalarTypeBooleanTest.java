package io.ebeaninternal.server.type;

import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.sql.Types;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class ScalarTypeBooleanTest {

  @Test
  public void json_true() throws IOException {
    String json = new JsonTester<>(new ScalarTypeBoolean.Native()).test(true);
    assertEquals(json, "{\"key\":true}");
  }

  @Test
  public void json_false() throws IOException {
    String json = new JsonTester<>(new ScalarTypeBoolean.Native()).test(false);
    assertEquals(json, "{\"key\":false}");
  }


  @Test
  public void IntBoolean_isLogicalBoolean() {

    ScalarTypeBoolean.IntBoolean intBoolean = new ScalarTypeBoolean.IntBoolean(1, 0);
    assertThat(intBoolean).isInstanceOf(ScalarTypeLogicalType.class);

    assertThat(intBoolean.getLogicalType()).isEqualTo(Types.BOOLEAN);
  }
}
