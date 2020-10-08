package io.ebean.test;

import org.junit.Test;

import static org.junit.Assert.*;

public class UserContextTest {

  @Test
  public void set() {

    UserContext.set("u1", "t1");

    assertEquals("u1", UserContext.currentUserId());
    assertEquals("t1", UserContext.currentTenantId());

    UserContext.set("u2", "t1");

    assertEquals("u2", UserContext.currentUserId());
    assertEquals("t1", UserContext.currentTenantId());

    UserContext.set("u3", "t3");

    assertEquals("u3", UserContext.currentUserId());
    assertEquals("t3", UserContext.currentTenantId());
  }

  @Test
  public void setUserId() {

    UserContext.reset();
    UserContext.setUserId("u1");
    assertEquals("u1", UserContext.currentUserId());
    assertNull(UserContext.currentTenantId());

    UserContext.setUserId("u2");
    assertEquals("u2", UserContext.currentUserId());
    assertNull(UserContext.currentTenantId());
  }


  @Test
  public void setTenantId() {

    UserContext.reset();
    UserContext.setTenantId("t1");
    assertEquals("t1", UserContext.currentTenantId());
    assertNull(UserContext.currentUserId());

    UserContext.setTenantId("t2");
    assertEquals("t2", UserContext.currentTenantId());
    assertNull(UserContext.currentUserId());
  }
}
