package io.ebean.test;

/**
 * Use in test code when the CurrentUserProvider and/or CurrentTenantProvider were configured by
 * this ebean-test-config plugin.
 * <p>
 * That is, the ebean-test-config plugin will check if there ia a CurrentUserProvider and if not
 * automatically set one and that provider reads the 'current user' from this UserContext.
 * <pre>{@code
 *
 *   // set the current userId which will be put
 *   // into 'WhoCreated' and 'WhoModified' properties
 *
 * 	 UserContext.setUserId("U1");
 *
 * 	 // persist bean that has ... a 'WhoModified' property
 *   Content content = new Content();
 *   content.setName("hello");
 *
 *   content.save();
 *
 * }</pre>
 */
public class UserContext {

  private static final UserContextThreadLocal local = new UserContextThreadLocal();

  private Object userId;
  private Object tenantId;

  private UserContext() {
  }

  /**
   * Return the current user.
   */
  public static Object currentUserId() {
    return local.get().userId;
  }

  /**
   * Return the current tenantId.
   */
  public static Object currentTenantId() {
    return local.get().tenantId;
  }

  /**
   * Set the current userId - this value is put into 'WhoCreated' and 'WhoModified' properties.
   */
  public static void setUserId(Object userId) {
    local.get().userId = userId;
  }

  /**
   * Set the current tenantId.
   */
  public static void setTenantId(Object tenantId) {
    local.get().tenantId = tenantId;
  }

  /**
   * Clear both the current userId and tenantId.
   */
  public static void reset() {
    local.remove();
  }

  /**
   * Set both the current userId and current tenantId.
   */
  public static void set(Object userId, String tenantId) {
    UserContext userContext = local.get();
    userContext.userId = userId;
    userContext.tenantId = tenantId;
  }

  private static class UserContextThreadLocal extends ThreadLocal<UserContext> {

    @Override
    protected UserContext initialValue() {
      return new UserContext();
    }
  }
}
