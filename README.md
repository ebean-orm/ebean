ebean-spring-txn
=====================

Integration with Spring managed transactions.

This project provides an Ebean `ExternalTransactionManager` specifically
to integrate with Springs JDBC Transaction manager.

## To use

```java
DatabaseConfig config = new DatabaseConfig();

// set SpringJdbcTransactionManager ... as the external transaction manager
config.setExternalTransactionManager(new SpringJdbcTransactionManager());

...
Database database = DatabaseFactory.create(config);

```

## Notes

You can use Ebean in Spring/Spring Boot *without* this and that case Ebean
manages the Transactions itself. With Ebean managing the transactions there
are some benefits with more control over JDBC batch, getGeneratedKeys
and a simpler abstraction (as Spring transactions is designed to manage
multiple resources such as JDBC Transactions and JPA EntityManager and Ebean
only needs to manage JDBC Transactions).


## Java modules

Add `requires io.ebean.spring.txn` to module-info.

```java
module foo {

  requires io.ebean.spring.txn;
  ...
}
```
