package org.example;

/**
 * The Interface UserService.
 */
public interface UserService {

	void save(User user);

	User find(long id);

	User findNoCurrentTransaction(long id);

	void nonTransactional();

	void batchInsert();

	void requiresNew();

  void insideTestRollback(long oid);
}
