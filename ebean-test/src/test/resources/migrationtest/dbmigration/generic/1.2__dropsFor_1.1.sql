-- Migrationscripts for ebean unittest
-- apply alter tables
alter table migtest_e_basic drop column description_file;
alter table migtest_e_basic drop column old_boolean;
alter table migtest_e_basic drop column old_boolean2;
alter table migtest_e_basic drop column eref_id;
alter table migtest_e_history2 drop column obsolete_string1;
alter table migtest_e_history2 drop column obsolete_string2;
-- apply post alter
drop table if exists drop_main;
drop table if exists drop_main_drop_ref_many;
drop table if exists drop_ref_many;
drop table if exists drop_ref_one;
drop table if exists "migtest_QuOtEd";
drop table if exists migtest_e_ref;
