package org.tests.compositekeys;

import io.ebean.CountDistinctOrder;
import io.ebean.DB;
import io.ebean.Query;
import io.ebean.annotation.Identity;
import io.ebean.xtest.BaseTestCase;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.deploy.BeanPropertyAssocMany;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static io.ebean.annotation.IdentityGenerated.BY_DEFAULT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Read more at https://groups.google.com/forum/#!topic/ebean/VD1MV2-LrOc
 */
public class TestOnCascadeDeleteChildrenWithCompositeKeys extends BaseTestCase {


  @BeforeEach
  public void before() {

    // remove all the User records first
    DB.deleteAll(DB.find(User.class).findList());

    // insert 2 User records
    DB.save(new User(1L, "one"));
    DB.save(new User(2L, "two"));
  }

  /**
   * work fine when delete User one by one
   */
  @Test
  public void testDeleteById() {

    assertEquals(2, DB.find(User.class).findList().size());
    DB.delete(User.class, 1L);
    DB.delete(User.class, 2L);
    assertEquals(0, DB.find(User.class).findList().size());
  }

  /**
   * Wrong SQL is generated when deleting user by list of ID
   * SQL generated: delete from user_role where (user_id) in ((?,?),(?,?))
   */
  @Test
  public void testDeleteByIdList() {

    assertEquals(2, DB.find(User.class).findList().size());
    List<Long> ids = new ArrayList<>();
    ids.add(1L);
    ids.add(2L);

    DB.deleteAll(User.class, ids); // PersistenceException would be thrown here
    assertEquals(0, DB.find(User.class).findList().size());
  }

  @Test
  public void testFindByParentIdList() {

    assertEquals(2, DB.find(User.class).findList().size());

    SpiEbeanServer spiServer = (SpiEbeanServer) DB.getDefault();

    BeanDescriptor<TestOnCascadeDeleteChildrenWithCompositeKeys.User> beanDescriptor =
      spiServer.descriptor(TestOnCascadeDeleteChildrenWithCompositeKeys.User.class);

    BeanPropertyAssocMany<?> beanProperty = (BeanPropertyAssocMany<?>) beanDescriptor.beanProperty("userRoles");

    List<Object> ids = new ArrayList<>();
    ids.add(1L);
    ids.add(2L);

    beanProperty.findIdsByParentIdList(ids, null, true);
    beanProperty.findIdsByParentId(1L, null, true);
  }

  /**
   * Test makes select and a select distinct of entities with composite keys.
   */
  @Test
  public void testSelectDistinctCountWithCompositeKey() {

    // first query with a simple findList
    Query<UserRole> query1 = DB.find(UserRole.class);
    query1.findList();

    assertThat(query1.getGeneratedSql()).contains("select t0.user_id, t0.role_id, t0.user_id, t0.role_id from em_user_role t0");

    // second query with count distinct
    query1 = DB.find(UserRole.class);
    query1.select("");
    query1.fetch("user", "name");
    query1.setDistinct(true).setCountDistinct(CountDistinctOrder.COUNT_DESC_ATTR_ASC).setMaxRows(20);
    query1.findSingleAttributeList();

    if (isH2() || isMariaDB() || isPostgresCompatible()) {
      assertThat(query1.getGeneratedSql()).contains("select distinct r1.attribute_, count(*) from "
        + "(select distinct t0.user_id, t0.role_id, t1.name as attribute_ "
        + "from em_user_role t0 join em_user t1 on t1.id = t0.user_id) r1 "
        + "group by r1.attribute_ order by count(*) desc, r1.attribute_ limit 20");
    } else if (isDb2()) {
      assertThat(query1.getGeneratedSql()).contains("select distinct r1.attribute_, count(*) from "
        + "(select distinct t0.user_id, t0.role_id, t1.name as attribute_ "
        + "from em_user_role t0 join em_user t1 on t1.id = t0.user_id) r1 "
        + "group by r1.attribute_ order by count(*) desc, r1.attribute_ fetch next 20 rows only");
    } else if (isSqlServer()) {
      assertThat(query1.getGeneratedSql()).contains("select distinct top 20 r1.attribute_, count(*) "
        + "from (select distinct t0.user_id, t0.role_id, t1.name as attribute_ from em_user_role t0 "
        + "join em_user t1 on t1.id = t0.user_id) r1 group by r1.attribute_ order by count(*) desc, r1.attribute_");
    } else {
      // no Oracle test yet
    }
  }

  @Identity(generated = BY_DEFAULT)
  @Entity
  @Table(name = "em_user")
  public static class User {
    @Id
    private Long id;
    private String name;
    @OneToMany(cascade = CascadeType.REMOVE)
    private Set<UserRole> userRoles;

    public User() {
    }

    public User(Long id, String name) {
      this.id = id;
      this.name = name;
    }

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

    @EmbeddedId
    private UserRolePK pk;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false, insertable = false, updatable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "role_id", nullable = false, insertable = false, updatable = false)
    private Role role;

    public UserRolePK getPk() {
      return pk;
    }

    public void setPk(UserRolePK pk) {
      this.pk = pk;
    }

    public User getUser() {
      return user;
    }

    public void setUser(User user) {
      this.user = user;
    }

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
    @Id
    private Long id;
    private String name;
    @OneToMany(cascade = CascadeType.REMOVE)
    private Set<UserRole> userRoles;

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

    public Set<UserRole> getUserRoles() {
      return userRoles;
    }

    public void setUserRoles(Set<UserRole> userRoles) {
      this.userRoles = userRoles;
    }
  }
}
