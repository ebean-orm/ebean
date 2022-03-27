package io.ebeaninternal.server.type;

import io.ebean.config.EncryptKey;
import io.ebeaninternal.server.deploy.BaseTest;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestSimpleEncryptor extends BaseTest {

  @Test
  public void test() {

    SimpleAesEncryptor e = new SimpleAesEncryptor();

    EncryptKey key = new BasicEncryptKey("hello");

    byte[] data = "test123".getBytes(StandardCharsets.UTF_8);

    byte[] ecData = e.encrypt(data, key);

    byte[] deData = e.decrypt(ecData, key);

    assertThat(data).containsExactly(deData);

    Timestamp t = new Timestamp(System.currentTimeMillis());
    byte[] ecTimestamp = e.encryptString(t.toString(), key);

    String tsFormat = e.decryptString(ecTimestamp, key);
    Timestamp t1 = Timestamp.valueOf(tsFormat);
    assertEquals(t, t1);
  }

  static class BasicEncryptKey implements EncryptKey {

    private final String key;

    public BasicEncryptKey(String key) {
      this.key = key;
    }

    @Override
    public String getStringValue() {
      return key;
    }

  }
}
