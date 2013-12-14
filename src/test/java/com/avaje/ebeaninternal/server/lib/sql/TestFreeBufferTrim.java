package com.avaje.ebeaninternal.server.lib.sql;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.avaje.ebean.BaseTestCase;

public class TestFreeBufferTrim extends BaseTestCase {

  @Test
  public void test() {

    FreeConnectionBuffer b = new FreeConnectionBuffer(3);
    Assert.assertEquals(0, b.size());

    PooledConnection p0 = Mockito.mock(PooledConnection.class);
    PooledConnection p1 = Mockito.mock(PooledConnection.class);
    PooledConnection p2 = Mockito.mock(PooledConnection.class);
    b.add(p0);
    b.add(p1);
    b.add(p2);

    Assert.assertEquals(3, b.size());

    // add 1 second as this is going to fast for System.currentTimeMillis() 
    long now = System.currentTimeMillis()+1000;
    int trimCount = b.trim(now);

    Assert.assertEquals(0, b.size());
    Assert.assertEquals(3, trimCount);
  }
  
  @Test
  public void testWithTime() {

    FreeConnectionBuffer b = new FreeConnectionBuffer(3);
    Assert.assertEquals(0, b.size());
    
    PooledConnection p0 = Mockito.mock(PooledConnection.class);
    Mockito.when(p0.getLastUsedTime()).thenReturn(1000l);
    
    PooledConnection p1 = Mockito.mock(PooledConnection.class);
    Mockito.when(p1.getLastUsedTime()).thenReturn(2000l);

    PooledConnection p2 = Mockito.mock(PooledConnection.class);
    Mockito.when(p2.getLastUsedTime()).thenReturn(1100l);
    
    b.add(p0);
    b.add(p1);
    b.add(p2);

    Assert.assertEquals(3, b.size());

    int trimCount = b.trim(1500);

    Assert.assertEquals(1, b.size());
    Assert.assertEquals(2, trimCount);
  }
  
  
  
}
