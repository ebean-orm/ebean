-- Migrationscripts for ebean unittest
-- apply changes
CALL usp_ebean_drop_column('migtest_e_basic', 'old_boolean');

CALL usp_ebean_drop_column('migtest_e_basic', 'old_boolean2');

CALL usp_ebean_drop_column('migtest_e_basic', 'eref_id');

CALL usp_ebean_drop_column('migtest_e_history2', 'obsolete_string1');

CALL usp_ebean_drop_column('migtest_e_history2', 'obsolete_string2');

drop table if exists migtest_e_ref;
drop sequence if exists migtest_e_ref_seq;
