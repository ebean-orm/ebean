package org.tests.unitinternal;

import io.ebean.BaseTestCase;
import io.ebean.config.EncryptKey;
import io.ebeaninternal.server.type.SimpleAesEncryptor;
import org.tests.basic.encrypt.BasicEncryptKey;
import org.junit.Assert;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Timestamp;

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
    Assert.assertEquals(t, t1);

  }
}
