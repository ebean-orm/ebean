package com.avaje.ebean.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.avaje.ebean.TxIsolation;
import com.avaje.ebean.TxType;
import com.avaje.ebean.config.PersistBatch;

/**
 * Specify transaction scoping for a method.
 * <p>
 * <b><i> This is only supported if "Enhancement" is used via javaagent, ANT
 * task or IDE enhancement plugin etc. </i></b>
 * </p>
 * <p>
 * Note: Currently there are 3 known annotations that perform this role.
 * <ul>
 * <li>EJB's javax.ejb.TransactionAttribute</li>
 * <li>Spring's org.springframework.transaction.annotation.Transactional</li>
 * <li>and this one, Ebean's own com.avaje.ebean.annotation.Transactional</li>
 * </ul>
 * Spring created their one because the EJB annotation does not support features
 * such as isolation level and specifying rollbackOn, noRollbackOn exceptions.
 * This one exists for Ebean because I agree that the standard one is
 * insufficient and don't want to include a dependency on Spring.
 * </p>
 * <p>
 * The default behaviour of EJB (and hence Spring) is to NOT ROLLBACK on checked
 * exceptions. I find this very counter-intuitive. Ebean will provide a property
 * to set the default behaviour to rollback on any exception and optionally
 * change the setting to be consistent with EJB/Spring if people wish to do so.
 * </p>
 * 
 * <pre>{@code
 *
 *  // a normal class
 *  public class MySimpleUserService {
 * 
 *    // this method is transactional automatically handling
 *    // transaction begin, commit and rollback etc
 *    @Transactional
 *    public void runInTrans() throws IOException {
 * 
 *      // tasks performed within the transaction
 *      ...
 *      // find some objects
 *      Customer cust = ebeanServer.find(Customer.class, 42);
 *    
 *      Order order = ...;
 *      ...
 *      // save some objects
 *      ebeanServer.save(customer);
 *      ebeanServer.save(order);
 *    }
 *
 * }</pre>
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface Transactional {

  /**
   * The type of transaction scoping. Defaults to REQUIRED.
   */
  TxType type() default TxType.REQUIRED;

  /**
   * Persist batch mode for the transaction.
   */
  PersistBatch batch() default PersistBatch.INHERIT;

  /**
   * Persist batch mode for the request if not set on the transaction.
   * <p>
   * If batch is set to NONE then batchOnCascade can be set to INSERT or ALL
   * and then each save(), delete(), insert(), update() request that cascades
   * to child beans can use JDBC batch.
   * </p>
   */
  PersistBatch batchOnCascade() default PersistBatch.INHERIT;

  /**
   * The batch size to use when using JDBC batch mode.
   * <p>
   * If unset this defaults to the value set in ServerConfig.
   * </p>
   */
  int batchSize() default 0;

  /**
   * The transaction isolation level this transaction should have.
   * <p>
   * This will only be used if this scope creates the transaction. If the
   * transaction has already started then this will currently be ignored (you
   * could argue that it should throw an exception).
   * </p>
   */
  TxIsolation isolation() default TxIsolation.DEFAULT;

  /**
   * Set this to true if the transaction should be only contain queries.
   */
  boolean readOnly() default false;

  /**
   * The name of the server that you want the transaction to be created from.
   * <p>
   * If left blank the 'default' server is used.
   * </p>
   */
  String serverName() default "";

  // int timeout() default 0;

  /**
   * The Throwable's that will explicitly cause a rollback to occur.
   */
  Class<? extends Throwable>[] rollbackFor() default {};

  /**
   * The Throwable's that will explicitly NOT cause a rollback to occur.
   */
  Class<? extends Throwable>[] noRollbackFor() default {};

}
