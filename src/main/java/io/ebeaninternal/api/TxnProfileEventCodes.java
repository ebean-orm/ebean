package io.ebeaninternal.api;

/**
 * Event codes used in transaction profiling.
 *
 * These appear in verbose transaction profile logs.
 */
public interface TxnProfileEventCodes {

  String EVT_COMMIT = "c";
  String EVT_ROLLBACK = "r";

  String EVT_INSERT = "i";
  String EVT_UPDATE = "u";
  String EVT_DELETE = "d";
  String EVT_DELETE_SOFT = "ds";
  String EVT_DELETE_PERMANENT = "dp";
  String EVT_ORMUPDATE = "uo";
  String FIND_UPDATE = "uq";
  String FIND_DELETE = "dq";

  String EVT_UPDATESQL = "su";
  String EVT_CALLABLESQL = "sc";

  String FIND_ONE = "fo";
  String FIND_MANY = "fm";
  String FIND_ITERATE = "fe";
  String FIND_ID_LIST = "fi";
  String FIND_ATTRIBUTE = "fa";
  String FIND_COUNT = "fc";
  String FIND_SUBQUERY = "fs";

  String FIND_MANY_LAZY = "lm";
  String FIND_ONE_LAZY = "lo";

}
