package org.example;

import io.ebean.Database;
import io.ebean.Transaction;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The Class UserServiceImpl.
 *
 * @author E Mc Greal
 * @since 18.05.2009
 */
public class UserServiceImpl implements UserService, ApplicationContextAware {

    private ApplicationContext applicationContext;

    /**
     * The ebean server.
     */
    @Autowired
    private Database ebeanServer;

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Throwable.class)
    public void insideTestRollback(long oid) {
      User found = ebeanServer.find(User.class, oid);
      assertThat(found.getName()).isEqualTo("rollback1");
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Throwable.class)
    public void save(User user) {
        ebeanServer.save(user);
    }

    @Transactional(readOnly = true, propagation = Propagation.REQUIRED)
    public User find(long id) {
        return ebeanServer.find(User.class, id);
    }

    public void nonTransactional() {
        ebeanServer.currentTransaction();
    }

    public User findNoCurrentTransaction(long id) {
        return ebeanServer.find(User.class, id);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void batchInsert() {

        List<User> users = new ArrayList<>();
        for (int i = 0; i < 25; i++) {
            User user = new User();
            user.setName("user" + i);
            users.add(user);
        }

        System.out.println("---------before batch-------");

        Transaction tx = ebeanServer.currentTransaction();
        tx.setBatchSize(20);
        ebeanServer.saveAll(users);

        System.out.println("---------after batch-------");

        applicationContext.getBean(UserService.class).requiresNew();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void requiresNew() {
        User user = new User();
        user.setName("user_x0");
        user.insert();
        //ebeanServer.save(user);
    }


    /**
     * Return the ebean server.
     */
    public Database getEbeanServer() {
        return ebeanServer;
    }

    /**
     * Sets the ebean server.
     */
    public void setEbeanServer(Database ebeanServer) {
        this.ebeanServer = ebeanServer;
    }


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
