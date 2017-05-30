package org.tests.compositekeys;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.deploy.BeanPropertyAssocMany;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.CascadeType;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;

/**
 * Read more at https://groups.google.com/forum/#!topic/ebean/VD1MV2-LrOc
 */
public class TestOnCascadeDeleteChildrenWithCompositeKeys extends BaseTestCase {


  @Before
  public void before() {

    // remove all the User records first
    Ebean.deleteAll(Ebean.find(User.class).findList());

    // insert 2 User records
    Ebean.save(new User(1L));
    Ebean.save(new User(2L));
  }

  /**
   * work fine when delete User one by one
   */
  @Test
  public void testDeleteById() {

    assertEquals(2, Ebean.find(User.class).findList().size());
    Ebean.delete(User.class, 1L);
    Ebean.delete(User.class, 2L);
    assertEquals(0, Ebean.find(User.class).findList().size());
  }

  /**
   * Wrong SQL is generated when deleting user by list of ID
   * SQL generated: delete from user_role where (user_id) in ((?,?),(?,?))
   */
  @Test
  public void testDeleteByIdList() {

    assertEquals(2, Ebean.find(User.class).findList().size());
    List<Long> ids = new ArrayList<>();
    ids.add(1L);
    ids.add(2L);

    Ebean.deleteAll(User.class, ids); // PersistenceException would be thrown here
    assertEquals(0, Ebean.find(User.class).findList().size());
  }

  @Test
  public void testFindByParentIdList() {

    assertEquals(2, Ebean.find(User.class).findList().size());

    SpiEbeanServer spiServer = (SpiEbeanServer) Ebean.getServer(null);

    BeanDescriptor<TestOnCascadeDeleteChildrenWithCompositeKeys.User> beanDescriptor =
      spiServer.getBeanDescriptor(TestOnCascadeDeleteChildrenWithCompositeKeys.User.class);

    BeanPropertyAssocMany<?> beanProperty = (BeanPropertyAssocMany<?>) beanDescriptor.getBeanProperty("userRoles");

    List<Object> ids = new ArrayList<>();
    ids.add(1L);
    ids.add(2L);

    beanProperty.findIdsByParentId(null, ids, null, null);
    beanProperty.findIdsByParentId(1L, null, null, null);
  }

  @Entity
  @Table(name = "em_user")
  public static class User {
    private Long id;
    private String name;
    private Set<UserRole> userRoles;

    public User() {
    }

    public User(Long id) {
      this.id = id;
    }

    @Id
    public Long getId() {
      return id;
    }

    public void setId(Long id) {
      this.id = id;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    @OneToMany(cascade = CascadeType.REMOVE)
    public Set<UserRole> getUserRoles() {
      return userRoles;
    }

    public void setUserRoles(Set<UserRole> userRoles) {
      this.userRoles = userRoles;
    }
  }

  @Entity
  @Table(name = "em_user_role")
  public static class UserRole implements Serializable {
    private static final long serialVersionUID = 1L;
    private UserRolePK pk;
    private User user;
    private Role role;

    @EmbeddedId
    public UserRolePK getPk() {
      return pk;
    }

    public void setPk(UserRolePK pk) {
      this.pk = pk;
    }

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false, insertable = false, updatable = false)
    public User getUser() {
      return user;
    }

    public void setUser(User user) {
      this.user = user;
    }

    @ManyToOne
    @JoinColumn(name = "role_id", nullable = false, insertable = false, updatable = false)
    public Role getRole() {
      return role;
    }

    public void setRole(Role role) {
      this.role = role;
    }

    @Embeddable
    public static class UserRolePK implements Serializable {
      private static final long serialVersionUID = 1L;
      private Long userId;
      private Long roleId;

      @Override
      public int hashCode() {
        return userId == null || roleId == null ? 0 : (int) (userId + roleId);
      }

      @Override
      public boolean equals(Object other) {
        if (userId == null || roleId == null) return false;
        if (other instanceof UserRolePK) {
          UserRolePK otherPk = (UserRolePK) other;
          return userId.equals(otherPk.userId) && roleId.equals(otherPk.roleId);
        }
        return false;
      }

      public Long getUserId() {
        return userId;
      }

      public void setUserId(Long userId) {
        this.userId = userId;
      }

      public Long getRoleId() {
        return roleId;
      }

      public void setRoleId(Long roleId) {
        this.roleId = roleId;
      }
    }
  }

  @Entity
  @Table(name = "em_role")
  public static class Role {
    private Long id;
    private String name;
    private Set<UserRole> userRoles;

    @Id
    public Long getId() {
      return id;
    }

    public void setId(Long id) {
      this.id = id;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    @OneToMany(cascade = CascadeType.REMOVE)
    public Set<UserRole> getUserRoles() {
      return userRoles;
    }

    public void setUserRoles(Set<UserRole> userRoles) {
      this.userRoles = userRoles;
    }
  }
}
