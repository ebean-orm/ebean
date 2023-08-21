package org.tests.transaction;

import io.ebean.DB;
import io.ebean.annotation.Transactional;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.PersistenceException;
import javax.persistence.Table;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * It shows the issue that the user record does NOT rolback when there is an exception thrown
 */
public class TransactionNotTerminatedAfterRollback {
  private static final Logger LOG = LoggerFactory.getLogger(TransactionNotTerminatedAfterRollback.class);

  @Test
  public void test() {
    try {
      new UserService().create(new User(1L, "David"));
      fail("Exception should be thrown");
    } catch (PersistenceException pe) {
      LOG.error("e: " + pe);
    }
    List<User> users = DB.find(User.class).findList();
    LOG.debug("users: {}", users);
    assertThat(users).isEmpty();
  }

  @Transactional(rollbackFor = PersistenceException.class)
  public class UserService {
    public void create(User i) {
      DB.save(i);
      DB.save(new User(1L, "Peter")); // make it throw exception and rollback
    }
  }

  @Entity
  @Table(name = "tx_user")
  public static class User {
    @Id
    Long id;
    String name;

    public User() {
    }

    public User(Long id, String name) {
      this.id = id;
      this.name = name;
    }

    @Override
    public String toString() {
      StringBuilder s = new StringBuilder();
      s.append('{');
      s.append("id: ").append(id).append(", ");
      s.append("name: ").append(name);
      s.append('}');
      return s.toString();
    }
  }
}
