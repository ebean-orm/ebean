package com.avaje.ebean.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.avaje.ebean.TxIsolation;
import com.avaje.ebean.TxType;

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
 * <pre class="code">
 * 
 *  // a normal class
 * public class MySimpleUserService {
 * 
 *  // this method is transactional automatically handling 
 *  // transaction begin, commit and rollback etc
 *  &#064;Transactional
 *  public void runInTrans() throws IOException {
 * 
 *    // tasks performed within the transaction
 *    ...
 *    // find some objects
 *    Customer cust = Ebean.find(Customer.class, 1);
 *    
 *    Order order = ...;
 *    ...
 *    // save some objects
 *    Ebean.save(customer);
 *    Ebean.save(order);
 *  }
 * </pre>
 * 
 * <p>
 * During development and testing you can set a debug level which will log the
 * transaction begin, commit and rollback events so that you can easily confirm
 * it is behaving as you would expect.
 * </p>
 * 
 * <pre class="code">
 *  ## in ebean.properties file
 *  
 *  ## Log transaction begins and ends etc
 *  ## (0=NoLogging 1=minimal ... 9=logAll)
 *  ebean.debug.transaction=3
 * 
 * </pre>
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface Transactional {

  /**
   * The type of transaction scoping. Defaults to REQUIRED.
   */
  TxType type() default TxType.REQUIRED;

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
   * The throwable's that will explicitly cause a rollback to occur.
   */
  Class<? extends Throwable>[] rollbackFor() default {};

  /**
   * The throwable's that will explicitly NOT cause a rollback to occur.
   */
  Class<? extends Throwable>[] noRollbackFor() default {};

};
