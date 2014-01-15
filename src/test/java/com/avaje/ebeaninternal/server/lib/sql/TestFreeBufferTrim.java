package com.avaje.ebeaninternal.server.lib.sql;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.avaje.ebean.BaseTestCase;

public class TestFreeBufferTrim extends BaseTestCase {
  
  @Test
  public void testWithTime() {

    FreeConnectionBuffer b = new FreeConnectionBuffer();
    Assert.assertEquals(0, b.size());
    
    PooledConnection p0 = Mockito.mock(PooledConnection.class);
    Mockito.when(p0.shouldTrim(1500, 0)).thenReturn(true);
    
    PooledConnection p1 = Mockito.mock(PooledConnection.class);
    Mockito.when(p1.shouldTrim(1500, 0)).thenReturn(true);
    
    PooledConnection p2 = Mockito.mock(PooledConnection.class);
    Mockito.when(p2.shouldTrim(1500, 0)).thenReturn(false);
    
    b.add(p0);
    b.add(p1);
    b.add(p2);

    Assert.assertEquals(3, b.size());

    int trimCount = b.trim(1500, 0);

    Assert.assertEquals(1, b.size());
    Assert.assertEquals(2, trimCount);
  }
  
  
  
}
