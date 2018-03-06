-- Migrationscripts for ebean unittest
-- apply changes
alter table migtest_e_basic drop column old_boolean;

alter table migtest_e_basic drop column old_boolean2;

alter table migtest_e_basic drop column eref_id;

drop table if exists migtest_e_ref cascade;
drop sequence if exists migtest_e_ref_seq;
