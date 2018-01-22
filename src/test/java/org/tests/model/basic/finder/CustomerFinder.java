package org.tests.model.basic.finder;

import io.ebean.Finder;
import org.tests.model.basic.Customer;

import java.util.List;

/**
 * Finder methods for Customer.
 */
public class CustomerFinder extends Finder<Integer, Customer> {

  public CustomerFinder() {
    super(Customer.class);
  }

  /**
   * Bulk update customers matching the name to inactive status.
   *
   * @return  the number of customers updated
   */
  public int updateToInactive(String name) {
    return update()
      .set("status", Customer.Status.INACTIVE)
      .setRaw("version = version + 1")
      .where().eq("name", name)
      .update();
  }

  /**
   * Find customer by unique name.
   */
  public Customer byName(String name) {
    return query().where()
      .eq("name", name)
      .findOne();
  }

  public List<Customer> byNameStatus(String nameStartsWith, Customer.Status status) {

    return query("where status = :status and name istartsWith :name order by name")
      .setParameter("status", status)
      .setParameter("name", nameStartsWith)
      .findList();
  }

  public List<String> namesStartingWith(String name) {

    return nativeSql("select name from o_customer where name like ? order by name")
      .setParameter(1, name+"%")
      .findSingleAttributeList();
  }

  /**
   * Bulk update names (not a good example here).
   */
  public int updateNames(String newName, long minId) {
    return update()
      .set("name", newName)
      .setRaw("version = version + 1")
      .where().gt("id", minId)
      .update();
  }

  public int totalCount() {
    return query().findCount();
  }
}
