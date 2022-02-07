-- Migrationscripts for ebean unittest
-- apply changes
CALL usp_ebean_drop_column('migtest_ckey_detail', 'one_key');

CALL usp_ebean_drop_column('migtest_ckey_detail', 'two_key');

CALL usp_ebean_drop_column('migtest_ckey_parent', 'assoc_id');

CALL usp_ebean_drop_column('migtest_e_basic', 'new_string_field');

CALL usp_ebean_drop_column('migtest_e_basic', 'new_boolean_field');

CALL usp_ebean_drop_column('migtest_e_basic', 'new_boolean_field2');

CALL usp_ebean_drop_column('migtest_e_basic', 'progress');

CALL usp_ebean_drop_column('migtest_e_basic', 'new_integer');

CALL usp_ebean_drop_column('migtest_e_history2', 'test_string2');

CALL usp_ebean_drop_column('migtest_e_history2', 'test_string3');

CALL usp_ebean_drop_column('migtest_e_history2', 'new_column');

CALL usp_ebean_drop_column('migtest_e_history5', 'test_boolean');

CALL usp_ebean_drop_column('migtest_e_softdelete', 'deleted');

CALL usp_ebean_drop_column('migtest_oto_child', 'master_id');

drop table if exists migtest_e_user;
drop sequence if exists migtest_e_user_seq;
drop table if exists migtest_mtm_c_migtest_mtm_m;
drop table if exists migtest_mtm_m_migtest_mtm_c;
alter table migtest_e_history drop system versioning;
