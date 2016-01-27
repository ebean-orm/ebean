package com.avaje.tests.unitinternal;

import java.sql.Timestamp;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.config.EncryptKey;
import com.avaje.ebeaninternal.server.type.SimpleAesEncryptor;
import com.avaje.tests.basic.encrypt.BasicEncryptKey;

public class TestSimpleEncryptor extends BaseTestCase {

  @Test
  public void test() {

    SimpleAesEncryptor e = new SimpleAesEncryptor();

    EncryptKey key = new BasicEncryptKey("hello");

    byte[] data = "test123".getBytes();

    byte[] ecData = e.encrypt(data, key);

    byte[] deData = e.decrypt(ecData, key);

    Timestamp t = new Timestamp(System.currentTimeMillis());
    byte[] ecTimestamp = e.encryptString(t.toString(), key);

    String tsFormat = e.decryptString(ecTimestamp, key);
    Timestamp t1 = Timestamp.valueOf(tsFormat);
    Assert.assertEquals(t, t1);

  }
}
