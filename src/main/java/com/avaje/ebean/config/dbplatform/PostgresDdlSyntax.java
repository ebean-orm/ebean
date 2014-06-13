package com.avaje.ebean.config.dbplatform;

public class PostgresDdlSyntax extends DbDdlSyntax {

  /**
   * Map bigint, integer and smallint into their equivilent serial types. 
   */
  @Override
  public String getIdentityColumnDefn(String columnDefn) {
    
    //smallserial, serial and bigserial
    if ("bigint".equalsIgnoreCase(columnDefn)) {
      return "bigserial";
    }
    if ("integer".equalsIgnoreCase(columnDefn)) {
      return "serial";
    }
    if ("smallint".equalsIgnoreCase(columnDefn)) {
      return "smallserial";
    }
    
    return columnDefn;
  }

}
