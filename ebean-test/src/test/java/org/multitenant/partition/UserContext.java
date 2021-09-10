package org.multitenant.partition;

public final class UserContext {

  private static final UserContextThreadLocal local = new UserContextThreadLocal();

  private String userId;
  private String tenantId;

  private UserContext(String userId, String tenantId) {
    this.userId = userId;
    this.tenantId = tenantId;
  }

  private UserContext() {
  }

  public String getUserId() {
    return userId;
  }

  public String getTenantId() {
    return tenantId;
  }

  public static UserContext get() {
    return local.get();
  }

  public static void reset() {
    local.remove();
  }

  public static void set(String userId, String tenantId) {
    local.set(new UserContext(userId, tenantId));
  }


  private static class UserContextThreadLocal extends ThreadLocal<UserContext> {

    @Override
    protected UserContext initialValue() {
      return new UserContext();
    }
  }
}
