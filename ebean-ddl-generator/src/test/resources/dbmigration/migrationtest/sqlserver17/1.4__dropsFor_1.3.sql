-- Migrationscripts for ebean unittest
-- apply changes
EXEC usp_ebean_drop_column migtest_ckey_detail, one_key;

EXEC usp_ebean_drop_column migtest_ckey_detail, two_key;

EXEC usp_ebean_drop_column migtest_ckey_parent, assoc_id;

EXEC usp_ebean_drop_column migtest_e_basic, new_string_field;

EXEC usp_ebean_drop_column migtest_e_basic, new_boolean_field;

EXEC usp_ebean_drop_column migtest_e_basic, new_boolean_field2;

EXEC usp_ebean_drop_column migtest_e_basic, progress;

EXEC usp_ebean_drop_column migtest_e_basic, new_integer;

EXEC usp_ebean_drop_column migtest_e_history2, test_string2;

EXEC usp_ebean_drop_column migtest_e_history2, test_string3;

EXEC usp_ebean_drop_column migtest_e_history2, new_column;

EXEC usp_ebean_drop_column migtest_e_history5, test_boolean;

EXEC usp_ebean_drop_column migtest_e_softdelete, deleted;

EXEC usp_ebean_drop_column migtest_oto_child, master_id;

IF OBJECT_ID('migtest_e_user', 'U') IS NOT NULL drop table migtest_e_user;
IF OBJECT_ID('migtest_e_user_seq', 'SO') IS NOT NULL drop sequence migtest_e_user_seq;
IF OBJECT_ID('migtest_mtm_c_migtest_mtm_m', 'U') IS NOT NULL drop table migtest_mtm_c_migtest_mtm_m;
IF OBJECT_ID('migtest_mtm_m_migtest_mtm_c', 'U') IS NOT NULL drop table migtest_mtm_m_migtest_mtm_c;
-- dropping history support for migtest_e_history;
EXEC usp_ebean_drop_default_constraint migtest_e_history, sys_periodFrom;
EXEC usp_ebean_drop_default_constraint migtest_e_history, sys_periodTo;
alter table migtest_e_history set (system_versioning = off);
alter table migtest_e_history drop period for system_time;
alter table migtest_e_history drop column sys_periodFrom;
alter table migtest_e_history drop column sys_periodTo;
IF OBJECT_ID('migtest_e_history_history', 'U') IS NOT NULL drop table migtest_e_history_history;

