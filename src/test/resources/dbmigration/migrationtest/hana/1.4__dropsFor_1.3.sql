-- Migrationscripts for ebean unittest
-- drop dependencies
alter table migtest_e_history drop system versioning;
alter table migtest_e_history drop period for system_time;
alter table migtest_e_history drop (sys_period_start,sys_period_end);
drop table migtest_e_history_history cascade;

-- apply changes
CALL usp_ebean_drop_column('migtest_ckey_detail', 'one_key');

CALL usp_ebean_drop_column('migtest_ckey_detail', 'two_key');

CALL usp_ebean_drop_column('migtest_ckey_parent', 'assoc_id');

CALL usp_ebean_drop_column('migtest_e_basic', 'new_string_field');

CALL usp_ebean_drop_column('migtest_e_basic', 'new_boolean_field');

CALL usp_ebean_drop_column('migtest_e_basic', 'new_boolean_field2');

CALL usp_ebean_drop_column('migtest_e_basic', 'progress');

CALL usp_ebean_drop_column('migtest_e_basic', 'new_integer');

alter table migtest_e_history2 drop system versioning /* 0 */;
CALL usp_ebean_drop_column('migtest_e_history2', 'test_string2');

CALL usp_ebean_drop_column('migtest_e_history2_history', 'test_string2');
alter table migtest_e_history2 add system versioning history table migtest_e_history2_history not validated /* 1 */;
alter table migtest_e_history2 drop system versioning /* 2 */;
CALL usp_ebean_drop_column('migtest_e_history2', 'test_string3');

CALL usp_ebean_drop_column('migtest_e_history2_history', 'test_string3');
alter table migtest_e_history2 add system versioning history table migtest_e_history2_history not validated /* 3 */;
alter table migtest_e_history2 drop system versioning /* 4 */;
CALL usp_ebean_drop_column('migtest_e_history2', 'new_column');

CALL usp_ebean_drop_column('migtest_e_history2_history', 'new_column');
alter table migtest_e_history2 add system versioning history table migtest_e_history2_history not validated /* 5 */;
alter table migtest_e_history5 drop system versioning /* 6 */;
CALL usp_ebean_drop_column('migtest_e_history5', 'test_boolean');

CALL usp_ebean_drop_column('migtest_e_history5_history', 'test_boolean');
alter table migtest_e_history5 add system versioning history table migtest_e_history5_history not validated /* 7 */;
CALL usp_ebean_drop_column('migtest_e_softdelete', 'deleted');

CALL usp_ebean_drop_column('migtest_oto_child', 'master_id');

drop table migtest_e_user cascade;
drop table migtest_mtm_c_migtest_mtm_m cascade;
drop table migtest_mtm_m_migtest_mtm_c cascade;
