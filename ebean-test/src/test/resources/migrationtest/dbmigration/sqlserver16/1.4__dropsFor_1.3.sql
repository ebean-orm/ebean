-- Migrationscripts for ebean unittest
-- apply changes
-- dropping history support for migtest_e_history;
alter table migtest_e_history set (system_versioning = off);
alter table migtest_e_history drop period for system_time;

-- alter table migtest_e_history2 set (system_versioning = off (history_table=dbo.migtest_e_history2_history));
-- history migration goes here
-- alter table migtest_e_history2 set (system_versioning = on (history_table=dbo.migtest_e_history2_history));
-- alter table migtest_e_history5 set (system_versioning = off (history_table=dbo.migtest_e_history5_history));
-- history migration goes here
-- alter table migtest_e_history5 set (system_versioning = on (history_table=dbo.migtest_e_history5_history));
-- alter table [table] set (system_versioning = off (history_table=dbo.table_history));
-- history migration goes here
-- alter table [table] set (system_versioning = on (history_table=dbo.table_history));
-- apply alter tables
EXEC usp_ebean_drop_column "table", textfield;
EXEC usp_ebean_drop_column "table", textfield2;
EXEC usp_ebean_drop_column migtest_ckey_detail, one_key;
EXEC usp_ebean_drop_column migtest_ckey_detail, two_key;
EXEC usp_ebean_drop_column migtest_ckey_parent, assoc_id;
EXEC usp_ebean_drop_column migtest_e_basic, new_string_field;
EXEC usp_ebean_drop_column migtest_e_basic, new_boolean_field;
EXEC usp_ebean_drop_column migtest_e_basic, new_boolean_field2;
EXEC usp_ebean_drop_column migtest_e_basic, progress;
EXEC usp_ebean_drop_column migtest_e_basic, new_integer;
EXEC usp_ebean_drop_default_constraint migtest_e_history, sys_periodFrom;
EXEC usp_ebean_drop_default_constraint migtest_e_history, sys_periodTo;
EXEC usp_ebean_drop_column migtest_e_history, sys_periodFrom;
EXEC usp_ebean_drop_column migtest_e_history, sys_periodTo;
EXEC usp_ebean_drop_column migtest_e_history2, test_string2;
EXEC usp_ebean_drop_column migtest_e_history2, test_string3;
EXEC usp_ebean_drop_column migtest_e_history2, new_column;
EXEC usp_ebean_drop_column migtest_e_history5, test_boolean;
EXEC usp_ebean_drop_column migtest_e_softdelete, deleted;
EXEC usp_ebean_drop_column migtest_oto_child, master_id;
-- apply post alter
IF OBJECT_ID('migtest_e_history_history', 'U') IS NOT NULL drop table migtest_e_history_history;
IF OBJECT_ID('drop_main', 'U') IS NOT NULL drop table drop_main;
IF OBJECT_ID('drop_main_seq', 'SO') IS NOT NULL drop sequence drop_main_seq;
IF OBJECT_ID('drop_main_drop_ref_many', 'U') IS NOT NULL drop table drop_main_drop_ref_many;
IF OBJECT_ID('drop_ref_many', 'U') IS NOT NULL drop table drop_ref_many;
IF OBJECT_ID('drop_ref_many_seq', 'SO') IS NOT NULL drop sequence drop_ref_many_seq;
IF OBJECT_ID('drop_ref_one', 'U') IS NOT NULL drop table drop_ref_one;
IF OBJECT_ID('drop_ref_one_seq', 'SO') IS NOT NULL drop sequence drop_ref_one_seq;
IF OBJECT_ID('drop_ref_one_to_one', 'U') IS NOT NULL drop table drop_ref_one_to_one;
IF OBJECT_ID('drop_ref_one_to_one_seq', 'SO') IS NOT NULL drop sequence drop_ref_one_to_one_seq;
IF OBJECT_ID('migtest_e_test_binary', 'U') IS NOT NULL drop table migtest_e_test_binary;
IF OBJECT_ID('migtest_e_test_binary_seq', 'SO') IS NOT NULL drop sequence migtest_e_test_binary_seq;
IF OBJECT_ID('migtest_e_test_json', 'U') IS NOT NULL drop table migtest_e_test_json;
IF OBJECT_ID('migtest_e_test_json_seq', 'SO') IS NOT NULL drop sequence migtest_e_test_json_seq;
IF OBJECT_ID('migtest_e_test_lob', 'U') IS NOT NULL drop table migtest_e_test_lob;
IF OBJECT_ID('migtest_e_test_lob_seq', 'SO') IS NOT NULL drop sequence migtest_e_test_lob_seq;
IF OBJECT_ID('migtest_e_test_varchar', 'U') IS NOT NULL drop table migtest_e_test_varchar;
IF OBJECT_ID('migtest_e_test_varchar_seq', 'SO') IS NOT NULL drop sequence migtest_e_test_varchar_seq;
IF OBJECT_ID('migtest_e_user', 'U') IS NOT NULL drop table migtest_e_user;
IF OBJECT_ID('migtest_e_user_seq', 'SO') IS NOT NULL drop sequence migtest_e_user_seq;
IF OBJECT_ID('migtest_mtm_c_migtest_mtm_m', 'U') IS NOT NULL drop table migtest_mtm_c_migtest_mtm_m;
IF OBJECT_ID('migtest_mtm_m_migtest_mtm_c', 'U') IS NOT NULL drop table migtest_mtm_m_migtest_mtm_c;
IF OBJECT_ID('migtest_mtm_m_phone_numbers', 'U') IS NOT NULL drop table migtest_mtm_m_phone_numbers;
