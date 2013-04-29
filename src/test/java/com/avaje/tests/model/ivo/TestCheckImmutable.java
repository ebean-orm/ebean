package com.avaje.tests.model.ivo;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Currency;

import org.junit.Assert;
import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebeaninternal.server.type.reflect.CheckImmutable;
import com.avaje.ebeaninternal.server.type.reflect.CheckImmutableResponse;
import com.avaje.ebeaninternal.server.type.reflect.ImmutableMeta;
import com.avaje.ebeaninternal.server.type.reflect.ImmutableMetaFactory;

public class TestCheckImmutable extends BaseTestCase {

  @Test
  public void test() {

    SimpleKnownImmutable known = new SimpleKnownImmutable();

    CheckImmutable check = new CheckImmutable(known);

    CheckImmutableResponse response = check.checkImmutable(Money.class);

    Assert.assertTrue(response.isImmutable());
    Assert.assertTrue(!response.isCompoundType());
  }

  public void testMeta() {

    ImmutableMetaFactory factory = new ImmutableMetaFactory();

    ImmutableMeta meta = factory.createImmutableMeta(Money.class);

    Assert.assertNotNull(meta);
    Constructor<?> c = meta.getConstructor();
    Class<?>[] parameterTypes = c.getParameterTypes();

    Method[] readers = meta.getReaders();
    Assert.assertTrue(parameterTypes.length == 1);
    Assert.assertTrue(readers.length == 1);

    Assert.assertEquals("getAmount", readers[0].getName());
    Assert.assertEquals(BigDecimal.class, parameterTypes[0]);

    meta = factory.createImmutableMeta(CMoney.class);

    Assert.assertNotNull(meta);
    c = meta.getConstructor();
    parameterTypes = c.getParameterTypes();
    readers = meta.getReaders();

    Assert.assertTrue(parameterTypes.length == 2);
    Assert.assertTrue(readers.length == 2);

    Assert.assertEquals(Money.class, parameterTypes[0]);
    Assert.assertEquals("getAmount", readers[0].getName());
    Assert.assertEquals(Currency.class, parameterTypes[1]);
    Assert.assertEquals("getCurrency", readers[1].getName());

  }

}
