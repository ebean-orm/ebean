package io.ebeaninternal.server.core;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.PersistenceException;
import javax.sql.DataSource;

/**
 * Helper to lookup a DataSource from JNDI.
 */
class JndiDataSourceLookup {

  /**
   * Return the DataSource by JNDI lookup.
   * <p>
   * If name is null the 'default' dataSource is returned.
   * </p>
   */
  public DataSource lookup(String jndiName) {
    try {
      Context ctx = new InitialContext();
      DataSource ds = (DataSource) ctx.lookup(jndiName);
      if (ds == null) {
        throw new PersistenceException("JNDI DataSource [" + jndiName + "] not found?");
      }
      return ds;

    } catch (NamingException ex) {
      throw new PersistenceException(ex);
    }
  }
}
