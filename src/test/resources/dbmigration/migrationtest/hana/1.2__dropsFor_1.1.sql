-- Migrationscripts for ebean unittest
-- apply changes
CALL usp_ebean_drop_column('migtest_e_basic', 'old_boolean');

CALL usp_ebean_drop_column('migtest_e_basic', 'old_boolean2');

CALL usp_ebean_drop_column('migtest_e_basic', 'eref_id');

alter table migtest_e_history2 drop system versioning /* 0 */;
CALL usp_ebean_drop_column('migtest_e_history2', 'obsolete_string1');

CALL usp_ebean_drop_column('migtest_e_history2_history', 'obsolete_string1');
alter table migtest_e_history2 add system versioning history table migtest_e_history2_history not validated /* 1 */;
alter table migtest_e_history2 drop system versioning /* 2 */;
CALL usp_ebean_drop_column('migtest_e_history2', 'obsolete_string2');

CALL usp_ebean_drop_column('migtest_e_history2_history', 'obsolete_string2');
alter table migtest_e_history2 add system versioning history table migtest_e_history2_history not validated /* 3 */;
drop table migtest_e_ref cascade;
