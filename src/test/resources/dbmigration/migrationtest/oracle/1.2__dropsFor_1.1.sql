-- apply changes
-- Migrationscript for oracle;
-- identity type: SEQUENCE;

alter table migtest_e_basic drop column old_boolean;

alter table migtest_e_basic drop column old_boolean2;

alter table migtest_e_basic drop column eref_id;

drop table migtest_e_ref cascade constraints purge;
drop sequence migtest_e_ref_seq;
