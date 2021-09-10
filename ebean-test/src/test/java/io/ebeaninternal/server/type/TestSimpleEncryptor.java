package io.ebeaninternal.server.type;

import io.ebean.BaseTestCase;
import io.ebean.config.EncryptKey;
import org.junit.jupiter.api.Test;
import org.tests.basic.encrypt.BasicEncryptKey;

import java.sql.Timestamp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestSimpleEncryptor extends BaseTestCase {

  @Test
  public void test() {

    SimpleAesEncryptor e = new SimpleAesEncryptor();

    EncryptKey key = new BasicEncryptKey("hello");

    byte[] data = "test123".getBytes();

    byte[] ecData = e.encrypt(data, key);

    byte[] deData = e.decrypt(ecData, key);

    assertThat(data).containsExactly(deData);

    Timestamp t = new Timestamp(System.currentTimeMillis());
    byte[] ecTimestamp = e.encryptString(t.toString(), key);

    String tsFormat = e.decryptString(ecTimestamp, key);
    Timestamp t1 = Timestamp.valueOf(tsFormat);
    assertEquals(t, t1);
  }
}
