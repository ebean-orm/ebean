package io.ebeaninternal.api;

/**
 * Event codes used in transaction profiling.
 */
public interface TxnProfileEventCodes {

  byte EVT_END = 0;
  byte EVT_COMMIT = 1;
  byte EVT_ROLLBACK = 2;

  byte EVT_INSERT = 10;
  byte EVT_UPDATE = 11;
  byte EVT_DELETE = 12;
  byte EVT_SOFT_DELETE = 13;
  byte EVT_DELETE_PERMANENT = 14;
  byte EVT_ORMUPDATE = 15;
  byte FIND_UPDATE = 16;
  byte FIND_DELETE = 17;

  byte EVT_UPDATESQL = 20;
  byte EVT_CALLABLESQL = 21;

  byte FIND_ONE = 30;
  byte FIND_MANY = 31;
  byte FIND_ITERATE = 34;
  byte FIND_ID_LIST = 35;
  byte FIND_ATTRIBUTE  = 36;
  byte FIND_COUNT = 37;
  byte FIND_SUBQUERY = 38;

  byte FIND_ONE_LAZY = 40;
  byte FIND_MANY_LAZY = 41;

}
