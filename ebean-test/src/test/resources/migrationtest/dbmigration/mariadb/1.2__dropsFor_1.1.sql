-- Migrationscripts for ebean unittest
-- apply changes
SET @@system_versioning_alter_history = 1;
-- apply alter tables
CALL usp_ebean_drop_column('migtest_e_basic', 'description_file');
CALL usp_ebean_drop_column('migtest_e_basic', 'old_boolean');
CALL usp_ebean_drop_column('migtest_e_basic', 'old_boolean2');
CALL usp_ebean_drop_column('migtest_e_basic', 'eref_id');
CALL usp_ebean_drop_column('migtest_e_history2', 'obsolete_string1');
CALL usp_ebean_drop_column('migtest_e_history2', 'obsolete_string2');
-- apply post alter
drop table if exists drop_main;
drop sequence if exists drop_main_seq;
drop table if exists drop_main_drop_ref_many;
drop table if exists drop_ref_many;
drop sequence if exists drop_ref_many_seq;
drop table if exists drop_ref_one;
drop sequence if exists drop_ref_one_seq;
drop table if exists `migtest_QuOtEd`;
drop table if exists migtest_e_ref;
drop sequence if exists migtest_e_ref_seq;
