package org.example;

import io.ebean.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import io.ebean.EbeanServer;

import java.util.ArrayList;
import java.util.List;

/**
 * The Class UserServiceImpl.
 *
 * @since 18.05.2009
 * @author E Mc Greal
 */
public class UserServiceImpl implements UserService {

	/** The ebean server. */
	@Autowired
	private EbeanServer ebeanServer;

	@Transactional(propagation = Propagation.REQUIRED, rollbackFor=Throwable.class)
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
    for(int i=0 ;i<25;i++){
      User user = new User();
      user.setName("user"+i);
      users.add(user);
    }

    System.out.println("---------before batch-------");

    Transaction tx = ebeanServer.currentTransaction();
    tx.setBatchSize(20);
    ebeanServer.saveAll(users);

    System.out.println("---------after batch-------");
  }


	/**
	 * Return the ebean server.
	 */
	public EbeanServer getEbeanServer() {
		return ebeanServer;
	}

	/**
	 * Sets the ebean server.
	 */
	public void setEbeanServer(EbeanServer ebeanServer) {
		this.ebeanServer = ebeanServer;
	}


}
